package com.example.georeminder.ui

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.example.georeminder.data.ReminderDatabase
import com.example.georeminder.data.ReminderEntity
import com.example.georeminder.data.ReminderRepository
import com.example.georeminder.ui.compose.AddReminderScreen
import com.example.georeminder.ui.theme.GeoReminderTheme
import com.example.georeminder.utils.LocationPermissionHelper
import kotlinx.coroutines.launch

class AddReminderActivity : ComponentActivity() {
    
    companion object {
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_REMINDER_TITLE = "extra_reminder_title"
        const val EXTRA_REMINDER_DESCRIPTION = "extra_reminder_description"
        const val EXTRA_REMINDER_LATITUDE = "extra_reminder_latitude"
        const val EXTRA_REMINDER_LONGITUDE = "extra_reminder_longitude"
        const val EXTRA_REMINDER_RADIUS = "extra_reminder_radius"
    }
    
    private lateinit var repository: ReminderRepository
    private lateinit var locationManager: LocationManager
    private var currentLatitude by mutableStateOf<Double?>(null)
    private var currentLongitude by mutableStateOf<Double?>(null)
    private var isEditMode by mutableStateOf(false)
    private var reminderId: Long = -1
    
    
    private val osmMapSelectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let { data ->
                val latitude = data.getDoubleExtra(OSMMapSelectionActivity.EXTRA_LATITUDE, 0.0)
                val longitude = data.getDoubleExtra(OSMMapSelectionActivity.EXTRA_LONGITUDE, 0.0)
                currentLatitude = latitude
                currentLongitude = longitude
                Toast.makeText(this, "Location selected from map", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize repository
        val database = ReminderDatabase.getDatabase(this)
        repository = ReminderRepository(database.reminderDao())
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        
        // Check if editing existing reminder
        intent.getLongExtra(EXTRA_REMINDER_ID, -1L).let { id ->
            if (id != -1L) {
                isEditMode = true
                reminderId = id
                currentLatitude = intent.getDoubleExtra(EXTRA_REMINDER_LATITUDE, 0.0)
                currentLongitude = intent.getDoubleExtra(EXTRA_REMINDER_LONGITUDE, 0.0)
            }
        }
        
        setContent {
            GeoReminderTheme {
                AddReminderScreen(
                    onBackClick = { finish() },
                    onCurrentLocationClick = { getCurrentLocation() },
                    onMapSelectionClick = { openOSMMapSelection() },
                    onSaveClick = { title, description, latitude, longitude, radius ->
                        if (isEditMode) {
                            updateReminder(title, description, latitude, longitude, radius)
                        } else {
                            saveReminder(title, description, latitude, longitude, radius)
                        }
                    },
                    currentLatitude = currentLatitude,
                    currentLongitude = currentLongitude,
                    isEditMode = isEditMode,
                    initialTitle = intent.getStringExtra(EXTRA_REMINDER_TITLE) ?: "",
                    initialDescription = intent.getStringExtra(EXTRA_REMINDER_DESCRIPTION) ?: "",
                    initialRadius = intent.getFloatExtra(EXTRA_REMINDER_RADIUS, 100f)
                )
            }
        }
    }
    
    private fun getCurrentLocation() {
        if (!LocationPermissionHelper.hasLocationPermissions(this)) {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            val location = if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } else {
                null
            }
            
            location?.let {
                currentLatitude = it.latitude
                currentLongitude = it.longitude
                Toast.makeText(this, "Current location acquired", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(this, "Unable to get current location. Please enter manually.", Toast.LENGTH_LONG).show()
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun getCurrentLocationForMap(onLocationReceived: (Double, Double) -> Unit) {
        try {
            val location = if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } else {
                null
            }
            
            location?.let {
                onLocationReceived(it.latitude, it.longitude)
            } ?: run {
                // Fallback to stored current location or default
                onLocationReceived(currentLatitude ?: 37.7749, currentLongitude ?: -122.4194)
            }
        } catch (e: SecurityException) {
            // Fallback to stored current location or default
            onLocationReceived(currentLatitude ?: 37.7749, currentLongitude ?: -122.4194)
        }
    }
    
    
    private fun openOSMMapSelection() {
        // Always try to get current location first to ensure map defaults to user location
        if (LocationPermissionHelper.hasLocationPermissions(this)) {
            getCurrentLocationForMap { lat, lng ->
                val intent = Intent(this, OSMMapSelectionActivity::class.java).apply {
                    putExtra(OSMMapSelectionActivity.EXTRA_INITIAL_LATITUDE, lat)
                    putExtra(OSMMapSelectionActivity.EXTRA_INITIAL_LONGITUDE, lng)
                }
                osmMapSelectionLauncher.launch(intent)
            }
        } else {
            // Fallback to default location if no permission
            val intent = Intent(this, OSMMapSelectionActivity::class.java).apply {
                putExtra(OSMMapSelectionActivity.EXTRA_INITIAL_LATITUDE, currentLatitude ?: 37.7749)
                putExtra(OSMMapSelectionActivity.EXTRA_INITIAL_LONGITUDE, currentLongitude ?: -122.4194)
            }
            osmMapSelectionLauncher.launch(intent)
        }
    }
    
    private fun saveReminder(title: String, description: String, latitude: Double, longitude: Double, radius: Float) {
        val reminder = ReminderEntity(
            title = title,
            description = description.ifBlank { null },
            latitude = latitude,
            longitude = longitude,
            radius = radius,
            isActive = true
        )
        
        lifecycleScope.launch {
            try {
                repository.insertReminder(reminder)
                
                Toast.makeText(
                    this@AddReminderActivity,
                    "Reminder saved successfully",
                    Toast.LENGTH_SHORT
                ).show()
                
                finish()
            } catch (e: Exception) {
                Toast.makeText(
                    this@AddReminderActivity,
                    "Error saving reminder",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun updateReminder(title: String, description: String, latitude: Double, longitude: Double, radius: Float) {
        val reminder = ReminderEntity(
            id = reminderId,
            title = title,
            description = description.ifBlank { null },
            latitude = latitude,
            longitude = longitude,
            radius = radius,
            isActive = true, // Reactivate when editing
            createdAt = System.currentTimeMillis() // Keep original timestamp would be better, but this is simpler
        )
        
        lifecycleScope.launch {
            try {
                repository.updateReminder(reminder)
                
                Toast.makeText(
                    this@AddReminderActivity,
                    "Reminder updated successfully",
                    Toast.LENGTH_SHORT
                ).show()
                
                finish()
            } catch (e: Exception) {
                Toast.makeText(
                    this@AddReminderActivity,
                    "Error updating reminder",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}