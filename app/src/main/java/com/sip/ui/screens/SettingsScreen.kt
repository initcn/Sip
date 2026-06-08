package com.sip.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sip.MainActivity
import com.sip.R
import com.sip.ReminderScheduler
import com.sip.ui.SipViewModel
import com.sip.ui.components.cards.SipCard
import com.sip.ui.components.layout.ScreenContainer
import com.sip.ui.components.layout.ScreenHeader
import com.sip.ui.theme.SipShapes

@Composable
fun SettingsScreen(
    paddingValues: PaddingValues,
    viewModel: SipViewModel
) {
    val context = LocalContext.current

    /*
    ---------------------------------------------------
    STATE COLLECTION FROM SHARED VIEWMODEL
    ---------------------------------------------------
    */
    val remindersEnabled by viewModel.remindersEnabled.collectAsState()
    val intervalMinutes by viewModel.intervalMinutes.collectAsState()
    val startTime by viewModel.startTime.collectAsState()
    val endTime by viewModel.endTime.collectAsState()
    val dailyGoal by viewModel.dailyGoal.collectAsState()

    var showPermissionDialog by remember { mutableStateOf(false) }

    /*
    ---------------------------------------------------
    UI SCROLLCONTAINER HIERARCHY
    ---------------------------------------------------
    */
    ScreenContainer(paddingValues = paddingValues) {

        item {
            ScreenHeader(title = stringResource(R.string.settings))
        }

        /*
        ---------------------------------------------------
        REMINDERS SECTION CARD
        ---------------------------------------------------
        */
        item {
            SipCard {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {

                    // Main Toggle Switch Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.reminders),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Switch(
                            checked = remindersEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.setRemindersEnabled(enabled)
                                if (enabled) {
                                    showPermissionDialog = true
                                } else {
                                    ReminderScheduler.stopReminders(context)
                                }
                            }
                        )
                    }

                    // FIXED: Re-mapped dynamically to use centralized engine validation bounds
                    PresetWithCustomInputSection(
                        title = stringResource(R.string.reminder_interval),
                        selectedValue = intervalMinutes,
                        presets = listOf(30L, 60L),
                        valueSuffix = "mins",
                        customLabel = stringResource(R.string.custom),
                        placeholder = stringResource(R.string.minutes),
                        validRange = SipViewModel.MIN_INTERVAL..SipViewModel.MAX_INTERVAL,
                        errorText = stringResource(R.string.interval_validation),
                        onValueSelected = { minutes ->
                            viewModel.setIntervalMinutes(minutes)
                            if (remindersEnabled) {
                                ReminderScheduler.stopReminders(context)
                                ReminderScheduler.startReminders(context, minutes)
                            }
                        }
                    )

                    HorizontalDivider()

                    // Consolidated Notification Active Frame Picker
                    NotificationDurationSection(
                        startTime = startTime,
                        endTime = endTime,
                        onStartTimeChanged = { time ->
                            viewModel.setStartTime(time)
                            if (remindersEnabled) {
                                ReminderScheduler.stopReminders(context)
                                ReminderScheduler.startReminders(context, intervalMinutes)
                            }
                        },
                        onEndTimeChanged = { time ->
                            viewModel.setEndTime(time)
                            if (remindersEnabled) {
                                ReminderScheduler.stopReminders(context)
                                ReminderScheduler.startReminders(context, intervalMinutes)
                            }
                        }
                    )

                    Text(
                        text = "You’ll only receive reminders between start and stop times.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        /*
        ---------------------------------------------------
        DAILY GOAL SECTION CARD
        ---------------------------------------------------
        */
        item {
            SipCard {
                // FIXED: Re-mapped dynamically to use centralized engine validation bounds
                PresetWithCustomInputSection(
                    title = stringResource(R.string.daily_goal),
                    selectedValue = dailyGoal,
                    presets = listOf(2000L, 3000L),
                    valueSuffix = "ml",
                    customLabel = stringResource(R.string.custom),
                    placeholder = stringResource(R.string.custom_goal),
                    validRange = SipViewModel.MIN_DAILY_GOAL..SipViewModel.MAX_DAILY_GOAL,
                    errorText = stringResource(R.string.goal_validation),
                    onValueSelected = { goal -> viewModel.setDailyGoal(goal) }
                )
            }
        }
    }

    /*
    ---------------------------------------------------
    CRITICAL PERMISSION DISPATCHER DIALOG
    ---------------------------------------------------
    */
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            shape = SipShapes.LargeCard,
            title = { Text(text = "Enable Reliable Reminders") },
            text = {
                Text(text = "Sip needs exact alarms and battery optimization disabled for reliable hydration reminders.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        (context as? MainActivity)?.requestExactAlarmPermission()
                        (context as? MainActivity)?.requestBatteryOptimizationDisable()

                        val alarmManager = context.getSystemService(android.app.AlarmManager::class.java)
                        val canSchedule = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                            alarmManager.canScheduleExactAlarms()
                        } else {
                            true
                        }

                        if (canSchedule) {
                            ReminderScheduler.startReminders(context, intervalMinutes)
                        }
                    }
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        viewModel.setRemindersEnabled(false)
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

/*
---------------------------------------------------
LOCALIZED PRIVATE SUB-LAYOUT COMPOSABLES
---------------------------------------------------
*/

@Composable
private fun NotificationDurationSection(
    startTime: String,
    endTime: String,
    onStartTimeChanged: (String) -> Unit,
    onEndTimeChanged: (String) -> Unit
) {
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.notification_duration),
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimeButton(time = startTime, onClick = {
                TimePickerDialog(context, { _, hour, minute ->
                    // FIXED: Added Locale.US to ensure uniform numeral parsing
                    onStartTimeChanged(String.format(java.util.Locale.US, "%02d:%02d", hour, minute))
                }, 8, 0, true).show()
            })

            Text("-")

            TimeButton(time = endTime, onClick = {
                TimePickerDialog(context, { _, hour, minute ->
                    // FIXED: Added Locale.US to ensure uniform numeral parsing
                    onEndTimeChanged(String.format(java.util.Locale.US, "%02d:%02d", hour, minute))
                }, 22, 0, true).show()
            })
        }
    }
}

