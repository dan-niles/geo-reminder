package com.example.georeminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.georeminder.data.ReminderDatabase
import com.example.georeminder.data.ReminderRepository
import com.example.georeminder.utils.NotificationHelper
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "GeofenceReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return
        
        if (geofencingEvent.hasError()) {
            Log.e(TAG, "Geofencing error: ${geofencingEvent.errorCode}")
            return
        }
        
        val geofenceTransition = geofencingEvent.geofenceTransition
        
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences ?: return
            
            triggeringGeofences.forEach { geofence ->
                handleGeofenceEnter(context, geofence.requestId)
            }
        }
    }
    
    private fun handleGeofenceEnter(context: Context, geofenceId: String) {
        val database = ReminderDatabase.getDatabase(context)
        val repository = ReminderRepository(database.reminderDao())
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reminder = repository.getReminderByGeofenceId(geofenceId)
                
                reminder?.let {
                    if (it.isActive) {
                        // Show notification
                        NotificationHelper.showReminderNotification(
                            context,
                            it.notificationTitle,
                            it.notificationMessage,
                            it.id
                        )
                        
                        // Optionally deactivate the reminder after triggering
                        // repository.toggleReminderStatus(it.id, false)
                        
                        Log.d(TAG, "Reminder triggered: ${it.title}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling geofence enter", e)
            }
        }
    }
}