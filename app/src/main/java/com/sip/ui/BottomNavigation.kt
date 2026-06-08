package com.sip.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sip.R
import com.sip.ui.theme.SipDimens
import com.sip.ui.theme.SipShapes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class SipScreen {

    HOME,
    HISTORY,

    STATS,
    SETTINGS
}

@Composable
fun SipBottomNavigation(
    currentScreen: SipScreen,
    onScreenSelected: (SipScreen) -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()

            .padding(
                horizontal =
                    SipDimens
                        .BottomBarHorizontalPadding,

                vertical =
                    SipDimens
                        .BottomBarVerticalPadding
            ),

        contentAlignment =
            Alignment.Center
    ) {

        Surface(

            shape =
                SipShapes
                    .BottomBar,

            tonalElevation = 6.dp,

            shadowElevation = 10.dp,

            color =
                MaterialTheme
                    .colorScheme
                    .surfaceContainerHigh
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()

                    .height(
                        SipDimens
                            .BottomBarHeight
                    )

                    .padding(
                        horizontal =
                            SipDimens
                                .BottomBarContentPadding
                    ),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                /*
                ---------------------------------------------------
                HOME
                ---------------------------------------------------
                */

                BottomNavItem(
                    selected =
                        currentScreen ==
                                SipScreen.HOME,

                    icon = {

                        Icon(
                            imageVector =
                                Icons.Default.Home,

                            contentDescription =
                                stringResource(
                                    R.string.home
                                ),

                            modifier = Modifier
                                .size(
                                    SipDimens
                                        .BottomBarIconSize
                                )
                        )
                    },

                    onClick = {

                        onScreenSelected(
                            SipScreen.HOME
                        )
                    }
                )

                /*
                ---------------------------------------------------
                HISTORY
                ---------------------------------------------------
                */

                BottomNavItem(
                    selected =
                        currentScreen ==
                                SipScreen.HISTORY,

                    icon = {

                        Icon(
                            imageVector =
                                Icons.Default.History,

                            contentDescription =
                                stringResource(
                                    R.string.history
                                ),

                            modifier = Modifier
                                .size(
                                    SipDimens
                                        .BottomBarIconSize
                                )
                        )
                    },

                    onClick = {

                        onScreenSelected(
                            SipScreen.HISTORY
                        )
                    }
                )

                /*
                ---------------------------------------------------
                STATS
                ---------------------------------------------------
                */

                BottomNavItem(
                    selected =
                        currentScreen ==
                                SipScreen.STATS,

                    icon = {

                        Icon(
                            imageVector =
                                Icons.Default.Insights,

                            contentDescription =
                                "Stats",

                            modifier = Modifier
                                .size(
                                    SipDimens
                                        .BottomBarIconSize
                                )
                        )
                    },

                    onClick = {

                        onScreenSelected(
                            SipScreen.STATS
                        )
                    }
                )

                /*
                ---------------------------------------------------
                SETTINGS
                ---------------------------------------------------
                */

                BottomNavItem(
                    selected =
                        currentScreen ==
                                SipScreen.SETTINGS,

                    icon = {

                        Icon(
                            imageVector =
                                Icons.Default.Settings,

                            contentDescription =
                                stringResource(
                                    R.string.settings
                                ),

                            modifier = Modifier
                                .size(
                                    SipDimens
                                        .BottomBarIconSize
                                )
                        )
                    },

                    onClick = {

                        onScreenSelected(
                            SipScreen.SETTINGS
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    selected: Boolean,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {

    Box(
        modifier = Modifier

            .clip(CircleShape)

            .background(

                if (selected) {

                    MaterialTheme
                        .colorScheme
                        .secondaryContainer

                } else {

                    Color.Transparent
                }
            )

            .clickable {

                onClick()
            }

            .padding(
                horizontal =
                    SipDimens
                        .BottomBarItemHorizontalPadding,

                vertical =
                    SipDimens
                        .BottomBarItemVerticalPadding
            ),

        contentAlignment =
            Alignment.Center
    ) {

        icon()
    }
}

private val Context.dataStore by preferencesDataStore(
    name = "sip_settings"
)

class SettingsPreferences(
    private val context: Context
) {

    companion object {

        /*
        ---------------------------------------------------
        REMINDERS
        ---------------------------------------------------
        */

        private val REMINDERS_ENABLED =
            booleanPreferencesKey(
                "reminders_enabled"
            )

        /*
        ---------------------------------------------------
        INTERVAL
        ---------------------------------------------------
        */

        private val INTERVAL_MINUTES =
            longPreferencesKey(
                "interval_minutes"
            )

        /*
        ---------------------------------------------------
        START TIME
        ---------------------------------------------------
        */

        private val START_TIME =
            stringPreferencesKey(
                "start_time"
            )

        /*
        ---------------------------------------------------
        END TIME
        ---------------------------------------------------
        */

        private val END_TIME =
            stringPreferencesKey(
                "end_time"
            )

        /*
        ---------------------------------------------------
        DAILY GOAL
        ---------------------------------------------------
        */

        private val DAILY_GOAL =
            longPreferencesKey(
                "daily_goal"
            )
    }

    /*
    ---------------------------------------------------
    REMINDERS ENABLED
    ---------------------------------------------------
    */

    val remindersEnabled: Flow<Boolean> =
        context.dataStore.data.map {

            it[REMINDERS_ENABLED] ?: false
        }

    /*
    ---------------------------------------------------
    INTERVAL
    ---------------------------------------------------
    */

    val intervalMinutes: Flow<Long> =
        context.dataStore.data.map {

            it[INTERVAL_MINUTES] ?: 60L
        }

    /*
    ---------------------------------------------------
    START TIME
    ---------------------------------------------------
    */

    val startTime: Flow<String> =
        context.dataStore.data.map {

            it[START_TIME] ?: "08:00"
        }

    /*
    ---------------------------------------------------
    END TIME
    ---------------------------------------------------
    */

    val endTime: Flow<String> =
        context.dataStore.data.map {

            it[END_TIME] ?: "22:00"
        }

    /*
    ---------------------------------------------------
    DAILY GOAL
    ---------------------------------------------------
    */

    val dailyGoal: Flow<Long> =
        context.dataStore.data.map {

            it[DAILY_GOAL] ?: 2000L
        }

    /*
    ---------------------------------------------------
    SET REMINDERS ENABLED
    ---------------------------------------------------
    */

    suspend fun setRemindersEnabled(
        enabled: Boolean
    ) {

        context.dataStore.edit {

            it[REMINDERS_ENABLED] = enabled
        }
    }

    /*
    ---------------------------------------------------
    SET INTERVAL
    ---------------------------------------------------
    */

    suspend fun setIntervalMinutes(
        minutes: Long
    ) {

        context.dataStore.edit {

            it[INTERVAL_MINUTES] = minutes
        }
    }

    /*
    ---------------------------------------------------
    SET START TIME
    ---------------------------------------------------
    */

    suspend fun setStartTime(
        time: String
    ) {

        context.dataStore.edit {

            it[START_TIME] = time
        }
    }

    /*
    ---------------------------------------------------
    SET END TIME
    ---------------------------------------------------
    */

    suspend fun setEndTime(
        time: String
    ) {

        context.dataStore.edit {

            it[END_TIME] = time
        }
    }

    /*
    ---------------------------------------------------
    SET DAILY GOAL
    ---------------------------------------------------
    */

    suspend fun setDailyGoal(
        goal: Long
    ) {

        context.dataStore.edit {

            it[DAILY_GOAL] = goal
        }
    }
}