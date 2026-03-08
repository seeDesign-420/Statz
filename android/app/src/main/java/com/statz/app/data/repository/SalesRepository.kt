package com.statz.app.data.repository

import com.statz.app.data.local.dao.SalesDao
import com.statz.app.data.local.model.DailySalesRecordEntity
import com.statz.app.data.local.model.DailySalesValueEntity
import com.statz.app.data.local.model.MonthlyTargetEntity
import com.statz.app.data.local.model.SalesCategoryEntity
import com.statz.app.domain.model.AppConfig
import com.statz.app.domain.model.CategoryType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Dashboard data for a single category in a month.
 */
data class CategoryDashboard(
    val category: SalesCategoryEntity,
    val target: Long,
    val actual: Long,
    val progressPercent: Int // 0–100, -1 for "no target" (T=0)
)

/**
 * Full monthly dashboard summary.
 */
data class MonthDashboard(
    val monthKey: String,
    val totalUnits: Long,
    val totalRevenue: Long, // cents — Accessories + Cash Sales
    val categories: List<CategoryDashboard>,
    val openOrdersNew: Int,
    val openOrdersUpgrade: Int,
    val declinedNew: Int,
    val declinedUpgrade: Int
)

/**
 * All daily entry data for a given date.
 */
data class DailyEntry(
    val dateKey: String,
    val categoryValues: Map<String, Long>, // categoryId -> value
    val openOrdersNew: Int,
    val openOrdersUpgrade: Int,
    val declinedNew: Int,
    val declinedUpgrade: Int
)

