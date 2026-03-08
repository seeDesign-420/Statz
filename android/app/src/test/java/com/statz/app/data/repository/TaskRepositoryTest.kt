package com.statz.app.data.repository

import com.statz.app.data.local.dao.TaskDao
import com.statz.app.data.local.model.TaskItemEntity
import com.statz.app.domain.model.QueryUrgency
import com.statz.app.notification.NotificationScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for TaskRepository business logic.
 */
class TaskRepositoryTest {

    private lateinit var fakeDao: FakeTaskDao
    private lateinit var fakeScheduler: FakeNotificationScheduler
    private lateinit var repository: TaskRepository

    @Before
    fun setUp() {
        fakeDao = FakeTaskDao()
        fakeScheduler = FakeNotificationScheduler()
        repository = TaskRepository(fakeDao, fakeScheduler)
    }

    @Test
    fun `createTask inserts task and returns id`() = runTest {
        val id = repository.createTask(title = "Buy groceries")

        assertNotNull(id)
        val task = fakeDao.tasks[id]
        assertNotNull(task)
        assertEquals("Buy groceries", task?.title)
        assertFalse(task!!.isDone)
    }

    @Test
    fun `toggleDone flips isDone state`() = runTest {
        val id = repository.createTask(title = "Test task")
        val task = fakeDao.tasks[id]!!
        assertFalse(task.isDone)

        repository.toggleDone(id)

        val toggled = fakeDao.tasks[id]!!
        assertTrue(toggled.isDone)
    }

    @Test
    fun `deleteTask removes from DAO and cancels alarm`() = runTest {
        val id = repository.createTask(title = "Delete me")
        assertTrue(fakeDao.tasks.containsKey(id))

        repository.deleteTask(id)

        assertFalse(fakeDao.tasks.containsKey(id))
        assertTrue(fakeScheduler.cancelledTaskIds.contains(id))
    }

    @Test
    fun `createTask with due date schedules alarm when reminder enabled`() = runTest {
        val dueAt = System.currentTimeMillis() + 3600_000L // 1 hour from now
        val id = repository.createTask(
            title = "Reminder task",
            dueAt = dueAt,
            reminderEnabled = true
        )

        assertTrue(fakeScheduler.scheduledTaskIds.contains(id))
    }

    @Test
    fun `createTask without reminder does not schedule alarm`() = runTest {
        val id = repository.createTask(
            title = "No reminder",
            dueAt = System.currentTimeMillis() + 3600_000L,
            reminderEnabled = false
        )

        assertFalse(fakeScheduler.scheduledTaskIds.contains(id))
    }
}

/**
 * Fake TaskDao for unit testing.
 */
private class FakeTaskDao : TaskDao {
    val tasks = mutableMapOf<String, TaskItemEntity>()

    override suspend fun insertTask(task: TaskItemEntity) {
        tasks[task.id] = task
    }

    override suspend fun updateTask(task: TaskItemEntity) {
        tasks[task.id] = task
    }

    override suspend fun deleteTask(id: String) {
        tasks.remove(id)
    }

    override suspend fun getTaskById(id: String): TaskItemEntity? = tasks[id]

    override fun observeTaskById(id: String): Flow<TaskItemEntity?> =
        flowOf(tasks[id])

    override fun observeActiveTasks(): Flow<List<TaskItemEntity>> =
        flowOf(tasks.values.filter { !it.isDone })

    override fun observeCompletedTasks(): Flow<List<TaskItemEntity>> =
        flowOf(tasks.values.filter { it.isDone })

    override fun observeOverdueTasks(now: Long): Flow<List<TaskItemEntity>> =
        flowOf(emptyList())

    override fun observeTodayTasks(startOfDay: Long, endOfDay: Long): Flow<List<TaskItemEntity>> =
        flowOf(emptyList())

    override fun observeUpcomingTasks(afterEndOfDay: Long): Flow<List<TaskItemEntity>> =
        flowOf(emptyList())

    override fun observeUnscheduledTasks(): Flow<List<TaskItemEntity>> =
        flowOf(tasks.values.filter { it.dueAt == null && !it.isDone })

    override suspend fun getReminderEnabledTasks(): List<TaskItemEntity> =
        tasks.values.filter { it.reminderEnabled }

    override suspend fun getDueTasks(nowMillis: Long): List<TaskItemEntity> =
        tasks.values.filter { it.dueAt != null && it.dueAt <= nowMillis && !it.isDone }
}

/**
 * Fake NotificationScheduler for unit testing.
 */
private class FakeNotificationScheduler : NotificationScheduler(
    context = throw UnsupportedOperationException("Fake — do not use context"),
    alarmManager = throw UnsupportedOperationException("Fake — do not use alarmManager")
) {
    val scheduledTaskIds = mutableSetOf<String>()
    val cancelledTaskIds = mutableSetOf<String>()
    val scheduledQueryIds = mutableSetOf<String>()
    val cancelledQueryIds = mutableSetOf<String>()

    override fun scheduleTaskAlarm(taskId: String, triggerAtMillis: Long) {
        scheduledTaskIds.add(taskId)
    }

    override fun cancelTaskAlarm(taskId: String) {
        cancelledTaskIds.add(taskId)
    }

    override fun scheduleQueryAlarm(queryId: String, triggerAtMillis: Long, urgency: QueryUrgency) {
        scheduledQueryIds.add(queryId)
    }

    override fun cancelQueryAlarm(queryId: String) {
        cancelledQueryIds.add(queryId)
    }
}
