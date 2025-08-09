package com.example.georeminder.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.georeminder.R
import com.example.georeminder.ui.MainActivity

object NotificationHelper {
    
    private const val REMINDER_CHANNEL_ID = "reminder_notifications"
    private const val REMINDER_CHANNEL_NAME = "Location Reminders"
    private const val REMINDER_NOTIFICATION_ID_BASE = 2000
    
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Reminder notification channel
            val reminderChannel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                REMINDER_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when you approach your destination"
                enableVibration(true)
                setShowBadge(true)
            }
            
            notificationManager.createNotificationChannel(reminderChannel)
        }
    }
    
    fun showReminderNotification(
        context: Context,
        title: String,
        message: String,
        reminderId: Long
    ) {
        if (!LocationPermissionHelper.hasNotificationPermission(context)) {
            return
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("reminder_id", reminderId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        val notificationBuilder = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_location_reminder)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVibrate(longArrayOf(0, 1000, 500, 1000))
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
        
        val notificationManager = NotificationManagerCompat.from(context)
        val notificationId = REMINDER_NOTIFICATION_ID_BASE + reminderId.toInt()
        
        try {
            notificationManager.notify(notificationId, notificationBuilder.build())
        } catch (e: SecurityException) {
            // Handle notification permission error
        }
    }
    
    fun cancelReminderNotification(context: Context, reminderId: Long) {
        val notificationManager = NotificationManagerCompat.from(context)
        val notificationId = REMINDER_NOTIFICATION_ID_BASE + reminderId.toInt()
        notificationManager.cancel(notificationId)
    }
}