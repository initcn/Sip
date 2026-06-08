package com.sip.ui

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sip.data.WaterDao
import com.sip.data.WaterEntry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

private val Context.dataStore by preferencesDataStore(name = "sip_settings")

enum class PeriodRange(val label: String) {
    DAY("Today"),
    WEEK("1 Week"),
    MONTH("1 Month")
}

data class StatsSummaryUiState(
    val total: Int = 0,
    val average: Int = 0,
    val bestDay: Int = 0
)

class SipViewModel(
    context: Context,
    private val waterDao: WaterDao
) : ViewModel() {

    private val appContext = context.applicationContext

    // Trigger used to force re-calculation when data changes
    private val _refreshTrigger = MutableStateFlow(System.currentTimeMillis())

    companion object {
        private const val DAY_MS = 24 * 60 * 60 * 1000L
        private const val WEEK_MS = 7 * DAY_MS

        const val MIN_WATER_ENTRY = 1L
        const val MAX_WATER_ENTRY = 5000L
        const val MIN_DAILY_GOAL = 500L
        const val MAX_DAILY_GOAL = 15000L
        const val MIN_INTERVAL = 15L
        const val MAX_INTERVAL = 720L

        private val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        private val INTERVAL_MINUTES = longPreferencesKey("interval_minutes")
        private val START_TIME = stringPreferencesKey("start_time")
        private val END_TIME = stringPreferencesKey("end_time")
        private val DAILY_GOAL = longPreferencesKey("daily_goal")
    }

    val todayTotal: StateFlow<Int> = waterDao
        .getTotalBetween(start = startOfDay(), end = endOfDay())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val weekTotal: StateFlow<Int> = waterDao
        .getTotalBetween(start = startOfWeek(), end = Long.MAX_VALUE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val monthTotal: StateFlow<Int> = waterDao
        .getTotalBetween(start = startOfMonth(), end = Long.MAX_VALUE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val weekAverage: StateFlow<Float> = weekTotal
        .map { total -> total.toFloat() / elapsedWeekDays() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val monthAverage: StateFlow<Float> = monthTotal
        .map { total -> total.toFloat() / elapsedMonthDays() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val historyEntries: StateFlow<List<WaterEntry>> = waterDao
        .getAllEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val remindersEnabled = appContext.dataStore.data
        .map { it[REMINDERS_ENABLED] ?: false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val intervalMinutes = appContext.dataStore.data
        .map { it[INTERVAL_MINUTES] ?: 60L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 60L)

    val startTime = appContext.dataStore.data
        .map { it[START_TIME] ?: "08:00" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "08:00")

    val endTime = appContext.dataStore.data
        .map { it[END_TIME] ?: "22:00" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "22:00")

    val dailyGoal = appContext.dataStore.data
        .map { it[DAILY_GOAL] ?: 2000L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 4000L)

    private val _selectedRange = MutableStateFlow(PeriodRange.WEEK)
    val selectedRange = _selectedRange.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val stats: StateFlow<StatsSummaryUiState> = combine(
        selectedRange,
        _refreshTrigger
    ) { range, _ -> range }
        .flatMapLatest { range ->
            val start = when (range) {
                PeriodRange.DAY -> startOfDay()
                PeriodRange.WEEK -> System.currentTimeMillis() - WEEK_MS
                PeriodRange.MONTH -> startOfMonth()
            }
            val now = System.currentTimeMillis()

            kotlinx.coroutines.flow.combine(
                waterDao.getTotalBetween(start = start, end = now),
                waterDao.getBestDayBetween(start = start, end = now)
            ) { total, bestDay ->
                val activeDays = when (range) {
                    PeriodRange.DAY -> 1
                    PeriodRange.WEEK -> elapsedWeekDays()
                    PeriodRange.MONTH -> elapsedMonthDays()
                }
                StatsSummaryUiState(
                    total = total,
                    average = if (activeDays > 0) total / activeDays else 0,
                    bestDay = bestDay
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsSummaryUiState())

    fun setRange(range: PeriodRange) {
        _selectedRange.value = range
    }

    fun addWater(amount: Int) {
        viewModelScope.launch {
            waterDao.insertEntry(WaterEntry(amount = amount, timestamp = System.currentTimeMillis()))
            _refreshTrigger.value = System.currentTimeMillis() // Pulse the trigger to refresh stats
        }
    }

    fun deleteEntry(entry: WaterEntry) {
        viewModelScope.launch {
            waterDao.deleteEntry(entry)
            _refreshTrigger.value = System.currentTimeMillis()
        }
    }

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch { appContext.dataStore.edit { it[REMINDERS_ENABLED] = enabled } }
    }

    fun setIntervalMinutes(minutes: Long) {
        viewModelScope.launch { appContext.dataStore.edit { it[INTERVAL_MINUTES] = minutes } }
    }

    fun setStartTime(time: String) {
        viewModelScope.launch { appContext.dataStore.edit { it[START_TIME] = time } }
    }

    fun setEndTime(time: String) {
        viewModelScope.launch { appContext.dataStore.edit { it[END_TIME] = time } }
    }

    fun setDailyGoal(goal: Long) {
        viewModelScope.launch { appContext.dataStore.edit { it[DAILY_GOAL] = goal } }
    }

    private fun startOfDay(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun endOfDay(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
    }.timeInMillis

    private fun startOfWeek(): Long = Calendar.getInstance().apply {
        while (get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) { add(Calendar.DAY_OF_MONTH, -1) }
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun startOfMonth(): Long = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun elapsedWeekDays(): Int {
        val diff = System.currentTimeMillis() - startOfWeek()
        return ((diff / DAY_MS) + 1).toInt()
    }

    private fun elapsedMonthDays(): Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
}