package com.example.georeminder.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.georeminder.data.ReminderEntity
import com.example.georeminder.ui.theme.GeoReminderTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    reminders: List<ReminderEntity>,
    onAddReminderClick: () -> Unit,
    onReminderClick: (ReminderEntity) -> Unit,
    onToggleReminder: (ReminderEntity, Boolean) -> Unit,
    onDeleteReminder: (ReminderEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "Geo-Reminder",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddReminderClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add reminder"
                )
            }
        }
    ) { paddingValues ->
        if (reminders.isEmpty()) {
            EmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = reminders,
                    key = { reminder -> reminder.id }
                ) { reminder ->
                    ReminderItem(
                        reminder = reminder,
                        onItemClick = onReminderClick,
                        onToggleClick = onToggleReminder,
                        onMenuClick = onDeleteReminder
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            
            Text(
                text = "🌍 No reminders yet",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Tap the ➕ button to create your first location-based reminder",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    GeoReminderTheme {
        MainScreen(
            reminders = listOf(
                ReminderEntity(
                    id = 1,
                    title = "Bus Stop Reminder",
                    description = "Wake me up before my stop",
                    latitude = 37.7749,
                    longitude = -122.4194,
                    radius = 250f,
                    isActive = true
                ),
                ReminderEntity(
                    id = 2,
                    title = "Grocery Store",
                    description = null,
                    latitude = 37.7849,
                    longitude = -122.4094,
                    radius = 100f,
                    isActive = false
                )
            ),
            onAddReminderClick = {},
            onReminderClick = {},
            onToggleReminder = { _, _ -> },
            onDeleteReminder = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenEmptyPreview() {
    GeoReminderTheme {
        MainScreen(
            reminders = emptyList(),
            onAddReminderClick = {},
            onReminderClick = {},
            onToggleReminder = { _, _ -> },
            onDeleteReminder = {}
        )
    }
}