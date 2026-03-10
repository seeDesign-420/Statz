package com.statz.app.data.repository

import com.statz.app.data.local.dao.SalesDao
import com.statz.app.data.local.model.DailySalesRecordEntity
import com.statz.app.data.local.model.DailySalesValueEntity
import com.statz.app.data.local.model.MonthlyTargetEntity
import com.statz.app.data.local.model.SalesCategoryEntity
import com.statz.app.domain.model.CategoryType
import com.statz.app.domain.xlsximport.XlsxImportResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SalesRepository business logic.
 * Uses a fake DAO to isolate repository-level behavior.
 */
class SalesRepositoryTest {

    private lateinit var fakeDao: FakeSalesDao
    private lateinit var repository: SalesRepository

    @Before
    fun setUp() {
        fakeDao = FakeSalesDao()
        repository = SalesRepository(fakeDao)
    }

    @Test
    fun `observeActiveCategories emits from DAO`() = runTest {
        val categories = listOf(
            SalesCategoryEntity("new", "New", CategoryType.UNIT, 0),
            SalesCategoryEntity("upgrade", "Upgrade", CategoryType.UNIT, 1)
        )
        fakeDao.categoriesFlow.value = categories

        val emission = repository.observeActiveCategories().first()

        assertEquals(2, emission.size)
        assertEquals("new", emission[0].id)
    }

    @Test
    fun `saveDailyEntry creates record and values`() = runTest {
        val entry = DailyEntry(
            dateKey = "2024-01-15",
            categoryValues = mapOf("new" to 5L, "upgrade" to 3L),
            openOrdersNew = 2,
            openOrdersUpgrade = 1,
            declinedNew = 0,
            declinedUpgrade = 0
        )

        repository.saveDailyEntry(entry)

        // Verify record was upserted
        assertNotNull(fakeDao.lastUpsertedRecord)
        assertEquals("2024-01-15", fakeDao.lastUpsertedRecord?.dateKey)
        assertEquals("2024-01", fakeDao.lastUpsertedRecord?.monthKey)
        assertEquals(2, fakeDao.lastUpsertedRecord?.openOrdersNew)

        // Verify values were upserted
        assertEquals(2, fakeDao.lastUpsertedValues.size)
    }

    @Test
    fun `saveTarget upserts with correct month key`() = runTest {
        repository.saveTarget("2024-01", "new", 100L)

        assertNotNull(fakeDao.lastUpsertedTarget)
        assertEquals("2024-01_new", fakeDao.lastUpsertedTarget?.id)
        assertEquals(100L, fakeDao.lastUpsertedTarget?.targetValue)
    }

    @Test
    fun `importFromXlsx replaces existing targets and actuals`() = runTest {
        val monthKey = "2024-06"

        // Pre-populate stale data that should be deleted on import
        fakeDao.targetsMap[monthKey] = mutableListOf(
            MonthlyTargetEntity("${monthKey}_stale_cat", monthKey, "stale_cat", 999L, 0L)
        )
        fakeDao.dailyRecordsMap["2024-06-01"] = DailySalesRecordEntity(
            dateKey = "2024-06-01", monthKey = monthKey, updatedAt = 0L
        )
        fakeDao.dailyValuesMap["2024-06-01"] = mutableListOf(
            DailySalesValueEntity("2024-06-01_stale_cat", "2024-06-01", "stale_cat", 777L)
        )

        // Import fresh XLSX data
        val result = XlsxImportResult(
            monthKey = monthKey,
            salesPersonName = "Test User",
            targets = mapOf("new" to 10L, "upgrade" to 5L),
            dailyEntries = mapOf(
                "2024-06-03" to mapOf("new" to 2L, "upgrade" to 1L)
            ),
            skippedProducts = emptyList()
        )
        repository.importFromXlsx(result)

        // Stale target should be gone
        val targets = fakeDao.targetsMap[monthKey] ?: emptyList()
        assertNull(targets.find { it.categoryId == "stale_cat" })

        // New targets should be present
        assertNotNull(targets.find { it.categoryId == "new" && it.targetValue == 10L })
        assertNotNull(targets.find { it.categoryId == "upgrade" && it.targetValue == 5L })

        // Stale daily data should be gone
        assertNull(fakeDao.dailyRecordsMap["2024-06-01"])
        assertTrue(fakeDao.dailyValuesMap["2024-06-01"].isNullOrEmpty())

        // New daily data should be present
        assertNotNull(fakeDao.dailyRecordsMap["2024-06-03"])
        assertEquals(2L, fakeDao.dailyValuesMap["2024-06-03"]?.find { it.categoryId == "new" }?.value)
        assertEquals(1L, fakeDao.dailyValuesMap["2024-06-03"]?.find { it.categoryId == "upgrade" }?.value)
    }
}

