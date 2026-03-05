package com.statz.app.data.backup

import android.content.Context
import android.net.Uri
import com.statz.app.data.local.AppDatabase
import com.statz.app.data.local.model.DailySalesRecordEntity
import com.statz.app.data.local.model.DailySalesValueEntity
import com.statz.app.data.local.model.MonthlyTargetEntity
import com.statz.app.data.local.model.QueryItemEntity
import com.statz.app.data.local.model.QueryLogEntryEntity
import com.statz.app.data.local.model.SalesCategoryEntity
import com.statz.app.data.local.model.TaskItemEntity
import com.statz.app.domain.model.CategoryType
import com.statz.app.domain.model.QueryStatus
import com.statz.app.domain.model.QueryUrgency
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * JSON backup/restore for all database tables per spec §5.1.
 */

@Serializable
data class BackupData(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val categories: List<CategoryBackup>,
    val monthlyTargets: List<MonthlyTargetBackup>,
    val dailyRecords: List<DailyRecordBackup>,
    val dailyValues: List<DailyValueBackup>,
    val queries: List<QueryBackup>,
    val queryLogs: List<QueryLogBackup>,
    val tasks: List<TaskBackup>
)

@Serializable
data class CategoryBackup(
    val id: String, val name: String, val type: String,
    val sortOrder: Int, val isActive: Boolean
)

@Serializable
data class MonthlyTargetBackup(
    val id: String, val monthKey: String, val categoryId: String,
    val targetValue: Long, val updatedAt: Long
)

@Serializable
data class DailyRecordBackup(
    val dateKey: String, val monthKey: String, val updatedAt: Long,
    val openOrdersNew: Int, val openOrdersUpgrade: Int,
    val declinedNew: Int, val declinedUpgrade: Int
)

@Serializable
data class DailyValueBackup(
    val id: String, val dateKey: String, val categoryId: String, val value: Long
)

@Serializable
data class QueryBackup(
    val id: String, val ticketNumber: String, val customerId: String,
    val customerName: String, val status: String, val urgency: String,
    val customFollowUpHours: Int? = null,
    val nextFollowUpAt: Long, val createdAt: Long, val updatedAt: Long,
    val closedAt: Long? = null
)

@Serializable
data class QueryLogBackup(
    val id: String, val queryId: String, val timestamp: Long,
    val note: String, val statusAfter: String? = null
)

