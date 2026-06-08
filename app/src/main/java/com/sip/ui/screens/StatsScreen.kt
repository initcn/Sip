package com.sip.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.unit.dp
import com.sip.ui.PeriodRange
import com.sip.ui.SipViewModel
import com.sip.ui.components.cards.SipCard
import com.sip.ui.components.inputs.SelectionPill
import com.sip.ui.components.layout.ScreenContainer
import com.sip.ui.components.layout.ScreenHeader

@Composable
fun StatsScreen(
    paddingValues: PaddingValues,
    viewModel: SipViewModel
) {
    /*
    ---------------------------------------------------
    REACTIVE STATE COLLECTION
    ---------------------------------------------------
    */
    // Observing selectedRange ensures the UI adapts when the user changes the filter.
    // The ViewModel reacts to this via flatMapLatest .
    val selectedRange by viewModel.selectedRange.collectAsState()

    // stats is a StateFlow observing the Room database[cite: 228, 233].
    // It will emit new values automatically when the water_entries table changes.
    val stats by viewModel.stats.collectAsState()

    /*
    ---------------------------------------------------
    UI RENDER LAYER
    ---------------------------------------------------
    */
    ScreenContainer(paddingValues = paddingValues) {

        /*
        ---------------------------------------------------
        HEADER INTERFACE
        ---------------------------------------------------
        */
        item {
            ScreenHeader(
                title = "Stats",
                trailingContent = {
                    SelectionPill(
                        selectedItem = selectedRange,
                        items = PeriodRange.entries,
                        label = { it.label },
                        onItemSelected = { viewModel.setRange(it) }
                    )
                }
            )
        }

        /*
        ---------------------------------------------------
        SUMMARY HIGHLIGHT CARD
        ---------------------------------------------------
        */
        item {
            val titleString = when (selectedRange) {
                PeriodRange.DAY -> "Today"
                PeriodRange.WEEK -> "Last 7 Days"
                PeriodRange.MONTH -> java.text.SimpleDateFormat(
                    "MMMM yyyy",
                    LocalLocale.current.platformLocale
                ).format(java.util.Date())
            }

            SummaryCard(
                title = titleString,
                total = "${stats.total} ml",
                average = "${stats.average} ml/day average"
            )
        }

        /*
        ---------------------------------------------------
        INSIGHT PACK GRID
        ---------------------------------------------------
        */
        item {
            val subtext = when (selectedRange) {
                PeriodRange.DAY -> "today"
                PeriodRange.WEEK -> "last 7 days"
                PeriodRange.MONTH -> "this month"
            }

            InsightRow(
                average = "${stats.average} ml",
                total = "${stats.total} ml",
                bestDay = "${stats.bestDay} ml",
                totalSubtitle = subtext
            )
        }
    }
}

/*
---------------------------------------------------
LOCALIZED PRIVATE PRESENTATION CARD BUNDLES
---------------------------------------------------
*/

@Composable
private fun SummaryCard(
    title: String,
    total: String,
    average: String
) {
    SipCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = total,
                style = MaterialTheme.typography.displayMedium
            )
            Text(
                text = average,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun InsightRow(
    average: String,
    total: String,
    bestDay: String,
    totalSubtitle: String
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        maxItemsInEachRow = 2
    ) {
        Box(modifier = Modifier.fillMaxWidth(0.48f)) {
            InsightCard(
                title = "Average",
                value = average,
                subtitle = "per day"
            )
        }

        Box(modifier = Modifier.fillMaxWidth(0.48f)) {
            InsightCard(
                title = "Total",
                value = total,
                subtitle = totalSubtitle
            )
        }

        Box(modifier = Modifier.fillMaxWidth(0.48f)) {
            InsightCard(
                title = "Best Day",
                value = bestDay,
                subtitle = ""
            )
        }
    }
}

@Composable
private fun InsightCard(
    title: String,
    value: String,
    subtitle: String
) {
    SipCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}