package com.sip

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object ReminderScheduler {

    private const val REQUEST_CODE = 1001

    fun startReminders(
        context: Context,
        intervalMinutes: Long
    ) {
        scheduleNextReminder(context)
    }

    fun scheduleNextReminder(
        context: Context
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            // Read directly from our unified engine instance
            val app = context.applicationContext as App
            val viewModel = app.sipViewModel

            val intervalMinutes = viewModel.intervalMinutes.first()
            val now = System.currentTimeMillis()
            val intervalMillis = intervalMinutes * 60 * 1000
            val triggerAtMillis = now + intervalMillis

            val alarmManager = context.getSystemService(AlarmManager::class.java)
            val intent = Intent(context, ReminderReceiver::class.java)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    return@launch
                }
            }

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun stopReminders(
        context: Context
    ) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val intent = Intent(context, ReminderReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }
}