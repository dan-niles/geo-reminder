package com.example.georeminder.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.georeminder.data.ReminderEntity
import com.example.georeminder.ui.theme.GeoReminderTheme
import com.example.georeminder.ui.theme.StatusActive
import com.example.georeminder.ui.theme.StatusInactive

@Composable
fun ReminderItem(
    reminder: ReminderEntity,
    onItemClick: (ReminderEntity) -> Unit,
    onToggleClick: (ReminderEntity, Boolean) -> Unit,
    onMenuClick: (ReminderEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { onItemClick(reminder) },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isActive) 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Location icon with circular background
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (reminder.isActive) 
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else 
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    )
                    .padding(10.dp),
                tint = if (reminder.isActive) 
                    MaterialTheme.colorScheme.primary
                else 
                    MaterialTheme.colorScheme.outline
            )
            
            // Title and description
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (!reminder.description.isNullOrBlank()) {
                    Text(
                        text = reminder.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Distance info with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "🔵 ${reminder.radius.toInt()}m radius",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• ⏰ ${formatCreationDate(reminder.createdAt)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Status and menu
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Status badge (clickable to toggle)
                Text(
                    text = if (reminder.isActive) "✅ ACTIVE" else "⏸️ INACTIVE",
                    modifier = Modifier
                        .background(
                            color = if (reminder.isActive) StatusActive else StatusInactive,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onToggleClick(reminder, !reminder.isActive) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                
                // Menu button
                IconButton(
                    onClick = { onMenuClick(reminder) }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun formatCreationDate(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val days = diff / (1000 * 60 * 60 * 24)
    
    return when {
        days == 0L -> "today"
        days == 1L -> "1 day ago"
        days < 7 -> "$days days ago"
        else -> "${days / 7} weeks ago"
    }
}

@Preview(showBackground = true)
@Composable
fun ReminderItemPreview() {
    GeoReminderTheme {
        ReminderItem(
            reminder = ReminderEntity(
                id = 1,
                title = "Bus Stop Reminder",
                description = "Wake me up before my stop",
                latitude = 37.7749,
                longitude = -122.4194,
                radius = 250f,
                isActive = true,
                createdAt = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000)
            ),
            onItemClick = {},
            onToggleClick = { _, _ -> },
            onMenuClick = {}
        )
    }
}