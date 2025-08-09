package com.example.georeminder.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.georeminder.R
import com.example.georeminder.data.ReminderDatabase
import com.example.georeminder.data.ReminderEntity
import com.example.georeminder.data.ReminderRepository
import com.example.georeminder.ui.MainActivity
import com.example.georeminder.utils.LocationPermissionHelper
import com.example.georeminder.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class OfflineLocationService : Service(), LocationListener {
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "location_service_channel"
        private const val CHANNEL_NAME = "Location Monitoring"
        private const val LOCATION_UPDATE_INTERVAL = 30000L // 30 seconds
        private const val MIN_DISTANCE_UPDATE = 50f // 50 meters
        
        const val ACTION_START_MONITORING = "com.example.georeminder.START_MONITORING"
        const val ACTION_STOP_MONITORING = "com.example.georeminder.STOP_MONITORING"
        
        fun startMonitoring(context: Context) {
            val intent = Intent(context, OfflineLocationService::class.java).apply {
                action = ACTION_START_MONITORING
            }
            ContextCompat.startForegroundService(context, intent)
        }
        
        fun stopMonitoring(context: Context) {
            val intent = Intent(context, OfflineLocationService::class.java).apply {
                action = ACTION_STOP_MONITORING
            }
            context.startService(intent)
        }
    }
    
    private lateinit var locationManager: LocationManager
    private lateinit var repository: ReminderRepository
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var activeReminders = mutableListOf<ReminderEntity>()
    
    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        
        val database = ReminderDatabase.getDatabase(this)
        repository = ReminderRepository(database.reminderDao())
        
        createNotificationChannel()
        loadActiveReminders()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_MONITORING -> startLocationUpdates()
            ACTION_STOP_MONITORING -> stopSelf()
        }
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun startLocationUpdates() {
        if (!LocationPermissionHelper.hasLocationPermissions(this)) {
            stopSelf()
            return
        }
        
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        
        try {
            // Request location updates from GPS and Network
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    LOCATION_UPDATE_INTERVAL,
                    MIN_DISTANCE_UPDATE,
                    this
                )
            }
            
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    LOCATION_UPDATE_INTERVAL,
                    MIN_DISTANCE_UPDATE,
                    this
                )
            }
        } catch (e: SecurityException) {
            // Permission was revoked
            stopSelf()
        }
    }
    
    private fun loadActiveReminders() {
        serviceScope.launch {
            repository.getActiveReminders().collect { reminders ->
                activeReminders.clear()
                activeReminders.addAll(reminders)
            }
        }
    }
    
    override fun onLocationChanged(location: Location) {
        checkReminders(location)
    }
    
    private fun checkReminders(currentLocation: Location) {
        serviceScope.launch {
            activeReminders.forEach { reminder ->
                val reminderLocation = Location("reminder").apply {
                    latitude = reminder.latitude
                    longitude = reminder.longitude
                }
                
                val distance = currentLocation.distanceTo(reminderLocation)
                
                if (distance <= reminder.radius) {
                    // User is within the geofence radius
                    triggerReminder(reminder)
                }
            }
        }
    }
    
    private fun triggerReminder(reminder: ReminderEntity) {
        NotificationHelper.showReminderNotification(
            this,
            reminder.notificationTitle,
            reminder.notificationMessage,
            reminder.id
        )
        
        // Optionally disable the reminder after triggering
        serviceScope.launch {
            repository.toggleReminderStatus(reminder.id, false)
        }
    }
    
    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GeoReminder Active")
            .setContentText("Monitoring location for reminders")
            .setSmallIcon(R.drawable.ic_location)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows when the app is monitoring your location for reminders"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(this)
        serviceScope.cancel()
    }
}