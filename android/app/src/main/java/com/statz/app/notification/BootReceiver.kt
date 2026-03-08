package com.statz.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.statz.app.di.DatabaseEntryPoint
import com.statz.app.domain.model.QueryStatus
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Reschedules all pending query alarms after device reboot.
 *
 * AlarmManager alarms are lost on reboot, so this receiver
 * queries the DB for all non-closed queries and re-registers them.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            DatabaseEntryPoint::class.java
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val queryDao = entryPoint.queryDao()
                val taskDao = entryPoint.taskDao()
                val scheduler = entryPoint.notificationScheduler()

                // Get all non-closed queries and reschedule their alarms
                val now = System.currentTimeMillis()
                val openQueries = queryDao.getAllNonClosedQueries()

                for (query in openQueries) {
                    val triggerAt = if (query.nextFollowUpAt > now) {
                        query.nextFollowUpAt
                    } else {
                        // Already overdue — trigger immediately
                        now + 1000L
                    }
                    scheduler.scheduleQueryAlarm(query.id, triggerAt, query.urgency)
                }

                // Get all tasks with reminders and reschedule their alarms
                val reminderTasks = taskDao.getReminderEnabledTasks()
                for (task in reminderTasks) {
                    val dueAt = task.dueAt ?: continue
                    val triggerAt = if (dueAt > now) dueAt else now + 1000L
                    scheduler.scheduleTaskAlarm(task.id, triggerAt)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