/**
 * Minimal fake DAO for unit testing SalesRepository.
 */
private class FakeSalesDao : SalesDao {
    val categoriesFlow = MutableStateFlow<List<SalesCategoryEntity>>(emptyList())
    var lastUpsertedRecord: DailySalesRecordEntity? = null
    var lastUpsertedValues: List<DailySalesValueEntity> = emptyList()
    var lastUpsertedTarget: MonthlyTargetEntity? = null
    val dailyValuesMap = mutableMapOf<String, MutableList<DailySalesValueEntity>>()
    val dailyRecordsMap = mutableMapOf<String, DailySalesRecordEntity>()
    val targetsMap = mutableMapOf<String, MutableList<MonthlyTargetEntity>>()

    // ── Categories ──────────────────────────────────────────────

    override fun observeActiveCategories(): Flow<List<SalesCategoryEntity>> = categoriesFlow

    override suspend fun getActiveCategories(): List<SalesCategoryEntity> =
        categoriesFlow.value

    override suspend fun insertCategories(categories: List<SalesCategoryEntity>) {}

    // ── Targets ─────────────────────────────────────────────────

    override fun observeTargetsForMonth(monthKey: String): Flow<List<MonthlyTargetEntity>> =
        flowOf(targetsMap[monthKey] ?: emptyList())

    override suspend fun getTarget(monthKey: String, categoryId: String): MonthlyTargetEntity? =
        targetsMap[monthKey]?.find { it.categoryId == categoryId }

    override suspend fun upsertTarget(target: MonthlyTargetEntity) {
        lastUpsertedTarget = target
        targetsMap.getOrPut(target.monthKey) { mutableListOf() }.apply {
            removeAll { it.categoryId == target.categoryId }
            add(target)
        }
    }

    override suspend fun upsertTargets(targets: List<MonthlyTargetEntity>) {
        targets.forEach { upsertTarget(it) }
    }

    // ── Daily Records ───────────────────────────────────────────

    override suspend fun upsertDailyRecord(record: DailySalesRecordEntity) {
        lastUpsertedRecord = record
        dailyRecordsMap[record.dateKey] = record
    }

    override suspend fun getDailyRecord(dateKey: String): DailySalesRecordEntity? =
        dailyRecordsMap[dateKey]

    override fun observeDailyRecord(dateKey: String): Flow<DailySalesRecordEntity?> =
        flowOf(dailyRecordsMap[dateKey])

    // ── Daily Values ────────────────────────────────────────────

    override suspend fun upsertDailyValues(values: List<DailySalesValueEntity>) {
        lastUpsertedValues = values
        values.forEach { v ->
            dailyValuesMap.getOrPut(v.dateKey) { mutableListOf() }.apply {
                removeAll { it.categoryId == v.categoryId }
                add(v)
            }
        }
    }

    override suspend fun upsertDailyRecordWithValues(
        record: DailySalesRecordEntity,
        values: List<DailySalesValueEntity>
    ) {
        upsertDailyRecord(record)
        if (values.isNotEmpty()) upsertDailyValues(values)
    }

    override suspend fun getDailyValues(dateKey: String): List<DailySalesValueEntity> =
        dailyValuesMap[dateKey] ?: emptyList()

    override fun observeDailyValues(dateKey: String): Flow<List<DailySalesValueEntity>> =
        flowOf(dailyValuesMap[dateKey] ?: emptyList())

    // ── XLSX Import (delete-then-insert) ────────────────────────

    override suspend fun deleteTargetsForMonth(monthKey: String) {
        targetsMap.remove(monthKey)
    }

    override suspend fun deleteDailyRecordsForMonth(monthKey: String) {
        val keysToRemove = dailyRecordsMap.filter { it.value.monthKey == monthKey }.keys.toList()
        keysToRemove.forEach { dateKey ->
            dailyRecordsMap.remove(dateKey)
            dailyValuesMap.remove(dateKey)
        }
    }

    override suspend fun replaceMonthFromXlsx(
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

    // ── Aggregation stubs (not exercised in repository tests) ───

    override suspend fun getMonthlyActual(monthKey: String, categoryId: String): Long = 0L

    override fun observeMonthlyActual(monthKey: String, categoryId: String): Flow<Long> =
        flowOf(0L)

    override suspend fun getMonthlyOpenOrdersNew(monthKey: String): Int = 0
    override suspend fun getMonthlyOpenOrdersUpgrade(monthKey: String): Int = 0
    override suspend fun getMonthlyDeclinedNew(monthKey: String): Int = 0
    override suspend fun getMonthlyDeclinedUpgrade(monthKey: String): Int = 0
    override suspend fun getMonthlyTotalRevenue(monthKey: String): Long = 0L
    override suspend fun getDailyRevenueForMonth(monthKey: String): List<DailyRevenueRow> =
        emptyList()
}
