package com.udacity.util

import android.app.NotificationManager
import android.content.Context
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.udacity.R

class NotificationDownload(private val context: Context, private val lifecycle: Lifecycle):
    LifecycleObserver {
    // Register NotificationUtils is Observer
    init {
        lifecycle.addObserver(this)
    }

    fun notify(fileName: String, downloadStatus: DownloadStatus) {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            Toast.makeText(
                context,
                context.getString(R.string.download_completed),
                Toast.LENGTH_SHORT
            ).show();
        }
        with(context.applicationContext) {
            // Send notification
            getNotificationManager().run {
                createDownloadStatusChannel(applicationContext)
                sendNotification(
                    fileName,
                    applicationContext,
                    downloadStatus
                )
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun unregisterObserver() = lifecycle.removeObserver(this)
}

fun Context.getNotificationManager(): NotificationManager = ContextCompat.getSystemService(
    this,
    NotificationManager::class.java
) as NotificationManager


