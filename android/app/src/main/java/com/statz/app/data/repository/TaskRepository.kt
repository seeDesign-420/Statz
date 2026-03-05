package com.statz.app.data.repository

import com.statz.app.data.local.dao.TaskDao
import com.statz.app.data.local.model.TaskItemEntity
import com.statz.app.domain.model.QueryUrgency
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Grouped task sections for the Today View (spec §4.2.A).
 */
data class TaskSections(
    val overdue: List<TaskItemEntity>,
    val today: List<TaskItemEntity>,
    val upcoming: List<TaskItemEntity>,
    val unscheduled: List<TaskItemEntity>
)

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {

    private val timezone = ZoneId.of("Africa/Johannesburg")

    // ── CRUD ────────────────────────────────────────────────────

    suspend fun createTask(
        title: String,
        notes: String? = null,
        dueAt: Long? = null,
        urgency: QueryUrgency = QueryUrgency.MEDIUM,
        customFollowUpHours: Int? = null,
        reminderEnabled: Boolean = false
    ): String {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        taskDao.insertTask(
            TaskItemEntity(
                id = id,
                title = title,
                notes = notes,
                dueAt = dueAt,
                urgency = urgency,
                customFollowUpHours = if (urgency == QueryUrgency.CUSTOM) customFollowUpHours else null,
                nextFollowUpAt = null,
                isDone = false,
                reminderEnabled = reminderEnabled,
                createdAt = now,
                updatedAt = now
            )
        )
        return id
    }

    suspend fun updateTask(task: TaskItemEntity) {
        taskDao.updateTask(task.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun toggleDone(taskId: String) {
        val task = taskDao.getTaskById(taskId) ?: return
        taskDao.setDone(taskId, !task.isDone, System.currentTimeMillis())
    }

    suspend fun markDone(taskId: String) {
        taskDao.setDone(taskId, true, System.currentTimeMillis())
    }

    suspend fun deleteTask(taskId: String) {
        taskDao.deleteTask(taskId)
    }

    // ── Observe ─────────────────────────────────────────────────

    fun observeTaskById(id: String): Flow<TaskItemEntity?> =
        taskDao.observeTaskById(id)

    fun observeActiveTasks(): Flow<List<TaskItemEntity>> =
        taskDao.observeActiveTasks()

    fun observeCompletedTasks(): Flow<List<TaskItemEntity>> =
        taskDao.observeCompletedTasks()

    /**
     * Section-based queries for Today View.
     */
    fun observeOverdueTasks(): Flow<List<TaskItemEntity>> {
        val now = System.currentTimeMillis()
        return taskDao.observeOverdueTasks(now)
    }

    fun observeTodayTasks(): Flow<List<TaskItemEntity>> {
        val today = LocalDate.now(timezone)
        val startOfDay = ZonedDateTime.of(today, LocalTime.MIN, timezone).toInstant().toEpochMilli()
        val endOfDay = ZonedDateTime.of(today.plusDays(1), LocalTime.MIN, timezone).toInstant().toEpochMilli()
        return taskDao.observeTodayTasks(startOfDay, endOfDay)
    }

    fun observeUpcomingTasks(): Flow<List<TaskItemEntity>> {
        val today = LocalDate.now(timezone)
        val endOfDay = ZonedDateTime.of(today.plusDays(1), LocalTime.MIN, timezone).toInstant().toEpochMilli()
        return taskDao.observeUpcomingTasks(endOfDay)
    }

    fun observeUnscheduledTasks(): Flow<List<TaskItemEntity>> =
        taskDao.observeUnscheduledTasks()

    // ── Reminder Support ────────────────────────────────────────

    suspend fun getDueTasks(nowMillis: Long): List<TaskItemEntity> =
        taskDao.getDueTasks(nowMillis)
}
