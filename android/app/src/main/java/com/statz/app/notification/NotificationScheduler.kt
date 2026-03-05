package com.statz.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.statz.app.domain.model.QueryUrgency
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules and cancels exact alarms for query follow-up notifications.
 *
 * Uses AlarmManager.setExactAndAllowWhileIdle() for precise delivery,
 * keyed by queryId so alarms are idempotently replaceable.
 */
@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmManager: AlarmManager
) {

    /**
     * Schedule (or reschedule) an exact alarm for a query follow-up.
     *
     * @param queryId The query's UUID — used as the PendingIntent request code.
     * @param triggerAtMillis The exact epoch millis when the notification should fire.
     * @param urgency The query urgency — passed to the receiver for channel selection.
     */
    fun scheduleQueryAlarm(queryId: String, triggerAtMillis: Long, urgency: QueryUrgency) {
        val pendingIntent = buildPendingIntent(queryId, urgency)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // API 31+: check if exact alarms are allowed
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                // Fall back to inexact alarm if exact permission not granted
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    /**
     * Cancel a previously scheduled alarm for a query.
     */
    fun cancelQueryAlarm(queryId: String) {
        val pendingIntent = buildPendingIntent(queryId, QueryUrgency.MEDIUM)
        alarmManager.cancel(pendingIntent)
    }

    private fun buildPendingIntent(queryId: String, urgency: QueryUrgency): PendingIntent {
        val intent = Intent(context, QueryAlarmReceiver::class.java).apply {
            action = ACTION_QUERY_ALARM
            putExtra(EXTRA_QUERY_ID, queryId)
            putExtra(EXTRA_URGENCY, urgency.name)
        }
        return PendingIntent.getBroadcast(
            context,
            queryId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val ACTION_QUERY_ALARM = "com.statz.app.ACTION_QUERY_ALARM"
        const val EXTRA_QUERY_ID = "extra_query_id"
        const val EXTRA_URGENCY = "extra_urgency"
    }
}
