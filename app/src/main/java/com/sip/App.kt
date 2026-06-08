package com.sip

import android.app.Application
import com.sip.data.SipDatabase
import com.sip.ui.SipViewModel

class App : Application() {

    val database by lazy {
        SipDatabase.getDatabase(this)
    }

    // Exposes the single leak-free engine instance globally
    val sipViewModel by lazy {
        SipViewModel(
            context = this,
            waterDao = database.waterDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper(this).createNotificationChannel()
    }
}