package com.udacity.util

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.udacity.DetailActivity
import com.udacity.DetailActivity.Companion.bundleSharingData
import com.udacity.R
import com.udacity.loading.Status

private val DOWNLOAD_ID = 0
private val NOTIFICATION_ID = 0

const val CHANNEL_ID = "channel_id"
const val CHANNEL_NAME = "LoadingApp"

fun NotificationManager.sendNotification(fileName: String, applicationContext: Context, downloadStatus: Status
) {
    // Create the content intent for the notification, which launches
    // this activity
    //create an intent to pass to the DetailActivity and pass the accompanying data
    // so that the activity can display detailed information about the downloaded file.
    val contentIntent = Intent(applicationContext, DetailActivity::class.java)
    contentIntent.putExtras(bundleSharingData(fileName, downloadStatus))
    // https://developer.android.com/develop/ui/views/notifications/build-notification#builder
    val contentPendingIntent = PendingIntent.getActivity(
        applicationContext,
        NOTIFICATION_ID,
        contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    // Build the notification
    // https://developer.android.com/develop/ui/views/notifications/build-notification#builder
    val builderAction = NotificationCompat.Action.Builder(
        null,
        applicationContext.getString(R.string.notification_action),
        contentPendingIntent
    ).build()

    // Create chanel notification
    // https://developer.android.com/develop/ui/views/notifications/build-notification#builder
    NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.notification_channel_id)
    )

        .setSmallIcon(R.drawable.ic_assistant_black_24dp)
        .setContentTitle(applicationContext.getString(R.string.notification_title))
        .setContentText(applicationContext.getString(R.string.notification_description))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(contentPendingIntent)
        .setAutoCancel(true)
        .addAction(builderAction)
            .run {
                notify(DOWNLOAD_ID, this.build())
            }
}
// https://developer.android.com/develop/ui/views/notifications/build-notification#builder
fun createDownloadStatusChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)

        val channelId = CHANNEL_ID
        val channelName = CHANNEL_NAME
        val channelDescription = "Channel for download notifications"
        val channelImportance = NotificationManager.IMPORTANCE_HIGH

        val channel = NotificationChannel(channelId, channelName, channelImportance).apply {
            description = channelDescription
            setShowBadge(true)
        }
        channel.description = "DownLoad Complete"
        notificationManager.createNotificationChannel(channel)
    }
}


