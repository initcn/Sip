package com.sip.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // Explicitly required for standard listing items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sip.R
import com.sip.data.WaterEntry
import com.sip.ui.components.cards.SipCard
import com.sip.ui.components.layout.ScreenHeader
import com.sip.ui.theme.SipShapes
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    paddingValues: PaddingValues,
    entries: List<WaterEntry>,
    onDeleteEntry: (WaterEntry) -> Unit
) {
    /*
    ---------------------------------------------------
    DELETE CONFIRMATION STATE
    ---------------------------------------------------
    */
    var entryToDelete by remember { mutableStateOf<WaterEntry?>(null) }

    if (entryToDelete != null) {
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            shape = SipShapes.LargeCard,
            title = { Text(text = stringResource(R.string.delete_entry)) },
            text = { Text(text = stringResource(R.string.delete_entry_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteEntry(entryToDelete!!)
                        entryToDelete = null
                    }
                ) {
                    Text(
                        text = stringResource(R.string.delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }

    /*
    ---------------------------------------------------
    TIMELINE MATHEMATICS: TIME-ZONE GROUPING ENGINE
    ---------------------------------------------------
    */
    val groupedEntries = remember(entries) {
        val headerFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val todayCalendar = Calendar.getInstance()
        val yesterdayCalendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

        entries.groupBy { entry ->
            val entryCalendar = Calendar.getInstance().apply { timeInMillis = entry.timestamp }

            when {
                entryCalendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
                        entryCalendar.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR) -> {
                    "Today"
                }
                entryCalendar.get(Calendar.YEAR) == yesterdayCalendar.get(Calendar.YEAR) &&
                        entryCalendar.get(Calendar.DAY_OF_YEAR) == yesterdayCalendar.get(Calendar.DAY_OF_YEAR) -> {
                    "Yesterday"
                }
                else -> {
                    headerFormat.format(Date(entry.timestamp))
                }
            }
        }
    }

    /*
    ---------------------------------------------------
    UI SCROLLTIMELINE INTERFACE
    ---------------------------------------------------
    */
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // App Tab Layout Header
        item {
            ScreenHeader(title = stringResource(R.string.history))
        }

        if (entries.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hydration logs found.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        /*
        ---------------------------------------------------
        STICKY COMPOSABLE DISPATCH LOOPS
        ---------------------------------------------------
        */
        groupedEntries.forEach { (dateHeader, dayEntries) ->

            // Native sticky boundary pinning descriptor
            stickyHeader(key = dateHeader) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = dateHeader,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Group Sub-items
            items(
                items = dayEntries,
                key = { it.id }
            ) { entry ->
                SwipeToDeleteItem(
                    modifier = Modifier.animateItem(),
                    entry = entry,
                    onConfirmDelete = { entryToDelete = entry }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteItem(
    modifier: Modifier = Modifier,
    entry: WaterEntry,
    onConfirmDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.StartToEnd) {
            onConfirmDelete()
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = false,
        backgroundContent = {
            val isSwiping = dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isSwiping) Color.Red.copy(alpha = 0.8f) else Color.Transparent,
                        shape = SipShapes.LargeCard
                    )
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (isSwiping) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = Color.White
                    )
                }
            }
        }
    ) {
        HistoryEntryItem(entry = entry)
    }
}

@Composable
private fun HistoryEntryItem(
    entry: WaterEntry
) {
    val formatter = remember {
        SimpleDateFormat("hh:mm a", Locale.getDefault())
    }

    SipCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "${entry.amount} ml",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = formatter.format(Date(entry.timestamp)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}