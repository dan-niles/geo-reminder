package com.example.georeminder.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "reminders")
@Parcelize
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String?,
    val latitude: Double,
    val longitude: Double,
    val radius: Float = 100f, // Default 100 meter radius
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val notificationTitle: String = title,
    val notificationMessage: String = description ?: "You're approaching your destination!",
    val geofenceId: String = "geofence_$id"
) : Parcelable

enum class ReminderStatus {
    ACTIVE,
    TRIGGERED,
    DISABLED
}