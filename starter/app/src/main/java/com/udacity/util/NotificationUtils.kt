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
import com.udacity.DetailActivity.Companion.bundleExtrasOf
import com.udacity.MainActivity
import com.udacity.R

private val DOWNLOAD_COMPLETED_ID = 1
private val NOTIFICATION_REQUEST_CODE = 1

/**
 * Builds and delivers the notification.
 *
 * @param context, activity context.
 */
fun NotificationManager.sendNotification(fileName: String, applicationContext: Context, downloadStatus: DownloadStatus
) {
    // Create the content intent for the notification, which launches
    // this activity
    //create an intent to pass to the DetailActivity and pass the accompanying data
    // so that the activity can display detailed information about the downloaded file.
    val contentIntent = Intent(applicationContext, DetailActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtras(bundleExtrasOf(fileName, downloadStatus))
    }
    val contentPendingIntent = PendingIntent.getActivity(
        applicationContext,
        NOTIFICATION_REQUEST_CODE,
        contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    // Build the notification
    val builderAction = NotificationCompat.Action.Builder(
        null,
        applicationContext.getString(R.string.notification_action),
        contentPendingIntent
    ).build()

    // Create chanel notification
    NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.notification_channel_id)
    )

        .setSmallIcon(R.drawable.ic_assistant_black_24dp)
        .setContentTitle(applicationContext.getString(R.string.notification_title))
        .setContentText(applicationContext.getString(R.string.notification_description))
        // (For Android 8.0 and higher, you must instead set the channel importance—shown in the next section.)
        // High priority makes a sound and appears as a heads up notification
        // Default priority makes a sound
        // Low priority makes no sound
        // Min priority makes no sound and does not appear in the status bar
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(contentPendingIntent)
        .setAutoCancel(true)
        .addAction(builderAction)
            .run {
                notify(DOWNLOAD_COMPLETED_ID, this.build())
            }
}

@SuppressLint("NewAPI")
fun NotificationManager.createDownloadStatusChannel(context: Context) {
    Build.VERSION.SDK_INT.takeIf { it >= Build.VERSION_CODES.O }?.run {
        NotificationChannel(
            context.getString(R.string.notification_channel_id),
            context.getString(R.string.notification_title),
            // This parameter determines how to interrupt the user for any notification that
            // belongs to this channel—though you must also set the priority with
            // NotificationCompat.Builder.setPriority()
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_description)
            setShowBadge(true)
        }.run {
            createNotificationChannel(this)
        }
    }
}


