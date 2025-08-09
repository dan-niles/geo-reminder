package com.example.georeminder.data

import kotlinx.coroutines.flow.Flow

class ReminderRepository(
    private val reminderDao: ReminderDao
) {
    
    fun getAllReminders(): Flow<List<ReminderEntity>> = reminderDao.getAllReminders()
    
    fun getActiveReminders(): Flow<List<ReminderEntity>> = reminderDao.getActiveReminders()
    
    suspend fun getReminderById(id: Long): ReminderEntity? = reminderDao.getReminderById(id)
    
    suspend fun getReminderByGeofenceId(geofenceId: String): ReminderEntity? = 
        reminderDao.getReminderByGeofenceId(geofenceId)
    
    suspend fun insertReminder(reminder: ReminderEntity): Long = reminderDao.insertReminder(reminder)
    
    suspend fun updateReminder(reminder: ReminderEntity) = reminderDao.updateReminder(reminder)
    
    suspend fun deleteReminder(reminder: ReminderEntity) = reminderDao.deleteReminder(reminder)
    
    suspend fun deleteReminderById(id: Long) = reminderDao.deleteReminderById(id)
    
    suspend fun toggleReminderStatus(id: Long, isActive: Boolean) = 
        reminderDao.updateReminderStatus(id, isActive)
    
    suspend fun cleanupOldReminders() {
        val oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        reminderDao.deleteOldInactiveReminders(oneWeekAgo)
    }
}