@Serializable
data class TaskBackup(
    val id: String, val title: String, val notes: String? = null,
    val dueAt: Long? = null, val urgency: String, val customFollowUpHours: Int? = null,
    val nextFollowUpAt: Long? = null, val isDone: Boolean,
    val reminderEnabled: Boolean, val createdAt: Long, val updatedAt: Long,
    // Legacy field for backward compatibility on import
    val priority: String? = null
)

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase
) {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    /**
     * Export all database tables to JSON and write to the given URI.
     */
    suspend fun exportToUri(uri: Uri): kotlin.Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val salesDao = database.salesDao()
            val queryDao = database.queryDao()
            val taskDao = database.taskDao()

            val categories = salesDao.getActiveCategories()
            val backup = BackupData(
                categories = categories.map {
                    CategoryBackup(it.id, it.name, it.type.name, it.sortOrder, it.isActive)
                },
                monthlyTargets = getAllTargets().map {
                    MonthlyTargetBackup(it.id, it.monthKey, it.categoryId, it.targetValue, it.updatedAt)
                },
                dailyRecords = getAllDailyRecords().map {
                    DailyRecordBackup(
                        it.dateKey, it.monthKey, it.updatedAt,
                        it.openOrdersNew, it.openOrdersUpgrade,
                        it.declinedNew, it.declinedUpgrade
                    )
                },
                dailyValues = getAllDailyValues().map {
                    DailyValueBackup(it.id, it.dateKey, it.categoryId, it.value)
                },
                queries = getAllQueries().map {
                    QueryBackup(
                        it.id, it.ticketNumber, it.customerId, it.customerName,
                        it.status.name, it.urgency.name, it.customFollowUpHours,
                        it.nextFollowUpAt, it.createdAt, it.updatedAt, it.closedAt
                    )
                },
                queryLogs = getAllQueryLogs().map {
                    QueryLogBackup(it.id, it.queryId, it.timestamp, it.note, it.statusAfter?.name)
                },
                tasks = getAllTasks().map {
                    TaskBackup(
                        it.id, it.title, it.notes, it.dueAt,
                        it.urgency.name, it.customFollowUpHours, it.nextFollowUpAt,
                        it.isDone, it.reminderEnabled,
                        it.createdAt, it.updatedAt
                    )
                }
            )

            val jsonString = json.encodeToString(backup)
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(jsonString.toByteArray(Charsets.UTF_8))
            }

            kotlin.Result.success(Unit)
        } catch (e: Exception) {
            kotlin.Result.failure(e)
        }
    }

    /**
     * Import from JSON URI, replacing all local data.
     */
    suspend fun importFromUri(uri: Uri): kotlin.Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader().readText()
            } ?: return@withContext kotlin.Result.failure(Exception("Cannot read file"))

            val backup = json.decodeFromString<BackupData>(jsonString)

            database.clearAllTables()

            val salesDao = database.salesDao()
            val queryDao = database.queryDao()
            val taskDao = database.taskDao()

            // Restore categories
            salesDao.insertCategories(backup.categories.map {
                SalesCategoryEntity(it.id, it.name, CategoryType.valueOf(it.type), it.sortOrder, it.isActive)
            })

            // Restore targets
            salesDao.upsertTargets(backup.monthlyTargets.map {
                MonthlyTargetEntity(it.id, it.monthKey, it.categoryId, it.targetValue, it.updatedAt)
            })

            // Restore daily records
            for (record in backup.dailyRecords) {
                salesDao.upsertDailyRecord(
                    DailySalesRecordEntity(
                        record.dateKey, record.monthKey, record.updatedAt,
                        record.openOrdersNew, record.openOrdersUpgrade,
                        record.declinedNew, record.declinedUpgrade
                    )
                )
            }

            // Restore daily values
            salesDao.upsertDailyValues(backup.dailyValues.map {
                DailySalesValueEntity(it.id, it.dateKey, it.categoryId, it.value)
            })

            // Restore queries
            for (q in backup.queries) {
                queryDao.insertQuery(
                    QueryItemEntity(
                        id = q.id,
                        ticketNumber = q.ticketNumber,
                        customerId = q.customerId,
                        customerName = q.customerName,
                        status = QueryStatus.valueOf(q.status),
                        urgency = QueryUrgency.valueOf(q.urgency),
                        customFollowUpHours = q.customFollowUpHours,
                        nextFollowUpAt = q.nextFollowUpAt,
                        createdAt = q.createdAt,
                        updatedAt = q.updatedAt,
                        closedAt = q.closedAt
                    )
                )
            }

            // Restore query logs
            for (log in backup.queryLogs) {
                queryDao.insertLogEntry(
                    QueryLogEntryEntity(
                        log.id, log.queryId, log.timestamp, log.note,
                        log.statusAfter?.let { QueryStatus.valueOf(it) }
                    )
                )
            }

            // Restore tasks
            for (task in backup.tasks) {
                // Support both new urgency field and legacy priority field
                val urgencyStr = task.urgency.ifEmpty { task.priority ?: "MEDIUM" }
                taskDao.insertTask(
                    TaskItemEntity(
                        task.id, task.title, task.notes, task.dueAt,
                        QueryUrgency.valueOf(urgencyStr),
                        task.customFollowUpHours, task.nextFollowUpAt,
                        task.isDone,
                        task.reminderEnabled, task.createdAt, task.updatedAt
                    )
                )
            }

            kotlin.Result.success(Unit)
        } catch (e: Exception) {
            kotlin.Result.failure(e)
        }
    }

    // ── Raw queries for backup export ───────────────────────────
    // These use @RawQuery or direct DAO methods.
    // For MVP, we leverage the existing DAO methods where possible
    // and add simple "get all" queries via the database directly.

    private suspend fun getAllTargets(): List<MonthlyTargetEntity> {
        // We query via a simple raw approach
        return database.salesDao().observeTargetsForMonth("%").let {
            // Workaround: retrieve all months via raw query
            database.runInTransaction<List<MonthlyTargetEntity>> {
                val cursor = database.openHelper.readableDatabase.query(
                    "SELECT * FROM monthly_targets"
                )
                val results = mutableListOf<MonthlyTargetEntity>()
                while (cursor.moveToNext()) {
                    results.add(
                        MonthlyTargetEntity(
                            id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                            monthKey = cursor.getString(cursor.getColumnIndexOrThrow("month_key")),
                            categoryId = cursor.getString(cursor.getColumnIndexOrThrow("category_id")),
                            targetValue = cursor.getLong(cursor.getColumnIndexOrThrow("target_value")),
                            updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow("updated_at"))
                        )
                    )
                }
                cursor.close()
                results
            }
        }
    }

    private suspend fun getAllDailyRecords(): List<DailySalesRecordEntity> {
        val cursor = database.openHelper.readableDatabase.query("SELECT * FROM daily_sales_records")
        val results = mutableListOf<DailySalesRecordEntity>()
        while (cursor.moveToNext()) {
            results.add(
                DailySalesRecordEntity(
                    dateKey = cursor.getString(cursor.getColumnIndexOrThrow("date_key")),
                    monthKey = cursor.getString(cursor.getColumnIndexOrThrow("month_key")),
                    updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow("updated_at")),
                    openOrdersNew = cursor.getInt(cursor.getColumnIndexOrThrow("open_orders_new")),
                    openOrdersUpgrade = cursor.getInt(cursor.getColumnIndexOrThrow("open_orders_upgrade")),
                    declinedNew = cursor.getInt(cursor.getColumnIndexOrThrow("declined_new")),
                    declinedUpgrade = cursor.getInt(cursor.getColumnIndexOrThrow("declined_upgrade"))
                )
            )
        }
        cursor.close()
        return results
    }

    private suspend fun getAllDailyValues(): List<DailySalesValueEntity> {
        val cursor = database.openHelper.readableDatabase.query("SELECT * FROM daily_sales_values")
        val results = mutableListOf<DailySalesValueEntity>()
        while (cursor.moveToNext()) {
            results.add(
                DailySalesValueEntity(
                    id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                    dateKey = cursor.getString(cursor.getColumnIndexOrThrow("date_key")),
                    categoryId = cursor.getString(cursor.getColumnIndexOrThrow("category_id")),
                    value = cursor.getLong(cursor.getColumnIndexOrThrow("value"))
                )
            )
        }
        cursor.close()
        return results
    }

    private suspend fun getAllQueries(): List<QueryItemEntity> {
        val cursor = database.openHelper.readableDatabase.query("SELECT * FROM query_items")
        val results = mutableListOf<QueryItemEntity>()
        while (cursor.moveToNext()) {
            val customHoursIdx = cursor.getColumnIndex("custom_follow_up_hours")
            results.add(
                QueryItemEntity(
                    id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                    ticketNumber = cursor.getString(cursor.getColumnIndexOrThrow("ticket_number")),
                    customerId = cursor.getString(cursor.getColumnIndexOrThrow("customer_id")),
                    customerName = cursor.getString(cursor.getColumnIndexOrThrow("customer_name")),
                    status = QueryStatus.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("status"))),
                    urgency = QueryUrgency.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("urgency"))),
                    customFollowUpHours = if (customHoursIdx >= 0 && !cursor.isNull(customHoursIdx)) cursor.getInt(customHoursIdx) else null,
                    nextFollowUpAt = cursor.getLong(cursor.getColumnIndexOrThrow("next_follow_up_at")),
                    createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at")),
                    updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow("updated_at")),
                    closedAt = if (cursor.isNull(cursor.getColumnIndexOrThrow("closed_at"))) null
                        else cursor.getLong(cursor.getColumnIndexOrThrow("closed_at"))
                )
            )
        }
        cursor.close()
        return results
    }

    private suspend fun getAllQueryLogs(): List<QueryLogEntryEntity> {
        val cursor = database.openHelper.readableDatabase.query("SELECT * FROM query_log_entries")
        val results = mutableListOf<QueryLogEntryEntity>()
        while (cursor.moveToNext()) {
            results.add(
                QueryLogEntryEntity(
                    id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                    queryId = cursor.getString(cursor.getColumnIndexOrThrow("query_id")),
                    timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp")),
                    note = cursor.getString(cursor.getColumnIndexOrThrow("note")),
                    statusAfter = cursor.getString(cursor.getColumnIndexOrThrow("status_after"))
                        ?.let { QueryStatus.valueOf(it) }
                )
            )
        }
        cursor.close()
        return results
    }

    private suspend fun getAllTasks(): List<TaskItemEntity> {
        val cursor = database.openHelper.readableDatabase.query("SELECT * FROM task_items")
        val results = mutableListOf<TaskItemEntity>()
        while (cursor.moveToNext()) {
            val customHoursIdx = cursor.getColumnIndex("custom_follow_up_hours")
            val nextFollowUpIdx = cursor.getColumnIndex("next_follow_up_at")
            results.add(
                TaskItemEntity(
                    id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                    title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    notes = cursor.getString(cursor.getColumnIndexOrThrow("notes")),
                    dueAt = if (cursor.isNull(cursor.getColumnIndexOrThrow("due_at"))) null
                        else cursor.getLong(cursor.getColumnIndexOrThrow("due_at")),
                    urgency = QueryUrgency.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("urgency"))),
                    customFollowUpHours = if (customHoursIdx >= 0 && !cursor.isNull(customHoursIdx)) cursor.getInt(customHoursIdx) else null,
                    nextFollowUpAt = if (nextFollowUpIdx >= 0 && !cursor.isNull(nextFollowUpIdx)) cursor.getLong(nextFollowUpIdx) else null,
                    isDone = cursor.getInt(cursor.getColumnIndexOrThrow("is_done")) == 1,
                    reminderEnabled = cursor.getInt(cursor.getColumnIndexOrThrow("reminder_enabled")) == 1,
                    createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at")),
                    updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow("updated_at"))
                )
            )
        }
        cursor.close()
        return results
    }
}