@Composable
private fun TimeButton(time: String, onClick: () -> Unit) {
    FilterChip(
        selected = false,
        onClick = onClick,
        label = { Text(text = time) }
    )
}

@Composable
private fun PresetWithCustomInputSection(
    title: String,
    selectedValue: Long,
    presets: List<Long>,
    valueSuffix: String,
    customLabel: String,
    placeholder: String,
    validRange: LongRange,
    errorText: String,
    onValueSelected: (Long) -> Unit
) {
    val isCustomValue = selectedValue !in presets
    var customExpanded by remember { mutableStateOf(false) }
    var customValue by remember(selectedValue) {
        mutableStateOf(if (isCustomValue) selectedValue.toString() else "")
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            presets.forEach { preset ->
                FilterChip(
                    selected = selectedValue == preset,
                    onClick = {
                        customExpanded = false
                        customValue = ""
                        onValueSelected(preset)
                    },
                    label = { Text("$preset $valueSuffix") }
                )
            }

            FilterChip(
                selected = isCustomValue,
                onClick = { customExpanded = !customExpanded },
                leadingIcon = { Icon(imageVector = Icons.Default.Edit, contentDescription = null) },
                label = {
                    Text(if (customValue.isNotBlank()) "$customValue $valueSuffix" else customLabel)
                }
            )
        }

        if (customExpanded) {
            val value = customValue.toLongOrNull()
            val isValid = value != null && value in validRange
            val showError = customValue.isNotBlank() && !isValid

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customValue,
                    onValueChange = { customValue = it },
                    placeholder = { Text(placeholder) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = showError,
                    supportingText = { if (showError) Text(errorText) },
                    modifier = Modifier.weight(1f)
                )

                FilledTonalButton(
                    enabled = isValid,
                    onClick = {
                        onValueSelected(value!!)
                        customExpanded = false
                    },
                    modifier = Modifier.height(56.dp)
                ) {
                    Text(stringResource(R.string.ok))
                }
            }
        }
    }
}