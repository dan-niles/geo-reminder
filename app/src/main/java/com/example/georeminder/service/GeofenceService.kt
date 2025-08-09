package com.example.georeminder.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.georeminder.R
import com.example.georeminder.data.ReminderDatabase
import com.example.georeminder.data.ReminderEntity
import com.example.georeminder.data.ReminderRepository
import com.example.georeminder.receiver.GeofenceBroadcastReceiver
import com.example.georeminder.ui.MainActivity
import com.example.georeminder.utils.LocationPermissionHelper
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class GeofenceService : Service() {
    
    private lateinit var repository: ReminderRepository
    
    private lateinit var geofencingClient: GeofencingClient
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    override fun onCreate() {
        super.onCreate()
        geofencingClient = LocationServices.getGeofencingClient(this)
        
        val database = ReminderDatabase.getDatabase(this)
        repository = ReminderRepository(database.reminderDao())
        
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_MONITORING -> startMonitoring()
            ACTION_STOP_MONITORING -> stopSelf()
            ACTION_ADD_GEOFENCE -> {
                val reminder = intent.getParcelableExtra<ReminderEntity>(EXTRA_REMINDER)
                reminder?.let { addGeofence(it) }
            }
            ACTION_REMOVE_GEOFENCE -> {
                val geofenceId = intent.getStringExtra(EXTRA_GEOFENCE_ID)
                geofenceId?.let { removeGeofence(it) }
            }
        }
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun startMonitoring() {
        if (!LocationPermissionHelper.hasLocationPermissions(this)) {
            stopSelf()
            return
        }
        
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        
        // Load and setup existing active geofences
        serviceScope.launch {
            repository.getActiveReminders().collect { reminders ->
                setupGeofences(reminders)
            }
        }
    }
    
    private fun setupGeofences(reminders: List<ReminderEntity>) {
        if (!LocationPermissionHelper.hasLocationPermissions(this)) return
        
        val geofences = reminders.map { reminder ->
            Geofence.Builder()
                .setRequestId(reminder.geofenceId)
                .setCircularRegion(reminder.latitude, reminder.longitude, reminder.radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()
        }
        
        if (geofences.isNotEmpty()) {
            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(geofences)
                .build()
            
            try {
                geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
            } catch (e: SecurityException) {
                // Permission was revoked
                stopSelf()
            }
        }
    }
    
    private fun addGeofence(reminder: ReminderEntity) {
        if (!LocationPermissionHelper.hasLocationPermissions(this)) return
        
        val geofence = Geofence.Builder()
            .setRequestId(reminder.geofenceId)
            .setCircularRegion(reminder.latitude, reminder.longitude, reminder.radius)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()
        
        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
        
        try {
            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
        } catch (e: SecurityException) {
            // Handle permission error
        }
    }
    
    private fun removeGeofence(geofenceId: String) {
        geofencingClient.removeGeofences(listOf(geofenceId))
    }
    
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
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
        serviceScope.cancel()
        geofencingClient.removeGeofences(geofencePendingIntent)
    }
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "geofence_service_channel"
        private const val CHANNEL_NAME = "Location Monitoring"
        
        const val ACTION_START_MONITORING = "com.example.georeminder.START_MONITORING"
        const val ACTION_STOP_MONITORING = "com.example.georeminder.STOP_MONITORING"
        const val ACTION_ADD_GEOFENCE = "com.example.georeminder.ADD_GEOFENCE"
        const val ACTION_REMOVE_GEOFENCE = "com.example.georeminder.REMOVE_GEOFENCE"
        const val EXTRA_REMINDER = "extra_reminder"
        const val EXTRA_GEOFENCE_ID = "extra_geofence_id"
        
        fun startMonitoring(context: Context) {
            val intent = Intent(context, GeofenceService::class.java).apply {
                action = ACTION_START_MONITORING
            }
            ContextCompat.startForegroundService(context, intent)
        }
        
        fun stopMonitoring(context: Context) {
            val intent = Intent(context, GeofenceService::class.java).apply {
                action = ACTION_STOP_MONITORING
            }
            context.startService(intent)
        }
        
        fun addGeofence(context: Context, reminder: ReminderEntity) {
            val intent = Intent(context, GeofenceService::class.java).apply {
                action = ACTION_ADD_GEOFENCE
                putExtra(EXTRA_REMINDER, reminder)
            }
            context.startService(intent)
        }
        
        fun removeGeofence(context: Context, geofenceId: String) {
            val intent = Intent(context, GeofenceService::class.java).apply {
                action = ACTION_REMOVE_GEOFENCE
                putExtra(EXTRA_GEOFENCE_ID, geofenceId)
            }
            context.startService(intent)
        }
    }
}