package com.example.georeminder.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    
    @Query("SELECT * FROM reminders ORDER BY createdAt DESC")
    fun getAllReminders(): Flow<List<ReminderEntity>>
    
    @Query("SELECT * FROM reminders WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveReminders(): Flow<List<ReminderEntity>>
    
    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): ReminderEntity?
    
    @Query("SELECT * FROM reminders WHERE geofenceId = :geofenceId")
    suspend fun getReminderByGeofenceId(geofenceId: String): ReminderEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long
    
    @Update
    suspend fun updateReminder(reminder: ReminderEntity)
    
    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)
    
    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)
    
    @Query("UPDATE reminders SET isActive = :isActive WHERE id = :id")
    suspend fun updateReminderStatus(id: Long, isActive: Boolean)
    
    @Query("DELETE FROM reminders WHERE isActive = 0 AND createdAt < :beforeDate")
    suspend fun deleteOldInactiveReminders(beforeDate: Long)
}