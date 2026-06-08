package com.sip

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val app = context.applicationContext as App
        val viewModel = app.sipViewModel

        CoroutineScope(Dispatchers.IO).launch {
            /*
            ---------------------------------------------------
            LOAD SETTINGS DIRECTLY FROM VIEWMODEL FLOWS
            ---------------------------------------------------
            */
            val startTime = viewModel.startTime.first()
            val endTime = viewModel.endTime.first()

            /*
            ---------------------------------------------------
            CURRENT TIME ARITHMETIC
            ---------------------------------------------------
            */
            val now = Calendar.getInstance()
            val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

            val startParts = startTime.split(":")
            val startMinutes = startParts[0].toInt() * 60 + startParts[1].toInt()

            val endParts = endTime.split(":")
            val endMinutes = endParts[0].toInt() * 60 + endParts[1].toInt()

            /*
            ---------------------------------------------------
            CHECK BOUNDS ACCORDING TO USER SCHEDULES
            ---------------------------------------------------
            */
            val isWithinRange = if (startMinutes == endMinutes) {
                true
            } else if (startMinutes <= endMinutes) {
                currentMinutes in startMinutes..endMinutes
            } else {
                currentMinutes in startMinutes..1439 || currentMinutes in 0..endMinutes
            }

            if (isWithinRange) {
                NotificationHelper(context).showReminderNotification()
            }

            /*
            ---------------------------------------------------
            SCHEDULE ENGINE RE-TRIGGER
            ---------------------------------------------------
            */
            ReminderScheduler.scheduleNextReminder(context)
        }
    }
}