@Singleton
class SalesRepository @Inject constructor(
    private val salesDao: SalesDao
) {

    private val timezone = AppConfig.TIMEZONE

    fun observeActiveCategories(): Flow<List<SalesCategoryEntity>> =
        salesDao.observeActiveCategories().distinctUntilChanged()

    // ── Dashboard ───────────────────────────────────────────────

    /**
     * Build the full month dashboard per spec §2.3.A
     */
    suspend fun getMonthDashboard(monthKey: String): MonthDashboard {
        val categories = salesDao.getActiveCategories()
        val targets = salesDao.observeTargetsForMonth(monthKey).first()
        val targetMap = targets.associateBy { it.categoryId }

        var totalUnits = 0L
        var totalRevenue = 0L

        val categoryRows = categories.map { cat ->
            val target = targetMap[cat.id]?.targetValue ?: 0L
            val actual = salesDao.getMonthlyActual(monthKey, cat.id)

            val progress = if (target == 0L) -1 else ((actual * 100) / target).toInt()

            when (cat.type) {
                CategoryType.UNIT -> totalUnits += actual
                CategoryType.MONEY -> totalRevenue += actual
            }

            CategoryDashboard(
                category = cat,
                target = target,
                actual = actual,
                progressPercent = progress
            )
        }

        return MonthDashboard(
            monthKey = monthKey,
            totalUnits = totalUnits,
            totalRevenue = totalRevenue,
            categories = categoryRows,
            openOrdersNew = salesDao.getMonthlyOpenOrdersNew(monthKey),
            openOrdersUpgrade = salesDao.getMonthlyOpenOrdersUpgrade(monthKey),
            declinedNew = salesDao.getMonthlyDeclinedNew(monthKey),
            declinedUpgrade = salesDao.getMonthlyDeclinedUpgrade(monthKey)
        )
    }

    // ── Targets ─────────────────────────────────────────────────

    fun observeTargetsForMonth(monthKey: String): Flow<List<MonthlyTargetEntity>> =
        salesDao.observeTargetsForMonth(monthKey).distinctUntilChanged()

    suspend fun saveTarget(monthKey: String, categoryId: String, value: Long) {
        val now = System.currentTimeMillis()
        salesDao.upsertTarget(
            MonthlyTargetEntity(
                id = "${monthKey}_${categoryId}",
                monthKey = monthKey,
                categoryId = categoryId,
                targetValue = value,
                updatedAt = now
            )
        )
    }

    // ── Daily Entry ─────────────────────────────────────────────

    /**
     * Get existing daily entry for a date, or defaults (all zeros).
     */
    suspend fun getDailyEntry(dateKey: String): DailyEntry {
        val record = salesDao.getDailyRecord(dateKey)
        val values = salesDao.getDailyValues(dateKey)
        val valueMap = values.associate { it.categoryId to it.value }

        return DailyEntry(
            dateKey = dateKey,
            categoryValues = valueMap,
            openOrdersNew = record?.openOrdersNew ?: 0,
            openOrdersUpgrade = record?.openOrdersUpgrade ?: 0,
            declinedNew = record?.declinedNew ?: 0,
            declinedUpgrade = record?.declinedUpgrade ?: 0
        )
    }

    /**
     * Save or update daily entry (idempotent per spec §2.3.B).
     */
    suspend fun saveDailyEntry(entry: DailyEntry) {
        val now = System.currentTimeMillis()
        val monthKey = entry.dateKey.substring(0, 7) // YYYY-MM

        val record = DailySalesRecordEntity(
            dateKey = entry.dateKey,
            monthKey = monthKey,
            updatedAt = now,
            openOrdersNew = entry.openOrdersNew,
            openOrdersUpgrade = entry.openOrdersUpgrade,
            declinedNew = entry.declinedNew,
            declinedUpgrade = entry.declinedUpgrade
        )

        val salesValues = entry.categoryValues.map { (categoryId, value) ->
            DailySalesValueEntity(
                id = "${entry.dateKey}_${categoryId}",
                dateKey = entry.dateKey,
                categoryId = categoryId,
                value = value
            )
        }

        salesDao.upsertDailyRecordWithValues(record, salesValues)
    }

    /**
     * Quick +1 for unit categories, adds to today's entry.
     */
    suspend fun incrementCategory(categoryId: String) {
        val today = LocalDate.now(timezone)
        val dateKey = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val entry = getDailyEntry(dateKey)
        val currentValue = entry.categoryValues[categoryId] ?: 0L
        val updated = entry.copy(
            categoryValues = entry.categoryValues + (categoryId to currentValue + 1)
        )
        saveDailyEntry(updated)
    }

    /**
     * Quick +R for money categories, adds to today's entry.
     * @param amountCents The amount to add in cents.
     */
    suspend fun addMoneyToCategory(categoryId: String, amountCents: Long) {
        val today = LocalDate.now(timezone)
        val dateKey = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val entry = getDailyEntry(dateKey)
        val currentValue = entry.categoryValues[categoryId] ?: 0L
        val updated = entry.copy(
            categoryValues = entry.categoryValues + (categoryId to currentValue + amountCents)
        )
        saveDailyEntry(updated)
    }

    // ── Export ───────────────────────────────────────────────────

    /**
     * Get category dashboards for a given month (used by clipboard export).
     */
    suspend fun getMonthCategoryDashboards(monthKey: String): List<CategoryDashboard> {
        val categories = salesDao.getActiveCategories()
        val targets = salesDao.observeTargetsForMonth(monthKey).first()
        val targetMap = targets.associateBy { it.categoryId }

        return categories.map { cat ->
            val target = targetMap[cat.id]?.targetValue ?: 0L
            val actual = salesDao.getMonthlyActual(monthKey, cat.id)
            val progress = if (target == 0L) -1 else ((actual * 100) / target).toInt()
            CategoryDashboard(
                category = cat,
                target = target,
                actual = actual,
                progressPercent = progress
            )
        }
    }

    // ── Helpers ─────────────────────────────────────────────────

    fun currentMonthKey(): String {
        val today = LocalDate.now(timezone)
        return today.format(DateTimeFormatter.ofPattern("yyyy-MM"))
    }

    fun todayDateKey(): String {
        val today = LocalDate.now(timezone)
        return today.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }
}
