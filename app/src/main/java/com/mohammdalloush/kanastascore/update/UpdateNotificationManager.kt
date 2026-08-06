package com.mohammdalloush.kanastascore.update

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object UpdateNotificationManager {
    private const val CHANNEL_ID = "app_updates"
    private const val NOTIFICATION_ID = 7401

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "تحديثات التطبيق",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "إشعارات توفر إصدار جديد من التطبيق"
            }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    fun showUpdateAvailable(context: Context, release: UpdateRelease): Boolean {
        createChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        val notificationManager = NotificationManagerCompat.from(context)
        if (!notificationManager.areNotificationsEnabled()) return false

        val targetUrl = release.downloadUrl ?: release.releaseUrl
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("يتوفر تحديث جديد")
            .setContentText("الإصدار ${release.tagName} جاهز للتنزيل")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    release.notes.ifBlank { "اضغط لفتح صفحة الإصدار وتنزيل التحديث." },
                ),
            )
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
        return true
    }
}
