package com.example.scam

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.webkit.JavascriptInterface
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.net.URL
import kotlin.concurrent.thread

class WebAppInterface(private val context: Context, private val channelId: String) {

    @JavascriptInterface
    fun postNotification(title: String?, body: String?, iconUrl: String?) {
        val t = title ?: ""
        val b = body ?: ""

        // Создаём уведомление в фоновом потоке (загрузка иконки может быть медленной)
        thread {
            var largeIcon: Bitmap? = null
            try {
                if (!iconUrl.isNullOrEmpty()) {
                    val url = URL(iconUrl)
                    val conn = url.openConnection()
                    conn.connectTimeout = 3000
                    conn.readTimeout = 3000
                    val stream = conn.getInputStream()
                    largeIcon = BitmapFactory.decodeStream(stream)
                    stream.close()
                }
            } catch (_: Exception) {
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                context, 
                0, 
                intent, 
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
            )

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(t)
                .setContentText(b)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            if (largeIcon != null) {
                builder.setLargeIcon(largeIcon)
            }

            try {
                with(NotificationManagerCompat.from(context)) {
                    // Проверяем разрешение перед отправкой уведомления (Android 13+)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), builder.build())
                        }
                    } else {
                        // Для версий ниже Android 13 разрешение не требуется
                        notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), builder.build())
                    }
                }
            } catch (e: SecurityException) {
                // Если разрешение было отозвано в процессе выполнения, молча игнорируем
            } catch (_: Exception) {
            }
        }
    }
}
