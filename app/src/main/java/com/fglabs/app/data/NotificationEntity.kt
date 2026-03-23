package com.fglabs.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notifications",
    indices = [Index(value = ["packageName", "notificationId"], unique = true)]
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val notificationId: Int = 0,
    val appName: String,
    val title: String?,
    val content: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val packageName: String,
    val imagePath: String? = null,
    val bigPicturePath: String? = null,
    val isRead: Boolean = false
)
