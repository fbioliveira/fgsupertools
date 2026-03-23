package com.fglabs.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert
    suspend fun insert(notification: NotificationEntity)

    @Query("DELETE FROM notifications")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(notification: NotificationEntity)
    
    @Query("SELECT COUNT(*) FROM notifications")
    fun getNotificationCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0 AND title IS NOT NULL AND title != ''")
    fun getUnreadNotificationCount(): Flow<Int>

    @Update
    suspend fun update(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)
}
