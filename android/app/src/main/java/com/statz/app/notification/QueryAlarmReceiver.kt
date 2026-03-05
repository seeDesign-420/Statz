package com.statz.app.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.statz.app.MainActivity
import com.statz.app.data.local.AppDatabase
import com.statz.app.domain.model.QueryStatus
import com.statz.app.domain.model.QueryUrgency
import com.statz.app.domain.model.WorkHoursUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver triggered by AlarmManager at the exact nextFollowUpAt time.
 *
 * Sends an urgency-aware notification and auto-advances the follow-up schedule.
 * Uses goAsync() for coroutine-safe DB access within the 10-second window.
 */
class QueryAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != NotificationScheduler.ACTION_QUERY_ALARM) return

        val queryId = intent.getStringExtra(NotificationScheduler.EXTRA_QUERY_ID) ?: return
        val urgencyName = intent.getStringExtra(NotificationScheduler.EXTRA_URGENCY) ?: return
        val urgency = try {
            QueryUrgency.valueOf(urgencyName)
        } catch (_: Exception) {
            QueryUrgency.MEDIUM
        }

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val query = db.queryDao().getQueryById(queryId)

                // Only notify if query is still open/follow-up/escalated
                if (query == null || query.status == QueryStatus.CLOSED) {
                    pendingResult.finish()
                    return@launch
                }

                // Send the notification
                sendNotification(context, queryId, query.ticketNumber, query.customerName, urgency)

                // Auto-advance: calculate and set the next follow-up
                val now = System.currentTimeMillis()
                val nextFollowUp = WorkHoursUtils.calculateNextFollowUp(
                    fromMillis = now,
                    urgency = query.urgency,
                    customHours = query.customFollowUpHours
                )
                db.queryDao().updateQuery(
                    query.copy(nextFollowUpAt = nextFollowUp, updatedAt = now)
                )

                // Reschedule alarm for the next follow-up
                val scheduler = NotificationScheduler(
                    context,
                    context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
                )
                scheduler.scheduleQueryAlarm(queryId, nextFollowUp, query.urgency)

            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun sendNotification(
        context: Context,
        queryId: String,
        ticketNumber: String,
        customerName: String,
        urgency: QueryUrgency
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = NotificationChannels.channelForUrgency(urgency)
        val priority = when (urgency) {
            QueryUrgency.CRITICAL, QueryUrgency.HIGH -> NotificationCompat.PRIORITY_HIGH
            QueryUrgency.MEDIUM, QueryUrgency.CUSTOM -> NotificationCompat.PRIORITY_DEFAULT
            QueryUrgency.LOW -> NotificationCompat.PRIORITY_LOW
        }

        // Tap to open app
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("navigate_to_query", queryId)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            queryId.hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze action
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SNOOZE
            putExtra(NotificationScheduler.EXTRA_QUERY_ID, queryId)
            putExtra(NotificationScheduler.EXTRA_URGENCY, urgency.name)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            "snooze_$queryId".hashCode(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Close action
        val closeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_CLOSE
            putExtra(NotificationScheduler.EXTRA_QUERY_ID, queryId)
        }
        val closePendingIntent = PendingIntent.getBroadcast(
            context,
            "close_$queryId".hashCode(),
            closeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val urgencyLabel = when (urgency) {
            QueryUrgency.CRITICAL -> "🔴 CRITICAL"
            QueryUrgency.HIGH -> "🟠 HIGH"
            QueryUrgency.MEDIUM -> "🟡 MEDIUM"
            QueryUrgency.LOW -> "🟢 LOW"
            QueryUrgency.CUSTOM -> "🟣 CUSTOM"
        }

        val displayTicket = ticketNumber.ifEmpty { "#${queryId.take(8)}" }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("$urgencyLabel — Follow-up Due")
            .setContentText("$displayTicket · $customerName")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$displayTicket · $customerName\nThis query requires your attention.")
            )
            .setPriority(priority)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .addAction(0, "Snooze 1h", snoozePendingIntent)
            .addAction(0, "Mark Closed", closePendingIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        notificationManager.notify(queryId.hashCode(), notification)
    }
}
