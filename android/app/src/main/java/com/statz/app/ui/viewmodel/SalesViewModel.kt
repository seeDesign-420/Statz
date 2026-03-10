package com.statz.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statz.app.data.local.model.SalesCategoryEntity
import com.statz.app.data.repository.CategoryDashboard
import com.statz.app.data.repository.DailyEntry
import com.statz.app.data.repository.MonthDashboard
import com.statz.app.data.repository.SalesRepository
import com.statz.app.data.settings.SettingsDataStore
import com.statz.app.domain.export.ClipboardExporter
import androidx.compose.runtime.Immutable
import com.statz.app.domain.model.AppConfig
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@Immutable
data class SalesUiState(
    val monthKey: String = "",
    val monthDisplay: String = "",
    val dashboard: MonthDashboard? = null,
    val dailyRevenueHistory: List<Double> = emptyList(),
    val isLoading: Boolean = true
)

@Immutable
data class DailyEntryUiState(
    val dateKey: String = "",
    val dateDisplay: String = "",
    val categories: List<SalesCategoryEntity> = emptyList(),
    val entry: DailyEntry? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false
)

@Immutable
data class TargetsUiState(
    val monthKey: String = "",
    val monthDisplay: String = "",
    val categories: List<SalesCategoryEntity> = emptyList(),
    val targets: Map<String, Long> = emptyMap(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false
)

@HiltViewModel
class SalesViewModel @Inject constructor(
    private val salesRepository: SalesRepository,
    private val settingsDataStore: SettingsDataStore,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private companion object {
        const val KEY_MONTH = "current_month"
    }

    private val timezone = AppConfig.TIMEZONE
    private val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

    private val _salesState = MutableStateFlow(SalesUiState())
    val salesState: StateFlow<SalesUiState> = _salesState.asStateFlow()

    private val _dailyEntryState = MutableStateFlow(DailyEntryUiState())
    val dailyEntryState: StateFlow<DailyEntryUiState> = _dailyEntryState.asStateFlow()

    private val _targetsState = MutableStateFlow(TargetsUiState())
    val targetsState: StateFlow<TargetsUiState> = _targetsState.asStateFlow()

    private val _clipboardText = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val clipboardText: SharedFlow<String> = _clipboardText

    private var currentYearMonth = YearMonth.now(timezone)

    val categories = salesRepository.observeActiveCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadDashboard(currentYearMonth)
    }

    // ── Dashboard ───────────────────────────────────────────────

    fun loadDashboard(yearMonth: YearMonth = currentYearMonth) {
        currentYearMonth = yearMonth
        val monthKey = yearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        _salesState.value = _salesState.value.copy(
            monthKey = monthKey,
            monthDisplay = yearMonth.format(monthFormatter),
            isLoading = true
        )
        viewModelScope.launch {
            val dashboard = salesRepository.getMonthDashboard(monthKey)
            val revenueHistory = salesRepository.getDailyRevenueHistory(monthKey)
            _salesState.value = _salesState.value.copy(
                dashboard = dashboard,
                dailyRevenueHistory = revenueHistory,
                isLoading = false
            )
        }
    }

    fun previousMonth() = loadDashboard(currentYearMonth.minusMonths(1))
    fun nextMonth() = loadDashboard(currentYearMonth.plusMonths(1))

    fun quickAddUnit(categoryId: String) {
        viewModelScope.launch {
            salesRepository.incrementCategory(categoryId)
            loadDashboard()
        }
    }

    fun quickAddMoney(categoryId: String, amountCents: Long) {
        viewModelScope.launch {
            salesRepository.addMoneyToCategory(categoryId, amountCents)
            loadDashboard()
        }
    }

    // ── Daily Entry ─────────────────────────────────────────────

    fun loadDailyEntry(dateKey: String) {
        val date = LocalDate.parse(dateKey)
        val display = date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
        _dailyEntryState.value = DailyEntryUiState(
            dateKey = dateKey,
            dateDisplay = display,
            isLoading = true
        )
        viewModelScope.launch {
            val cats = salesRepository.observeActiveCategories().first()
            val entry = salesRepository.getDailyEntry(dateKey)
            _dailyEntryState.value = _dailyEntryState.value.copy(
                categories = cats,
                entry = entry,
                isLoading = false
            )
        }
    }

    fun saveDailyEntry(entry: DailyEntry) {
        _dailyEntryState.value = _dailyEntryState.value.copy(isSaving = true)
        viewModelScope.launch {
            salesRepository.saveDailyEntry(entry)

            // Generate clipboard report
            try {
                val monthKey = entry.dateKey.substring(0, 7)
                val displayName = settingsDataStore.settings.first().displayName
                val monthlyCategories = salesRepository.getMonthCategoryDashboards(monthKey)
                val report = ClipboardExporter.formatDailyReport(
                    displayName = displayName,
                    dateKey = entry.dateKey,
                    monthlyCategories = monthlyCategories,
                    dailyEntry = entry
                )
                _clipboardText.tryEmit(report)
            } catch (_: Exception) {
                // Clipboard export is best-effort, don't block save
            }

            _dailyEntryState.value = _dailyEntryState.value.copy(
                isSaving = false,
                savedSuccessfully = true
            )
            loadDashboard()
        }
    }

    // ── Targets ─────────────────────────────────────────────────

    fun loadTargets(monthKey: String) {
        val yearMonth = YearMonth.parse(monthKey)
        val display = yearMonth.format(monthFormatter)
        _targetsState.value = TargetsUiState(
            monthKey = monthKey,
            monthDisplay = display,
            isLoading = true
        )
        viewModelScope.launch {
            val cats = salesRepository.observeActiveCategories().first()
            val targets = salesRepository.observeTargetsForMonth(monthKey).first()
            _targetsState.value = _targetsState.value.copy(
                categories = cats,
                targets = targets.associate { it.categoryId to it.targetValue },
                isLoading = false
            )
        }
    }

    fun saveTarget(monthKey: String, categoryId: String, value: Long) {
        viewModelScope.launch {
            salesRepository.saveTarget(monthKey, categoryId, value)
        }
    }

    fun saveAllTargets(monthKey: String, targets: Map<String, Long>) {
        _targetsState.value = _targetsState.value.copy(isSaving = true)
        viewModelScope.launch {
            targets.forEach { (categoryId, value) ->
                salesRepository.saveTarget(monthKey, categoryId, value)
            }
            _targetsState.value = _targetsState.value.copy(isSaving = false)
            loadDashboard()
        }
    }

    fun currentMonthKey(): String = salesRepository.currentMonthKey()
    fun todayDateKey(): String = salesRepository.todayDateKey()
}
