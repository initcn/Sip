package com.sip

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {

            val app = context.applicationContext as App
            val viewModel = app.sipViewModel

            CoroutineScope(Dispatchers.IO).launch {
                val remindersEnabled = viewModel.remindersEnabled.first()

                if (remindersEnabled) {
                    val intervalMinutes = viewModel.intervalMinutes.first()
                    ReminderScheduler.startReminders(context, intervalMinutes)
                }
            }
        }
    }
}