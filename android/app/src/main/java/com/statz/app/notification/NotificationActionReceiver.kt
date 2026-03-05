package com.statz.app.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.statz.app.data.local.AppDatabase
import com.statz.app.domain.model.QueryStatus
import com.statz.app.domain.model.QueryUrgency
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Handles notification action button taps (Snooze / Close)
 * without requiring the user to open the app.
 */
class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val queryId = intent?.getStringExtra(NotificationScheduler.EXTRA_QUERY_ID) ?: return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val query = db.queryDao().getQueryById(queryId) ?: return@launch
                val now = System.currentTimeMillis()

                val scheduler = NotificationScheduler(
                    context,
                    context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
                )

                when (intent.action) {
                    ACTION_SNOOZE -> {
                        // Snooze by 1 hour
                        val snoozeUntil = now + SNOOZE_DURATION_MS
                        db.queryDao().updateQuery(
                            query.copy(nextFollowUpAt = snoozeUntil, updatedAt = now)
                        )
                        scheduler.scheduleQueryAlarm(queryId, snoozeUntil, query.urgency)
                    }
                    ACTION_CLOSE -> {
                        // Close the query and cancel its alarm
                        db.queryDao().updateQuery(
                            query.copy(
                                status = QueryStatus.CLOSED,
                                closedAt = now,
                                updatedAt = now
                            )
                        )
                        scheduler.cancelQueryAlarm(queryId)
                    }
                }

                // Dismiss the notification
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(queryId.hashCode())

            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_SNOOZE = "com.statz.app.ACTION_SNOOZE_QUERY"
        const val ACTION_CLOSE = "com.statz.app.ACTION_CLOSE_QUERY"
        private const val SNOOZE_DURATION_MS = 60 * 60 * 1000L // 1 hour
    }
}
