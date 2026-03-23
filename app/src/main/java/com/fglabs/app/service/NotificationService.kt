package com.fglabs.app.service

import android.app.Notification
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Icon
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.fglabs.app.data.AppDatabase
import com.fglabs.app.data.NotificationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class NotificationService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn?.let {
            val packageName = it.packageName
            val notificationId = it.id
            val extras = it.notification.extras
            val title = extras.getString(Notification.EXTRA_TITLE)
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            
            // Avoid capturing notifications from our own app
            if (packageName == applicationContext.packageName) return

            val appLabel = try {
                packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(packageName, 0)
                ).toString()
            } catch (e: Exception) {
                packageName
            }

            // Extract small icon (imagePath)
            val smallBitmap = getBitmapFromExtra(extras, Notification.EXTRA_LARGE_ICON)
                ?: getBitmapFromExtra(extras, Notification.EXTRA_SMALL_ICON)

            // Extract big picture (bigPicturePath)
            val bigBitmap = getBitmapFromExtra(extras, Notification.EXTRA_PICTURE)

            var imagePath: String? = null
            if (smallBitmap != null) {
                imagePath = saveBitmap(smallBitmap, "small_${it.postTime}")
            }

            var bigPicturePath: String? = null
            if (bigBitmap != null) {
                bigPicturePath = saveBitmap(bigBitmap, "big_${it.postTime}")
            }

            val entity = NotificationEntity(
                notificationId = notificationId,
                appName = appLabel,
                title = title,
                content = text,
                packageName = packageName,
                imagePath = imagePath,
                bigPicturePath = bigPicturePath
            )

            serviceScope.launch {
                AppDatabase.getDatabase(applicationContext).notificationDao().insert(entity)
            }
        }
    }

    private fun getBitmapFromExtra(extras: Bundle, key: String): Bitmap? {
        val extra = extras.get(key) ?: return null
        if (extra is Bitmap) return extra
        if (extra is Icon) {
            val drawable = extra.loadDrawable(this) ?: return null
            if (drawable is BitmapDrawable) return drawable.bitmap
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth.coerceAtLeast(1),
                drawable.intrinsicHeight.coerceAtLeast(1),
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }
        return null
    }

    private fun saveBitmap(bitmap: Bitmap, fileName: String): String? {
        val file = File(applicationContext.filesDir, "notif_img_$fileName.png")
        return try {
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }
}
