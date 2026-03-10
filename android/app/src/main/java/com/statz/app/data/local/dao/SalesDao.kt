package com.statz.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.statz.app.data.local.model.DailySalesRecordEntity
import com.statz.app.data.local.model.DailySalesValueEntity
import com.statz.app.data.local.model.MonthlyTargetEntity
import com.statz.app.data.local.model.SalesCategoryEntity
import com.statz.app.data.repository.DailyRevenueRow
import kotlinx.coroutines.flow.Flow

@Dao
interface SalesDao {

    // ── Categories ──────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategories(categories: List<SalesCategoryEntity>)

    @Query("SELECT * FROM sales_categories WHERE is_active = 1 ORDER BY sort_order ASC")
    fun observeActiveCategories(): Flow<List<SalesCategoryEntity>>

    @Query("SELECT * FROM sales_categories WHERE is_active = 1 ORDER BY sort_order ASC")
    suspend fun getActiveCategories(): List<SalesCategoryEntity>

    // ── Monthly Targets ─────────────────────────────────────────

    @Upsert
    suspend fun upsertTarget(target: MonthlyTargetEntity)

    @Upsert
    suspend fun upsertTargets(targets: List<MonthlyTargetEntity>)

    @Query("SELECT * FROM monthly_targets WHERE month_key = :monthKey")
    fun observeTargetsForMonth(monthKey: String): Flow<List<MonthlyTargetEntity>>

    @Query("SELECT * FROM monthly_targets WHERE month_key = :monthKey AND category_id = :categoryId")
    suspend fun getTarget(monthKey: String, categoryId: String): MonthlyTargetEntity?

    // ── XLSX Import (delete-then-insert) ───────────────────────

    @Query("DELETE FROM monthly_targets WHERE month_key = :monthKey")
    suspend fun deleteTargetsForMonth(monthKey: String)

    /**
     * Delete all daily records for a month.
     * CASCADE FK on daily_sales_values auto-deletes associated values.
     */
    @Query("DELETE FROM daily_sales_records WHERE month_key = :monthKey")
    suspend fun deleteDailyRecordsForMonth(monthKey: String)

    /**
     * Atomically replace all targets and daily sales data for a month.
     * Used by XLSX import — POS data is the source of truth.
     */
    @Transaction
    suspend fun replaceMonthFromXlsx(
        monthKey: String,
        targets: List<MonthlyTargetEntity>,
        records: List<DailySalesRecordEntity>,
        values: List<DailySalesValueEntity>
    ) {
        deleteTargetsForMonth(monthKey)
        deleteDailyRecordsForMonth(monthKey)
        upsertTargets(targets)
        records.forEach { upsertDailyRecord(it) }
        if (values.isNotEmpty()) upsertDailyValues(values)
    }

    // ── Daily Sales Records ─────────────────────────────────────

    @Upsert
    suspend fun upsertDailyRecord(record: DailySalesRecordEntity)

    @Query("SELECT * FROM daily_sales_records WHERE date_key = :dateKey")
    suspend fun getDailyRecord(dateKey: String): DailySalesRecordEntity?

    @Query("SELECT * FROM daily_sales_records WHERE date_key = :dateKey")
    fun observeDailyRecord(dateKey: String): Flow<DailySalesRecordEntity?>

    // ── Daily Sales Values ──────────────────────────────────────

    @Upsert
    suspend fun upsertDailyValues(values: List<DailySalesValueEntity>)

    /**
     * Atomically upsert a daily record and its values together.
     */
    @Transaction
    suspend fun upsertDailyRecordWithValues(
        record: DailySalesRecordEntity,
        values: List<DailySalesValueEntity>
    ) {
        upsertDailyRecord(record)
        if (values.isNotEmpty()) {
            upsertDailyValues(values)
        }
    }

    @Query("SELECT * FROM daily_sales_values WHERE date_key = :dateKey")
    suspend fun getDailyValues(dateKey: String): List<DailySalesValueEntity>

    @Query("SELECT * FROM daily_sales_values WHERE date_key = :dateKey")
    fun observeDailyValues(dateKey: String): Flow<List<DailySalesValueEntity>>

    // ── Monthly Aggregation ─────────────────────────────────────

    /**
     * Sum of daily values for a category within a month.
     * This is the "Actual" (A) value per spec §2.2.
     */
    @Query("""
        SELECT COALESCE(SUM(dsv.value), 0)
        FROM daily_sales_values dsv
        INNER JOIN daily_sales_records dsr ON dsv.date_key = dsr.date_key
        WHERE dsr.month_key = :monthKey AND dsv.category_id = :categoryId
    """)
    suspend fun getMonthlyActual(monthKey: String, categoryId: String): Long

    /**
     * Reactive version: observe the monthly actual for a category.
     */
    @Query("""
        SELECT COALESCE(SUM(dsv.value), 0)
        FROM daily_sales_values dsv
        INNER JOIN daily_sales_records dsr ON dsv.date_key = dsr.date_key
        WHERE dsr.month_key = :monthKey AND dsv.category_id = :categoryId
    """)
    fun observeMonthlyActual(monthKey: String, categoryId: String): Flow<Long>

    /**
     * Sum of open orders (New) for a month.
     */
    @Query("""
        SELECT COALESCE(SUM(open_orders_new), 0)
        FROM daily_sales_records WHERE month_key = :monthKey
    """)
    suspend fun getMonthlyOpenOrdersNew(monthKey: String): Int

    /**
     * Sum of open orders (Upgrade) for a month.
     */
    @Query("""
        SELECT COALESCE(SUM(open_orders_upgrade), 0)
        FROM daily_sales_records WHERE month_key = :monthKey
    """)
    suspend fun getMonthlyOpenOrdersUpgrade(monthKey: String): Int

    /**
     * Sum of declined (New) for a month.
     */
    @Query("""
        SELECT COALESCE(SUM(declined_new), 0)
        FROM daily_sales_records WHERE month_key = :monthKey
    """)
    suspend fun getMonthlyDeclinedNew(monthKey: String): Int

    /**
     * Sum of declined (Upgrade) for a month.
     */
    @Query("""
        SELECT COALESCE(SUM(declined_upgrade), 0)
        FROM daily_sales_records WHERE month_key = :monthKey
    """)
    suspend fun getMonthlyDeclinedUpgrade(monthKey: String): Int

    /**
     * Total revenue for a month across all MONEY categories.
     */
    @Query("""
        SELECT COALESCE(SUM(dsv.value), 0)
        FROM daily_sales_values dsv
        INNER JOIN daily_sales_records dsr ON dsv.date_key = dsr.date_key
        INNER JOIN sales_categories sc ON dsv.category_id = sc.id
        WHERE dsr.month_key = :monthKey AND sc.type = 'MONEY'
    """)
    suspend fun getMonthlyTotalRevenue(monthKey: String): Long


    // ── Daily Revenue History (for wave chart) ──────────────────

    /**
     * Returns the daily total revenue (sum of all money-type category values)
     * for each date in the given month, ordered chronologically.
     */
    @Query("""
        SELECT dsr.date_key, COALESCE(SUM(dsv.value), 0) AS total
        FROM daily_sales_records dsr
        LEFT JOIN daily_sales_values dsv ON dsr.date_key = dsv.date_key
        LEFT JOIN sales_categories sc ON dsv.category_id = sc.id AND sc.type = 'MONEY'
        WHERE dsr.month_key = :monthKey
        GROUP BY dsr.date_key
        ORDER BY dsr.date_key ASC
    """)
    suspend fun getDailyRevenueForMonth(monthKey: String): List<DailyRevenueRow>
}
