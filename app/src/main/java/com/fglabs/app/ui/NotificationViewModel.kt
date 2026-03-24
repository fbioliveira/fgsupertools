package com.fglabs.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fglabs.app.data.AppDatabase
import com.fglabs.app.data.NotificationEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class NotificationViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).notificationDao()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val allNotifications: StateFlow<List<NotificationEntity>> = combine(
        dao.getAllNotifications(),
        _searchQuery
    ) { notifications, query ->
        // No longer filtering by title here to see everything
        if (query.isBlank()) {
            notifications
        } else {
            notifications.filter {
                it.appName.contains(query, ignoreCase = true) || 
                (it.title?.contains(query, ignoreCase = true) == true) ||
                (it.content?.contains(query, ignoreCase = true) == true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notificationCount: StateFlow<Int> = dao.getNotificationCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val unreadCount: StateFlow<Int> = dao.getUnreadNotificationCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val unreadPackageNames: StateFlow<List<String>> = dao.getUnreadPackageNames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun markAsRead(notification: NotificationEntity) {
        if (!notification.isRead) {
            viewModelScope.launch {
                dao.markAsRead(notification.id)
            }
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            val notifications = dao.getAllNotifications().firstOrNull()
            notifications?.forEach { notif ->
                notif.imagePath?.let { path ->
                    val file = File(path)
                    if (file.exists()) file.delete()
                }
            }
            dao.deleteAll()
        }
    }

    fun deleteNotification(notification: NotificationEntity) {
        viewModelScope.launch {
            notification.imagePath?.let { path ->
                val file = File(path)
                if (file.exists()) file.delete()
            }
            dao.delete(notification)
        }
    }
}
