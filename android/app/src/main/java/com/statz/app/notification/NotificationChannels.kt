package com.statz.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.statz.app.domain.model.QueryUrgency

/**
 * Urgency-aware notification channel definitions.
 *
 * Each urgency level gets its own channel so users can independently
 * configure notification behavior per urgency in system settings.
 */
object NotificationChannels {

    const val CHANNEL_QUERY_CRITICAL = "statz_query_critical"
    const val CHANNEL_QUERY_HIGH = "statz_query_high"
    const val CHANNEL_QUERY_MEDIUM = "statz_query_medium"
    const val CHANNEL_QUERY_LOW = "statz_query_low"
    const val CHANNEL_QUERY_CUSTOM = "statz_query_custom"
    const val CHANNEL_TASKS = "statz_tasks"

    /**
     * Map urgency level to the appropriate channel ID.
     */
    fun channelForUrgency(urgency: QueryUrgency): String = when (urgency) {
        QueryUrgency.CRITICAL -> CHANNEL_QUERY_CRITICAL
        QueryUrgency.HIGH -> CHANNEL_QUERY_HIGH
        QueryUrgency.MEDIUM -> CHANNEL_QUERY_MEDIUM
        QueryUrgency.LOW -> CHANNEL_QUERY_LOW
        QueryUrgency.CUSTOM -> CHANNEL_QUERY_CUSTOM
    }

    /**
     * Create all notification channels. Safe to call multiple times
     * (Android ignores re-creation of existing channels).
     */
    fun createAll(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channels = listOf(
            NotificationChannel(
                CHANNEL_QUERY_CRITICAL,
                "Critical Query Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent follow-up reminders for critical customer queries (6h interval)"
                enableVibration(true)
                setBypassDnd(true)
            },
            NotificationChannel(
                CHANNEL_QUERY_HIGH,
                "High Priority Query Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Follow-up reminders for high priority queries (12h interval)"
                enableVibration(true)
            },
            NotificationChannel(
                CHANNEL_QUERY_MEDIUM,
                "Medium Priority Query Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Follow-up reminders for medium priority queries (24h interval)"
            },
            NotificationChannel(
                CHANNEL_QUERY_LOW,
                "Low Priority Query Reminders",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Follow-up reminders for low priority queries (48h interval)"
            },
            NotificationChannel(
                CHANNEL_QUERY_CUSTOM,
                "Custom Query Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Follow-up reminders for queries with custom intervals"
            },
            NotificationChannel(
                CHANNEL_TASKS,
                "Task Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for due to-do tasks"
            }
        )

        manager.createNotificationChannels(channels)
    }
}
