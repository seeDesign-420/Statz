package com.statz.app.worker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.statz.app.R
import com.statz.app.data.repository.QueryRepository
import com.statz.app.data.repository.TaskRepository
import com.statz.app.data.settings.SettingsDataStore
import com.statz.app.domain.model.QueryUrgency
import com.statz.app.notification.NotificationChannels
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Periodic worker that acts as a safety-net fallback for notifications.
 *
 * Primary notification delivery is handled by AlarmManager via QueryAlarmReceiver.
 * This worker runs every 15 minutes to catch any queries that may have missed
 * their alarm (e.g., after app update or unusual conditions).
 *
 * Also handles task due reminders (which don't use AlarmManager).
 */
@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val queryRepository: QueryRepository,
    private val taskRepository: TaskRepository,
    private val settingsDataStore: SettingsDataStore
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val settings = settingsDataStore.settings.first()
        val now = System.currentTimeMillis()

        // ── Query follow-up fallback ────────────────────────────
        // AlarmManager handles precise delivery; this catches stragglers
        if (settings.queryRemindersEnabled) {
            val dueQueries = queryRepository.getDueQueries(now)
            for (query in dueQueries) {
                val channelId = NotificationChannels.channelForUrgency(query.urgency)
                val priority = when (query.urgency) {
                    QueryUrgency.CRITICAL, QueryUrgency.HIGH -> NotificationCompat.PRIORITY_HIGH
                    QueryUrgency.MEDIUM, QueryUrgency.CUSTOM -> NotificationCompat.PRIORITY_DEFAULT
                    QueryUrgency.LOW -> NotificationCompat.PRIORITY_LOW
                }

                sendNotification(
                    title = "Query Follow-up Due",
                    body = "${query.ticketNumber.ifEmpty { "#${query.id.take(8)}" }} — ${query.customerName}",
                    notificationId = query.id.hashCode(),
                    channelId = channelId,
                    priority = priority
                )
                // Auto-advance follow-up (also reschedules the alarm)
                queryRepository.advanceFollowUp(
                    queryId = query.id,
                    workStart = settings.workStartTime,
                    workEnd = settings.workEndTime,
                    weekendsEnabled = settings.weekendsEnabled
                )
            }
        }

        // ── Task due reminders ──────────────────────────────────
        if (settings.todoRemindersEnabled) {
            val dueTasks = taskRepository.getDueTasks(now)
            for (task in dueTasks) {
                sendNotification(
                    title = "Task Due",
                    body = task.title,
                    notificationId = task.id.hashCode(),
                    channelId = NotificationChannels.CHANNEL_TASKS,
                    priority = NotificationCompat.PRIORITY_DEFAULT
                )
            }
        }

        return Result.success()
    }

    private fun sendNotification(
        title: String,
        body: String,
        notificationId: Int,
        channelId: String,
        priority: Int = NotificationCompat.PRIORITY_DEFAULT
    ) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Deep link intent
        val intent = applicationContext.packageManager
            .getLaunchIntentForPackage(applicationContext.packageName)
            ?.apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    companion object {
        const val WORK_NAME = "statz_reminder_worker"

        /**
         * Enqueue the periodic reminder worker (15 min interval).
         */
        fun enqueue(context: Context) {
            val request = PeriodicWorkRequestBuilder<ReminderWorker>(
                15, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
