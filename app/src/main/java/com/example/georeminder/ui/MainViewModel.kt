package com.example.georeminder.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.georeminder.data.ReminderEntity
import com.example.georeminder.data.ReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: ReminderRepository
) : ViewModel() {
    
    private val _reminders = MutableStateFlow<List<ReminderEntity>>(emptyList())
    val reminders: StateFlow<List<ReminderEntity>> = _reminders.asStateFlow()
    
    init {
        loadReminders()
    }
    
    private fun loadReminders() {
        viewModelScope.launch {
            repository.getAllReminders().collect { reminderList ->
                _reminders.value = reminderList
            }
        }
    }
    
    fun toggleReminderStatus(reminderId: Long, isActive: Boolean) {
        viewModelScope.launch {
            repository.toggleReminderStatus(reminderId, isActive)
        }
    }
    
    fun deleteReminder(reminderId: Long) {
        viewModelScope.launch {
            repository.deleteReminderById(reminderId)
        }
    }
    
    fun cleanupOldReminders() {
        viewModelScope.launch {
            repository.cleanupOldReminders()
        }
    }
}