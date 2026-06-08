package com.sip

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.sip.ui.SipBottomNavigation
import com.sip.ui.SipScreen
import com.sip.ui.screens.HistoryScreen
import com.sip.ui.screens.HomeScreen
import com.sip.ui.screens.SettingsScreen
import com.sip.ui.screens.StatsScreen
import com.sip.ui.theme.SipTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        requestNotificationPermission()

        setContent {
            SipTheme {
                val app = application as App
                val sipViewModel = app.sipViewModel

                val dailyGoal by sipViewModel.dailyGoal.collectAsState()
                var currentScreen by remember { mutableStateOf(SipScreen.HOME) }

                Scaffold(
                    bottomBar = {
                        SipBottomNavigation(
                            currentScreen = currentScreen,
                            onScreenSelected = { currentScreen = it }
                        )
                    }
                ) { paddingValues ->
                    Surface(modifier = Modifier.fillMaxSize()) {
                        when (currentScreen) {
                            /*
                            ---------------------------------------------------
                            HOME SCREEN
                            ---------------------------------------------------
                            */
                            SipScreen.HOME -> {
                                HomeScreen(
                                    paddingValues = paddingValues,
                                    viewModel = sipViewModel,
                                    dailyGoal = dailyGoal
                                )
                            }

                            /*
                            ---------------------------------------------------
                            HISTORY SCREEN (UNIFIED)
                            ---------------------------------------------------
                            */
                            SipScreen.HISTORY -> {
                                // FIXED: Collect stream metrics straight out of our single engine scope
                                val historyEntries by sipViewModel.historyEntries.collectAsState()

                                HistoryScreen(
                                    paddingValues = paddingValues,
                                    entries = historyEntries,
                                    onDeleteEntry = { sipViewModel.deleteEntry(it) }
                                )
                            }

                            /*
                            ---------------------------------------------------
                            STATS SCREEN
                            ---------------------------------------------------
                            */
                            SipScreen.STATS -> {
                                StatsScreen(
                                    paddingValues = paddingValues,
                                    viewModel = sipViewModel
                                )
                            }

                            /*
                            ---------------------------------------------------
                            SETTINGS SCREEN
                            ---------------------------------------------------
                            */
                            SipScreen.SETTINGS -> {
                                SettingsScreen(
                                    paddingValues = paddingValues,
                                    viewModel = sipViewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    /*
    ---------------------------------------------------
    PERMISSIONS & OPTIMIZATION OVERRIDES
    ---------------------------------------------------
    */

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100
                )
            }
        }
    }

    fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }
    }

    @SuppressLint("BatteryLife")
    fun requestBatteryOptimizationDisable() {
        val powerManager = getSystemService(PowerManager::class.java)
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                "package:$packageName".toUri()
            )
            startActivity(intent)
        }
    }
}