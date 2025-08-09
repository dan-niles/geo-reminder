package com.example.georeminder.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.georeminder.R
import com.example.georeminder.data.ReminderDatabase
import com.example.georeminder.data.ReminderRepository
import com.example.georeminder.service.OfflineLocationService
import com.example.georeminder.ui.compose.MainScreen
import com.example.georeminder.ui.theme.GeoReminderTheme
import com.example.georeminder.utils.LocationPermissionHelper
import com.example.georeminder.utils.NotificationHelper

class MainActivity : ComponentActivity() {
    
    private lateinit var viewModel: MainViewModel
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                // Location permissions granted
                requestBackgroundLocationPermission()
            }
            else -> {
                showPermissionDeniedDialog()
            }
        }
    }
    
    private val requestBackgroundLocationLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            requestNotificationPermission()
        } else {
            showBackgroundLocationRationale()
        }
    }
    
    private val requestNotificationLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startLocationMonitoring()
        } else {
            // App can still work without notification permission
            startLocationMonitoring()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize ViewModel
        val database = ReminderDatabase.getDatabase(this)
        val repository = ReminderRepository(database.reminderDao())
        viewModel = MainViewModel(repository)
        
        // Create notification channels
        NotificationHelper.createNotificationChannels(this)
        
        // Check permissions on startup
        checkAndRequestPermissions()
        
        setContent {
            GeoReminderTheme {
                val reminders by viewModel.reminders.collectAsState(initial = emptyList())
                
                MainScreen(
                    reminders = reminders,
                    onAddReminderClick = {
                        val intent = Intent(this@MainActivity, AddReminderActivity::class.java)
                        startActivity(intent)
                    },
                    onReminderClick = { reminder ->
                        val intent = Intent(this@MainActivity, AddReminderActivity::class.java).apply {
                            putExtra(AddReminderActivity.EXTRA_REMINDER_ID, reminder.id)
                            putExtra(AddReminderActivity.EXTRA_REMINDER_TITLE, reminder.title)
                            putExtra(AddReminderActivity.EXTRA_REMINDER_DESCRIPTION, reminder.description ?: "")
                            putExtra(AddReminderActivity.EXTRA_REMINDER_LATITUDE, reminder.latitude)
                            putExtra(AddReminderActivity.EXTRA_REMINDER_LONGITUDE, reminder.longitude)
                            putExtra(AddReminderActivity.EXTRA_REMINDER_RADIUS, reminder.radius)
                        }
                        startActivity(intent)
                    },
                    onToggleReminder = { reminder, isActive ->
                        viewModel.toggleReminderStatus(reminder.id, isActive)
                    },
                    onDeleteReminder = { reminder ->
                        showDeleteConfirmationDialog(reminder.id, reminder.title)
                    }
                )
            }
        }
    }
    
    private fun checkAndRequestPermissions() {
        when {
            LocationPermissionHelper.hasLocationPermissions(this) -> {
                if (LocationPermissionHelper.hasBackgroundLocationPermission(this)) {
                    requestNotificationPermission()
                } else {
                    requestBackgroundLocationPermission()
                }
            }
            else -> {
                requestLocationPermission()
            }
        }
    }
    
    private fun requestLocationPermission() {
        val permissions = LocationPermissionHelper.getLocationPermissions()
        requestPermissionLauncher.launch(permissions)
    }
    
    private fun requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!LocationPermissionHelper.hasBackgroundLocationPermission(this)) {
                showBackgroundLocationRationale()
                return
            }
        }
        requestNotificationPermission()
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!LocationPermissionHelper.hasNotificationPermission(this)) {
                val permissions = LocationPermissionHelper.getNotificationPermissions()
                if (permissions.isNotEmpty()) {
                    requestNotificationLauncher.launch(permissions[0])
                    return
                }
            }
        }
        startLocationMonitoring()
    }
    
    private fun startLocationMonitoring() {
        if (LocationPermissionHelper.hasLocationPermissions(this)) {
            OfflineLocationService.startMonitoring(this)
        }
    }
    
    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.permission_location_title)
            .setMessage(R.string.permission_location_message)
            .setPositiveButton(R.string.grant_permission) { _, _ ->
                openAppSettings()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun showBackgroundLocationRationale() {
        AlertDialog.Builder(this)
            .setTitle(R.string.permission_background_title)
            .setMessage(R.string.permission_background_message)
            .setPositiveButton(R.string.grant_permission) { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    requestBackgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                requestNotificationPermission()
            }
            .show()
    }
    
    private fun showDeleteConfirmationDialog(reminderId: Long, title: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete)
            .setMessage("Delete '$title'?")
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteReminder(reminderId)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", packageName, null)
        startActivity(intent)
    }
}