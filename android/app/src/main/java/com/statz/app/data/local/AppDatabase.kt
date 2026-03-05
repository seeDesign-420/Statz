package com.statz.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.statz.app.data.local.dao.QueryDao
import com.statz.app.data.local.dao.SalesDao
import com.statz.app.data.local.dao.TaskDao
import com.statz.app.data.local.model.DailySalesRecordEntity
import com.statz.app.data.local.model.DailySalesValueEntity
import com.statz.app.data.local.model.MonthlyTargetEntity
import com.statz.app.data.local.model.QueryItemEntity
import com.statz.app.data.local.model.QueryLogEntryEntity
import com.statz.app.data.local.model.SalesCategoryEntity
import com.statz.app.data.local.model.TaskItemEntity
import com.statz.app.domain.model.CategoryType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SalesCategoryEntity::class,
        MonthlyTargetEntity::class,
        DailySalesRecordEntity::class,
        DailySalesValueEntity::class,
        QueryItemEntity::class,
        QueryLogEntryEntity::class,
        TaskItemEntity::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun salesDao(): SalesDao
    abstract fun queryDao(): QueryDao
    abstract fun taskDao(): TaskDao

    companion object {

        const val DATABASE_NAME = "statz_db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Migration from v1 to v2: add custom_follow_up_hours column.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE query_items ADD COLUMN custom_follow_up_hours INTEGER DEFAULT NULL")
            }
        }

        /**
         * Migration from v2 to v3: replace TaskPriority with QueryUrgency.
         * Renames priority → urgency, adds custom_follow_up_hours and next_follow_up_at.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create new table with the updated schema
                db.execSQL("""
                    CREATE TABLE task_items_new (
                        id TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        notes TEXT,
                        due_at INTEGER,
                        urgency TEXT NOT NULL DEFAULT 'MEDIUM',
                        custom_follow_up_hours INTEGER,
                        next_follow_up_at INTEGER,
                        is_done INTEGER NOT NULL DEFAULT 0,
                        reminder_enabled INTEGER NOT NULL DEFAULT 0,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL
                    )
                """.trimIndent())
                // Copy data, mapping priority → urgency (values are the same names)
                db.execSQL("""
                    INSERT INTO task_items_new (id, title, notes, due_at, urgency, is_done, reminder_enabled, created_at, updated_at)
                    SELECT id, title, notes, due_at, priority, is_done, reminder_enabled, created_at, updated_at
                    FROM task_items
                """.trimIndent())
                // Drop old table and rename new one
                db.execSQL("DROP TABLE task_items")
                db.execSQL("ALTER TABLE task_items_new RENAME TO task_items")
            }
        }

        /**
         * Get or create the database instance.
         * Used by BroadcastReceivers where Hilt DI is not available.
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .addCallback(createSeedCallback {
                        INSTANCE ?: throw IllegalStateException("DB not initialized")
                    })
                    .build()
                    .also { INSTANCE = it }
            }
        }

        /**
         * Set the instance (called from DI module to share the same instance).
         */
        fun setInstance(db: AppDatabase) {
            INSTANCE = db
        }

        /**
         * Seeded sales categories per spec §2.1.
         */
        val SEED_CATEGORIES = listOf(
            // Unit categories
            SalesCategoryEntity("new", "New", CategoryType.UNIT, 0),
            SalesCategoryEntity("upgrade", "Upgrade", CategoryType.UNIT, 1),
            SalesCategoryEntity("sme_new", "SME New", CategoryType.UNIT, 2),
            SalesCategoryEntity("sme_up", "SME Up", CategoryType.UNIT, 3),
            SalesCategoryEntity("ec_new", "Employee Connect New", CategoryType.UNIT, 4),
            SalesCategoryEntity("ec_upgd", "Employee Connect Upgd", CategoryType.UNIT, 5),
            SalesCategoryEntity("fiber", "Fiber", CategoryType.UNIT, 6),
            SalesCategoryEntity("home_wifi_contract", "Home WiFi Contract", CategoryType.UNIT, 7),
            SalesCategoryEntity("home_wifi_mtm", "Home WiFi MTM", CategoryType.UNIT, 8),
            SalesCategoryEntity("insurance", "Insurance", CategoryType.UNIT, 9),
            // Money categories
            SalesCategoryEntity("accessories", "Accessories", CategoryType.MONEY, 10),
            SalesCategoryEntity("cash_sales", "Cash Sales", CategoryType.MONEY, 11)
        )

        /**
         * Create a Room database callback that seeds default categories on first creation.
         */
        fun createSeedCallback(database: () -> AppDatabase): Callback {
            return object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    CoroutineScope(Dispatchers.IO).launch {
                        database().salesDao().insertCategories(SEED_CATEGORIES)
                    }
                }
            }
        }
    }
}
