package com.example.georeminder.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.georeminder.ui.theme.GeoReminderTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderScreen(
    onBackClick: () -> Unit,
    onCurrentLocationClick: () -> Unit,
    onMapSelectionClick: () -> Unit,
    onSaveClick: (title: String, description: String, latitude: Double, longitude: Double, radius: Float) -> Unit,
    modifier: Modifier = Modifier,
    currentLatitude: Double? = null,
    currentLongitude: Double? = null,
    isEditMode: Boolean = false,
    initialTitle: String = "",
    initialDescription: String = "",
    initialRadius: Float = 100f
) {
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var latitude by remember(currentLatitude) { mutableStateOf(currentLatitude?.toString() ?: "") }
    var longitude by remember(currentLongitude) { mutableStateOf(currentLongitude?.toString() ?: "") }
    var radius by remember { mutableFloatStateOf(initialRadius) }
    
    var titleError by remember { mutableStateOf<String?>(null) }
    var latitudeError by remember { mutableStateOf<String?>(null) }
    var longitudeError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Reminder" else "Add Reminder") },
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title field with emoji
            OutlinedTextField(
                value = title,
                onValueChange = { 
                    title = it
                    titleError = null
                },
                label = { Text("📝 Reminder title") },
                placeholder = { Text("e.g., Pick up groceries") },
                isError = titleError != null,
                supportingText = titleError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Description field with emoji
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("💬 Description (optional)") },
                placeholder = { Text("Add more details about this reminder...") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            
            // Location section with emoji
            Text(
                text = "📍 Location Coordinates",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = latitude,
                    onValueChange = { 
                        latitude = it
                        latitudeError = null
                    },
                    label = { Text("🌐 Latitude") },
                    placeholder = { Text("e.g., 37.7749") },
                    isError = latitudeError != null,
                    supportingText = latitudeError?.let { { Text(it) } },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                OutlinedTextField(
                    value = longitude,
                    onValueChange = { 
                        longitude = it
                        longitudeError = null
                    },
                    label = { Text("🌍 Longitude") },
                    placeholder = { Text("e.g., -122.4194") },
                    isError = longitudeError != null,
                    supportingText = longitudeError?.let { { Text(it) } },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
            
            // Location Selection Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCurrentLocationClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text("Use Current Location")
                }
                
                OutlinedButton(
                    onClick = onMapSelectionClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text("Select on Map")
                }
            }
            
            // Radius section
            Text(
                text = "Alert radius: ${radius.toInt()}m",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Slider(
                value = radius,
                onValueChange = { radius = it },
                valueRange = 50f..500f,
                steps = 17, // (500-50)/25 - 1
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Save button
            Button(
                onClick = {
                    // Validate inputs
                    var hasError = false
                    
                    if (title.isBlank()) {
                        titleError = "Title is required"
                        hasError = true
                    }
                    
                    val lat = latitude.toDoubleOrNull()
                    if (lat == null || lat < -90 || lat > 90) {
                        latitudeError = "Invalid latitude (-90 to 90)"
                        hasError = true
                    }
                    
                    val lng = longitude.toDoubleOrNull()
                    if (lng == null || lng < -180 || lng > 180) {
                        longitudeError = "Invalid longitude (-180 to 180)"
                        hasError = true
                    }
                    
                    if (!hasError && lat != null && lng != null) {
                        onSaveClick(title, description, lat, lng, radius)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditMode) "Update Reminder" else "Save Reminder")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddReminderScreenPreview() {
    GeoReminderTheme {
        AddReminderScreen(
            onBackClick = {},
            onCurrentLocationClick = {},
            onMapSelectionClick = {},
            onSaveClick = { _, _, _, _, _ -> },
            currentLatitude = 37.7749,
            currentLongitude = -122.4194
        )
    }
}