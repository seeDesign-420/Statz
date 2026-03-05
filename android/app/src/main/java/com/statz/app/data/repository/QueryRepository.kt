package com.statz.app.data.repository

import com.statz.app.data.local.dao.QueryDao
import com.statz.app.data.local.model.QueryItemEntity
import com.statz.app.data.local.model.QueryLogEntryEntity
import com.statz.app.domain.model.QueryStatus
import com.statz.app.domain.model.QueryUrgency
import com.statz.app.domain.model.WorkHoursUtils
import com.statz.app.notification.NotificationScheduler
import kotlinx.coroutines.flow.Flow
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QueryRepository @Inject constructor(
    private val queryDao: QueryDao,
    private val notificationScheduler: NotificationScheduler
) {

    fun observeAllQueries(): Flow<List<QueryItemEntity>> =
        queryDao.observeAllQueries()

    fun observeQueriesByStatus(status: QueryStatus): Flow<List<QueryItemEntity>> =
        queryDao.observeQueriesByStatus(status)

    fun searchQueries(search: String): Flow<List<QueryItemEntity>> =
        queryDao.searchQueries(search)

    fun observeQueryById(id: String): Flow<QueryItemEntity?> =
        queryDao.observeQueryById(id)

    fun observeLogEntries(queryId: String): Flow<List<QueryLogEntryEntity>> =
        queryDao.observeLogEntries(queryId)

    // ── Create ──────────────────────────────────────────────────

    /**
     * Create a new query with initial follow-up set by urgency rules.
     * Schedules an exact alarm for the follow-up notification.
     */
    suspend fun createQuery(
        ticketNumber: String,
        customerId: String,
        customerName: String,
        urgency: QueryUrgency,
        customFollowUpHours: Int? = null,
        workStart: LocalTime = WorkHoursUtils.DEFAULT_WORK_START,
        workEnd: LocalTime = WorkHoursUtils.DEFAULT_WORK_END,
        weekendsEnabled: Boolean = false
    ): String {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        val nextFollowUp = WorkHoursUtils.calculateNextFollowUp(
            fromMillis = now,
            urgency = urgency,
            customHours = customFollowUpHours,
            workStart = workStart,
            workEnd = workEnd,
            weekendsEnabled = weekendsEnabled
        )

        queryDao.insertQuery(
            QueryItemEntity(
                id = id,
                ticketNumber = ticketNumber,
                customerId = customerId,
                customerName = customerName,
                status = QueryStatus.OPEN,
                urgency = urgency,
                customFollowUpHours = customFollowUpHours,
                nextFollowUpAt = nextFollowUp,
                createdAt = now,
                updatedAt = now
            )
        )

        // Schedule precise alarm for follow-up notification
        notificationScheduler.scheduleQueryAlarm(id, nextFollowUp, urgency)

        // Log initial creation
        addLogEntry(id, "Query created", QueryStatus.OPEN)

        return id
    }

    // ── Status Transitions ──────────────────────────────────────

    suspend fun markFollowUp(queryId: String) {
        changeStatus(queryId, QueryStatus.FOLLOW_UP, "Marked for follow-up")
    }

    suspend fun escalate(queryId: String) {
        changeStatus(queryId, QueryStatus.ESCALATED, "Escalated")
    }

    suspend fun closeQuery(queryId: String) {
        val now = System.currentTimeMillis()
        val query = queryDao.getQueryById(queryId) ?: return
        queryDao.updateQuery(
            query.copy(
                status = QueryStatus.CLOSED,
                closedAt = now,
                updatedAt = now
            )
        )
        // Cancel the alarm — no more follow-ups needed
        notificationScheduler.cancelQueryAlarm(queryId)
        addLogEntry(queryId, "Query closed", QueryStatus.CLOSED)
    }

    suspend fun reopenQuery(queryId: String, urgency: QueryUrgency? = null) {
        val now = System.currentTimeMillis()
        val query = queryDao.getQueryById(queryId) ?: return
        val effectiveUrgency = urgency ?: query.urgency
        val nextFollowUp = WorkHoursUtils.calculateNextFollowUp(
            now,
            effectiveUrgency,
            customHours = query.customFollowUpHours
        )

        queryDao.updateQuery(
            query.copy(
                status = QueryStatus.OPEN,
                urgency = effectiveUrgency,
                nextFollowUpAt = nextFollowUp,
                closedAt = null,
                updatedAt = now
            )
        )
        // Reschedule alarm for the new follow-up time
        notificationScheduler.scheduleQueryAlarm(queryId, nextFollowUp, effectiveUrgency)
        addLogEntry(queryId, "Query reopened", QueryStatus.OPEN)
    }

    private suspend fun changeStatus(queryId: String, newStatus: QueryStatus, note: String) {
        val now = System.currentTimeMillis()
        val query = queryDao.getQueryById(queryId) ?: return
        queryDao.updateQuery(query.copy(status = newStatus, updatedAt = now))
        addLogEntry(queryId, note, newStatus)
    }

    // ── Snooze ──────────────────────────────────────────────────

    /**
     * Snooze: push nextFollowUpAt by the given millis offset.
     */
    suspend fun snooze(queryId: String, offsetMillis: Long) {
        val now = System.currentTimeMillis()
        val query = queryDao.getQueryById(queryId) ?: return
        val newFollowUp = now + offsetMillis
        queryDao.updateQuery(query.copy(nextFollowUpAt = newFollowUp, updatedAt = now))
        // Reschedule alarm for the snoozed time
        notificationScheduler.scheduleQueryAlarm(queryId, newFollowUp, query.urgency)
        addLogEntry(queryId, "Snoozed follow-up")
    }

    /**
     * Snooze to a specific absolute time (e.g. tomorrow 09:00).
     */
    suspend fun snoozeUntil(queryId: String, untilMillis: Long) {
        val now = System.currentTimeMillis()
        val query = queryDao.getQueryById(queryId) ?: return
        queryDao.updateQuery(query.copy(nextFollowUpAt = untilMillis, updatedAt = now))
        // Reschedule alarm for the snoozed time
        notificationScheduler.scheduleQueryAlarm(queryId, untilMillis, query.urgency)
        addLogEntry(queryId, "Snoozed follow-up")
    }

    // ── Edits ───────────────────────────────────────────────────

    suspend fun updateQueryFields(
        queryId: String,
        ticketNumber: String? = null,
        customerId: String? = null,
        customerName: String? = null,
        urgency: QueryUrgency? = null,
        customFollowUpHours: Int? = null
    ) {
        val now = System.currentTimeMillis()
        val query = queryDao.getQueryById(queryId) ?: return

        val effectiveUrgency = urgency ?: query.urgency
        val effectiveCustomHours = if (urgency == QueryUrgency.CUSTOM) customFollowUpHours else query.customFollowUpHours
        val needsReschedule = urgency != null && urgency != query.urgency

        val updatedQuery = query.copy(
            ticketNumber = ticketNumber ?: query.ticketNumber,
            customerId = customerId ?: query.customerId,
            customerName = customerName ?: query.customerName,
            urgency = effectiveUrgency,
            customFollowUpHours = effectiveCustomHours,
            updatedAt = now
        )

        if (needsReschedule && query.status != QueryStatus.CLOSED) {
            val nextFollowUp = WorkHoursUtils.calculateNextFollowUp(
                now,
                effectiveUrgency,
                customHours = effectiveCustomHours
            )
            queryDao.updateQuery(updatedQuery.copy(nextFollowUpAt = nextFollowUp))
            notificationScheduler.scheduleQueryAlarm(queryId, nextFollowUp, effectiveUrgency)
        } else {
            queryDao.updateQuery(updatedQuery)
        }
    }

    // ── Log ─────────────────────────────────────────────────────

    suspend fun addLogEntry(queryId: String, note: String, statusAfter: QueryStatus? = null) {
        queryDao.insertLogEntry(
            QueryLogEntryEntity(
                id = UUID.randomUUID().toString(),
                queryId = queryId,
                timestamp = System.currentTimeMillis(),
                note = note,
                statusAfter = statusAfter
            )
        )
    }

    // ── Reminder Support ────────────────────────────────────────

    /**
     * Get queries due for follow-up reminder (WorkManager fallback).
     */
    suspend fun getDueQueries(nowMillis: Long): List<QueryItemEntity> =
        queryDao.getDueQueries(nowMillis)

    /**
     * Advance a query's follow-up after a reminder fires.
     * Reschedules the alarm for the next follow-up time.
     */
    suspend fun advanceFollowUp(
        queryId: String,
        workStart: LocalTime = WorkHoursUtils.DEFAULT_WORK_START,
        workEnd: LocalTime = WorkHoursUtils.DEFAULT_WORK_END,
        weekendsEnabled: Boolean = false
    ) {
        val now = System.currentTimeMillis()
        val query = queryDao.getQueryById(queryId) ?: return
        val nextFollowUp = WorkHoursUtils.calculateNextFollowUp(
            fromMillis = now,
            urgency = query.urgency,
            customHours = query.customFollowUpHours,
            workStart = workStart,
            workEnd = workEnd,
            weekendsEnabled = weekendsEnabled
        )
        queryDao.updateQuery(query.copy(nextFollowUpAt = nextFollowUp, updatedAt = now))
        // Reschedule alarm
        notificationScheduler.scheduleQueryAlarm(queryId, nextFollowUp, query.urgency)
        addLogEntry(queryId, "Auto reminder sent")
    }

    suspend fun deleteQuery(id: String) {
        // Cancel alarm before deleting
        notificationScheduler.cancelQueryAlarm(id)
        queryDao.deleteQuery(id)
    }
}
