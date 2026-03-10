package com.statz.app.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.statz.app.di.DatabaseEntryPoint
import com.statz.app.domain.model.QueryStatus
import com.statz.app.domain.model.QueryUrgency
import dagger.hilt.android.EntryPointAccessors
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
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            DatabaseEntryPoint::class.java
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val queryDao = entryPoint.queryDao()
                val query = queryDao.getQueryById(queryId) ?: return@launch
                val now = System.currentTimeMillis()

                val scheduler = entryPoint.alarmScheduler()

                when (intent.action) {
                    ACTION_SNOOZE -> {
                        // Snooze by 1 hour
                        val snoozeUntil = now + SNOOZE_DURATION_MS
                        queryDao.updateQuery(
                            query.copy(nextFollowUpAt = snoozeUntil, updatedAt = now)
                        )
                        scheduler.scheduleQueryAlarm(queryId, snoozeUntil, query.urgency)
                    }
                    ACTION_CLOSE -> {
                        // Close the query and cancel its alarm
                        queryDao.updateQuery(
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
