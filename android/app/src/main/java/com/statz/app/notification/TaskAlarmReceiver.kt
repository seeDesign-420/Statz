package com.statz.app.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.statz.app.R
import com.statz.app.MainActivity
import com.statz.app.di.DatabaseEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver triggered by AlarmManager at the exact dueAt time for tasks.
 *
 * Sends a notification on the CHANNEL_TASKS channel.
 * Uses goAsync() for coroutine-safe DB access within the 10-second window.
 */
class TaskAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ACTION_TASK_ALARM) return

        val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return
        val pendingResult = goAsync()
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            DatabaseEntryPoint::class.java
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val taskDao = entryPoint.taskDao()
                val task = taskDao.getTaskById(taskId)

                // Only notify if task exists, is not done, and has reminder enabled
                if (task == null || task.isDone || !task.reminderEnabled) {
                    pendingResult.finish()
                    return@launch
                }

                sendNotification(context, taskId, task.title)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun sendNotification(
        context: Context,
        taskId: String,
        title: String
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Tap to open app
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("navigate_to_task", taskId)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            "task_$taskId".hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_TASKS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("📋 Task Due")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        notificationManager.notify("task_$taskId".hashCode(), notification)
    }

    companion object {
        const val ACTION_TASK_ALARM = "com.statz.app.ACTION_TASK_ALARM"
        const val EXTRA_TASK_ID = "extra_task_id"
    }
}
