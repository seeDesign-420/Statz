package com.statz.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statz.app.data.local.model.QueryItemEntity
import com.statz.app.data.local.model.QueryLogEntryEntity
import com.statz.app.data.repository.QueryRepository
import com.statz.app.domain.model.QueryStatus
import com.statz.app.domain.model.QueryUrgency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QueriesUiState(
    val searchQuery: String = "",
    val activeFilter: QueryStatus? = null, // null = All
    val isSearchActive: Boolean = false
)

data class QueryDetailUiState(
    val query: QueryItemEntity? = null,
    val logs: List<QueryLogEntryEntity> = emptyList(),
    val isLoading: Boolean = true,
    val noteText: String = ""
)

data class NewQueryUiState(
    val ticketNumber: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val urgency: QueryUrgency = QueryUrgency.MEDIUM,
    val customFollowUpHours: String = "8",
    val initialNote: String = "",
    val isCreating: Boolean = false,
    val createdId: String? = null
)

@HiltViewModel
class QueriesViewModel @Inject constructor(
    private val queryRepository: QueryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QueriesUiState())
    val uiState: StateFlow<QueriesUiState> = _uiState.asStateFlow()

    private val _detailState = MutableStateFlow(QueryDetailUiState())
    val detailState: StateFlow<QueryDetailUiState> = _detailState.asStateFlow()

    private val _newQueryState = MutableStateFlow(NewQueryUiState())
    val newQueryState: StateFlow<NewQueryUiState> = _newQueryState.asStateFlow()

    // Reactive query list based on filter
    val queries = _uiState.flatMapLatest { state ->
        when {
            state.searchQuery.isNotBlank() -> queryRepository.searchQueries(state.searchQuery)
            state.activeFilter != null -> queryRepository.observeQueriesByStatus(state.activeFilter)
            else -> queryRepository.observeAllQueries()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── List Actions ────────────────────────────────────────────

    fun setFilter(status: QueryStatus?) {
        _uiState.value = _uiState.value.copy(activeFilter = status)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun toggleSearch() {
        val current = _uiState.value
        _uiState.value = current.copy(
            isSearchActive = !current.isSearchActive,
            searchQuery = if (current.isSearchActive) "" else current.searchQuery
        )
    }

    // ── Detail ──────────────────────────────────────────────────

    fun loadQueryDetail(queryId: String) {
        _detailState.value = QueryDetailUiState(isLoading = true)
        viewModelScope.launch {
            queryRepository.observeQueryById(queryId).collect { query ->
                _detailState.value = _detailState.value.copy(query = query, isLoading = false)
            }
        }
        viewModelScope.launch {
            queryRepository.observeLogEntries(queryId).collect { logs ->
                _detailState.value = _detailState.value.copy(logs = logs)
            }
        }
    }

    fun updateNoteText(text: String) {
        _detailState.value = _detailState.value.copy(noteText = text)
    }

    fun addNote(queryId: String) {
        val note = _detailState.value.noteText.trim()
        if (note.isEmpty()) return
        viewModelScope.launch {
            queryRepository.addLogEntry(queryId, note)
            _detailState.value = _detailState.value.copy(noteText = "")
        }
    }

    fun markFollowUp(queryId: String) {
        viewModelScope.launch { queryRepository.markFollowUp(queryId) }
    }

    fun escalate(queryId: String) {
        viewModelScope.launch { queryRepository.escalate(queryId) }
    }

    fun closeQuery(queryId: String) {
        viewModelScope.launch { queryRepository.closeQuery(queryId) }
    }

    // Snooze offsets
    fun snooze1Hour(queryId: String) {
        viewModelScope.launch { queryRepository.snooze(queryId, 1 * 60 * 60 * 1000L) }
    }

    fun snooze4Hours(queryId: String) {
        viewModelScope.launch { queryRepository.snooze(queryId, 4 * 60 * 60 * 1000L) }
    }

    fun snoozeTomorrow9am(queryId: String) {
        viewModelScope.launch {
            val tomorrow = java.time.LocalDate.now(java.time.ZoneId.of("Africa/Johannesburg")).plusDays(1)
            val at9 = java.time.ZonedDateTime.of(
                tomorrow, java.time.LocalTime.of(9, 0),
                java.time.ZoneId.of("Africa/Johannesburg")
            ).toInstant().toEpochMilli()
            queryRepository.snoozeUntil(queryId, at9)
        }
    }

    // ── New Query ───────────────────────────────────────────────

    fun updateNewQuery(
        ticketNumber: String? = null,
        customerId: String? = null,
        customerName: String? = null,
        urgency: QueryUrgency? = null,
        customFollowUpHours: String? = null,
        initialNote: String? = null
    ) {
        val current = _newQueryState.value
        _newQueryState.value = current.copy(
            ticketNumber = ticketNumber ?: current.ticketNumber,
            customerId = customerId ?: current.customerId,
            customerName = customerName ?: current.customerName,
            urgency = urgency ?: current.urgency,
            customFollowUpHours = customFollowUpHours ?: current.customFollowUpHours,
            initialNote = initialNote ?: current.initialNote
        )
    }

    fun createQuery() {
        val state = _newQueryState.value
        if (state.customerName.isBlank()) return
        _newQueryState.value = state.copy(isCreating = true)
        viewModelScope.launch {
            val customHours = if (state.urgency == QueryUrgency.CUSTOM) {
                state.customFollowUpHours.toIntOrNull()?.coerceAtLeast(1)
            } else null

            val id = queryRepository.createQuery(
                ticketNumber = state.ticketNumber,
                customerId = state.customerId,
                customerName = state.customerName,
                urgency = state.urgency,
                customFollowUpHours = customHours
            )
            if (state.initialNote.isNotBlank()) {
                queryRepository.addLogEntry(id, state.initialNote)
            }
            _newQueryState.value = _newQueryState.value.copy(
                isCreating = false,
                createdId = id
            )
        }
    }

    fun resetNewQuery() {
        _newQueryState.value = NewQueryUiState()
    }

    fun deleteQuery(queryId: String) {
        viewModelScope.launch { queryRepository.deleteQuery(queryId) }
    }
}
