package com.statz.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.statz.app.data.local.AppDatabase
import com.statz.app.domain.model.QueryStatus
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

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val scheduler = NotificationScheduler(
                    context,
                    context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
                )

                // Get all non-closed queries and reschedule their alarms
                val now = System.currentTimeMillis()
                val openQueries = db.queryDao().getAllNonClosedQueries()

                for (query in openQueries) {
                    val triggerAt = if (query.nextFollowUpAt > now) {
                        query.nextFollowUpAt
                    } else {
                        // Already overdue — trigger immediately
                        now + 1000L
                    }
                    scheduler.scheduleQueryAlarm(query.id, triggerAt, query.urgency)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
