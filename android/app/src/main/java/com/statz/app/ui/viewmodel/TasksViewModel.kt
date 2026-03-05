package com.statz.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statz.app.data.local.model.TaskItemEntity
import com.statz.app.data.repository.TaskRepository
import com.statz.app.domain.model.QueryUrgency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskDetailUiState(
    val task: TaskItemEntity? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _quickAddText = MutableStateFlow("")
    val quickAddText: StateFlow<String> = _quickAddText.asStateFlow()

    private val _detailState = MutableStateFlow(TaskDetailUiState())
    val detailState: StateFlow<TaskDetailUiState> = _detailState.asStateFlow()

    // Section-based reactive task lists for Today View
    val overdueTasks = taskRepository.observeOverdueTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayTasks = taskRepository.observeTodayTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingTasks = taskRepository.observeUpcomingTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unscheduledTasks = taskRepository.observeUnscheduledTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedTasks = taskRepository.observeCompletedTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Quick Add ───────────────────────────────────────────────

    fun updateQuickAddText(text: String) {
        _quickAddText.value = text
    }

    fun quickAdd() {
        val title = _quickAddText.value.trim()
        if (title.isEmpty()) return
        viewModelScope.launch {
            taskRepository.createTask(title = title)
            _quickAddText.value = ""
        }
    }

    // ── Task Actions ────────────────────────────────────────────

    fun toggleDone(taskId: String) {
        viewModelScope.launch { taskRepository.toggleDone(taskId) }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch { taskRepository.deleteTask(taskId) }
    }

    // ── Detail ──────────────────────────────────────────────────

    fun loadTaskDetail(taskId: String) {
        _detailState.value = TaskDetailUiState(isLoading = true)
        viewModelScope.launch {
            taskRepository.observeTaskById(taskId).collect { task ->
                _detailState.value = TaskDetailUiState(task = task, isLoading = false)
            }
        }
    }

    fun updateTask(task: TaskItemEntity) {
        viewModelScope.launch { taskRepository.updateTask(task) }
    }

    fun updateTaskFields(
        taskId: String,
        title: String? = null,
        notes: String? = null,
        dueAt: Long? = null,
        clearDueAt: Boolean = false,
        urgency: QueryUrgency? = null,
        customFollowUpHours: Int? = null,
        reminderEnabled: Boolean? = null
    ) {
        viewModelScope.launch {
            val current = _detailState.value.task ?: return@launch
            val newUrgency = urgency ?: current.urgency
            val updated = current.copy(
                title = title ?: current.title,
                notes = notes ?: current.notes,
                dueAt = if (clearDueAt) null else (dueAt ?: current.dueAt),
                urgency = newUrgency,
                customFollowUpHours = if (newUrgency == QueryUrgency.CUSTOM)
                    (customFollowUpHours ?: current.customFollowUpHours)
                else null,
                reminderEnabled = reminderEnabled ?: current.reminderEnabled
            )
            taskRepository.updateTask(updated)
        }
    }

    fun markDone(taskId: String) {
        viewModelScope.launch { taskRepository.markDone(taskId) }
    }
}
