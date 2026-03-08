package com.statz.app.data.repository

import com.statz.app.data.local.dao.SalesDao
import com.statz.app.data.local.model.DailySalesRecordEntity
import com.statz.app.data.local.model.DailySalesValueEntity
import com.statz.app.data.local.model.MonthlyTargetEntity
import com.statz.app.data.local.model.SalesCategoryEntity
import com.statz.app.domain.model.CategoryType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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

        val result = mutableListOf<List<SalesCategoryEntity>>()
        repository.observeActiveCategories().collect {
            result.add(it)
            return@collect // take first emission
        }

        assertEquals(1, result.size)
        assertEquals(2, result[0].size)
        assertEquals("new", result[0][0].id)
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
        assertEquals(100L, fakeDao.lastUpsertedTarget?.target)
    }

    @Test
    fun `incrementCategory increments existing value`() = runTest {
        // Pre-fill a value
        fakeDao.dailyValuesMap["2024-01-15"] = mutableListOf(
            DailySalesValueEntity("2024-01-15_new", "2024-01-15", "new", 5L)
        )
        fakeDao.dailyRecordsMap["2024-01-15"] = DailySalesRecordEntity(
            dateKey = "2024-01-15", monthKey = "2024-01", updatedAt = 0L,
            openOrdersNew = 0, openOrdersUpgrade = 0, declinedNew = 0, declinedUpgrade = 0
        )

        repository.incrementCategory("new", "2024-01-15")

        // Value should be 6 now (5 + 1)
        val updated = fakeDao.dailyValuesMap["2024-01-15"]?.find { it.categoryId == "new" }
        assertEquals(6L, updated?.value)
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

    override fun observeActiveCategories(): Flow<List<SalesCategoryEntity>> = categoriesFlow

    override suspend fun insertCategories(categories: List<SalesCategoryEntity>) {}

    override fun observeTargetsForMonth(monthKey: String): Flow<List<MonthlyTargetEntity>> =
        flowOf(emptyList())

    override suspend fun getTargetsForMonth(monthKey: String): List<MonthlyTargetEntity> =
        emptyList()

    override suspend fun upsertTarget(target: MonthlyTargetEntity) {
        lastUpsertedTarget = target
    }

    override suspend fun upsertDailyRecord(record: DailySalesRecordEntity) {
        lastUpsertedRecord = record
        dailyRecordsMap[record.dateKey] = record
    }

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

    override suspend fun getDailyRecord(dateKey: String): DailySalesRecordEntity? =
        dailyRecordsMap[dateKey]

    override suspend fun getMonthlyValuesSum(monthKey: String): List<DailySalesValueEntity> =
        emptyList()

    override suspend fun getMonthlyRecordsSummed(monthKey: String): DailySalesRecordEntity? = null
}
