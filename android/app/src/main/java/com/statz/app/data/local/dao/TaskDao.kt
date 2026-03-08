package com.statz.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.statz.app.data.local.model.TaskItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert
    suspend fun insertTask(task: TaskItemEntity)

    @Update
    suspend fun updateTask(task: TaskItemEntity)

    @Upsert
    suspend fun upsertTask(task: TaskItemEntity)

    @Query("SELECT * FROM task_items WHERE id = :id")
    suspend fun getTaskById(id: String): TaskItemEntity?

    @Query("SELECT * FROM task_items WHERE id = :id")
    fun observeTaskById(id: String): Flow<TaskItemEntity?>

    /**
     * All active (not done) tasks, ordered by urgency desc then dueAt asc.
     */
    @Query("""
        SELECT * FROM task_items
        WHERE is_done = 0
        ORDER BY
            CASE urgency
                WHEN 'CRITICAL' THEN 0
                WHEN 'HIGH' THEN 1
                WHEN 'MEDIUM' THEN 2
                WHEN 'LOW' THEN 3
                WHEN 'CUSTOM' THEN 4
            END ASC,
            due_at ASC
    """)
    fun observeActiveTasks(): Flow<List<TaskItemEntity>>

    /**
     * Overdue tasks: not done, dueAt < now.
     */
    @Query("""
        SELECT * FROM task_items
        WHERE is_done = 0 AND due_at IS NOT NULL AND due_at < :nowMillis
        ORDER BY due_at ASC
    """)
    fun observeOverdueTasks(nowMillis: Long): Flow<List<TaskItemEntity>>

    /**
     * Tasks due today (between startOfDay and endOfDay).
     */
    @Query("""
        SELECT * FROM task_items
        WHERE is_done = 0
          AND due_at IS NOT NULL
          AND due_at >= :startOfDayMillis
          AND due_at < :endOfDayMillis
        ORDER BY due_at ASC
    """)
    fun observeTodayTasks(startOfDayMillis: Long, endOfDayMillis: Long): Flow<List<TaskItemEntity>>

    /**
     * Upcoming tasks: not done, dueAt >= endOfDay (tomorrow onward).
     */
    @Query("""
        SELECT * FROM task_items
        WHERE is_done = 0
          AND due_at IS NOT NULL
          AND due_at >= :endOfDayMillis
        ORDER BY due_at ASC
    """)
    fun observeUpcomingTasks(endOfDayMillis: Long): Flow<List<TaskItemEntity>>

    /**
     * Tasks with no due date (unscheduled), not done.
     */
    @Query("""
        SELECT * FROM task_items
        WHERE is_done = 0 AND due_at IS NULL
        ORDER BY created_at DESC
    """)
    fun observeUnscheduledTasks(): Flow<List<TaskItemEntity>>

    /**
     * Completed tasks.
     */
    @Query("""
        SELECT * FROM task_items
        WHERE is_done = 1
        ORDER BY updated_at DESC
    """)
    fun observeCompletedTasks(): Flow<List<TaskItemEntity>>

    /**
     * Find tasks due for reminder (for WorkManager).
     */
    @Query("""
        SELECT * FROM task_items
        WHERE is_done = 0
          AND reminder_enabled = 1
          AND due_at IS NOT NULL
          AND due_at <= :nowMillis
    """)
    suspend fun getDueTasks(nowMillis: Long): List<TaskItemEntity>

    @Query("DELETE FROM task_items WHERE id = :id")
    suspend fun deleteTask(id: String)

    @Query("UPDATE task_items SET is_done = :isDone, updated_at = :updatedAt WHERE id = :id")
    suspend fun setDone(id: String, isDone: Boolean, updatedAt: Long)

    /**
     * All tasks with reminders enabled and a due date set (for boot rescheduling).
     */
    @Query("""
        SELECT * FROM task_items
        WHERE is_done = 0
          AND reminder_enabled = 1
          AND due_at IS NOT NULL
    """)
    suspend fun getReminderEnabledTasks(): List<TaskItemEntity>
}
