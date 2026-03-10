package com.statz.app.notification

import com.statz.app.domain.model.QueryUrgency

/**
 * Abstraction over alarm scheduling for notifications.
 * Allows unit-testing without Android framework dependencies.
 */
interface AlarmScheduler {
    fun scheduleQueryAlarm(queryId: String, triggerAtMillis: Long, urgency: QueryUrgency)
    fun cancelQueryAlarm(queryId: String)
    fun scheduleTaskAlarm(taskId: String, triggerAtMillis: Long)
    fun cancelTaskAlarm(taskId: String)
}
