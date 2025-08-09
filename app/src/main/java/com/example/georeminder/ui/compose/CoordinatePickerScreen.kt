package com.example.georeminder.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.georeminder.ui.theme.GeoReminderTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoordinatePickerScreen(
    onBackClick: () -> Unit,
    onLocationSelected: (latitude: Double, longitude: Double) -> Unit,
    modifier: Modifier = Modifier,
    initialLatitude: Double = 37.7749,
    initialLongitude: Double = -122.4194
) {
    var selectedLatitude by remember { mutableStateOf(initialLatitude) }
    var selectedLongitude by remember { mutableStateOf(initialLongitude) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Pick Coordinates") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onLocationSelected(selectedLatitude, selectedLongitude)
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Select Location"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Instructions
            Text(
                text = "Tap on the world map below to select coordinates",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Current coordinates display
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Selected Location",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Latitude: ${"%.6f".format(selectedLatitude)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Longitude: ${"%.6f".format(selectedLongitude)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // World Map Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                WorldMapCanvas(
                    selectedLatitude = selectedLatitude,
                    selectedLongitude = selectedLongitude,
                    onLocationTapped = { lat, lng ->
                        selectedLatitude = lat
                        selectedLongitude = lng
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // Help text
            Text(
                text = "Tip: This is a simplified world map. For precise locations, you can also enter coordinates manually.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )
        }
    }
}

@Composable
private fun WorldMapCanvas(
    selectedLatitude: Double,
    selectedLongitude: Double,
    onLocationTapped: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    
    Canvas(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures { offset ->
                val width = size.width.toFloat()
                val height = size.height.toFloat()
                
                // Convert tap coordinates to lat/lng
                val longitude = (offset.x / width) * 360.0 - 180.0
                val latitude = 90.0 - (offset.y / height) * 180.0
                
                // Clamp values to valid ranges
                val clampedLat = latitude.coerceIn(-90.0, 90.0)
                val clampedLng = longitude.coerceIn(-180.0, 180.0)
                
                onLocationTapped(clampedLat, clampedLng)
            }
        }
    ) {
        val width = size.width
        val height = size.height
        
        // Draw world map outline (simplified)
        drawWorldMap(onSurfaceColor.copy(alpha = 0.3f))
        
        // Draw grid lines
        drawGrid(onSurfaceColor.copy(alpha = 0.1f))
        
        // Draw selected location marker
        val markerX = ((selectedLongitude + 180.0) / 360.0 * width).toFloat()
        val markerY = ((90.0 - selectedLatitude) / 180.0 * height).toFloat()
        
        // Draw marker with circle and crosshairs
        drawCircle(
            color = primaryColor,
            radius = 12f,
            center = Offset(markerX, markerY)
        )
        drawCircle(
            color = Color.White,
            radius = 8f,
            center = Offset(markerX, markerY)
        )
        drawCircle(
            color = primaryColor,
            radius = 4f,
            center = Offset(markerX, markerY)
        )
    }
}

private fun DrawScope.drawWorldMap(color: Color) {
    val width = size.width
    val height = size.height
    
    // Draw simplified continent outlines
    // This is a very basic representation - you could add more detail
    
    // North America (simplified)
    drawRect(
        color = color,
        topLeft = Offset(width * 0.15f, height * 0.2f),
        size = androidx.compose.ui.geometry.Size(width * 0.25f, height * 0.4f)
    )
    
    // South America (simplified)
    drawRect(
        color = color,
        topLeft = Offset(width * 0.2f, height * 0.55f),
        size = androidx.compose.ui.geometry.Size(width * 0.15f, height * 0.35f)
    )
    
    // Europe (simplified)
    drawRect(
        color = color,
        topLeft = Offset(width * 0.48f, height * 0.15f),
        size = androidx.compose.ui.geometry.Size(width * 0.1f, height * 0.2f)
    )
    
    // Africa (simplified)
    drawRect(
        color = color,
        topLeft = Offset(width * 0.47f, height * 0.3f),
        size = androidx.compose.ui.geometry.Size(width * 0.15f, height * 0.4f)
    )
    
    // Asia (simplified)
    drawRect(
        color = color,
        topLeft = Offset(width * 0.55f, height * 0.1f),
        size = androidx.compose.ui.geometry.Size(width * 0.3f, height * 0.45f)
    )
    
    // Australia (simplified)
    drawRect(
        color = color,
        topLeft = Offset(width * 0.72f, height * 0.65f),
        size = androidx.compose.ui.geometry.Size(width * 0.12f, height * 0.15f)
    )
}

private fun DrawScope.drawGrid(color: Color) {
    val width = size.width
    val height = size.height
    
    // Draw longitude lines (vertical)
    for (i in 0..12) {
        val x = (i / 12f) * width
        drawLine(
            color = color,
            start = Offset(x, 0f),
            end = Offset(x, height),
            strokeWidth = 1f
        )
    }
    
    // Draw latitude lines (horizontal)
    for (i in 0..6) {
        val y = (i / 6f) * height
        drawLine(
            color = color,
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1f
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CoordinatePickerScreenPreview() {
    GeoReminderTheme {
        CoordinatePickerScreen(
            onBackClick = {},
            onLocationSelected = { _, _ -> },
            initialLatitude = 37.7749,
            initialLongitude = -122.4194
        )
    }
}