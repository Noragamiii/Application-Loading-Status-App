package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil.setContentView
import com.udacity.util.DownloadStatus
import com.udacity.util.NotificationDownload
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import com.udacity.databinding.ActivityMainBinding
import com.udacity.databinding.ContentMainBinding
import com.udacity.loading.ButtonState
import com.udacity.loading.LoadingButton
import kotlinx.android.synthetic.main.content_main.custom_button
import kotlinx.android.synthetic.main.content_main.view.*


class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding

    private var downloadID: Long = NO_DOWNLOAD
    private var downloadContentObserver: ContentObserver? = null
    private var downloadNotificator: NotificationDownload? = null
    private var downloadFileName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView<ActivityMainBinding>(this, R.layout.activity_main)
            .apply {
                viewBinding = this
                setSupportActionBar(toolbar)
                registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                )
                onButtonClicked()
            }
    }

    private fun ActivityMainBinding.onButtonClicked() {
        with(contentMain) {
            customButton.setOnClickListener {
                when (downloadOptionRadioGroup.checkedRadioButtonId) {
                    View.NO_ID ->
                        Toast.makeText(
                            this@MainActivity,
                            "Please select the file to download",
                            Toast.LENGTH_SHORT
                        ).show()
                    else -> {
                        downloadFileName = findViewById<RadioButton>(downloadOptionRadioGroup.checkedRadioButtonId).text.toString()
                        download()
                    }
                }
            }
        }
    }

    // BroadcastReceiver is initialized to handle download completion event in DownloadManager
    // If download done send notification
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            id?.let {
                // Status download
                val downloadStatus = getDownloadManager().queryStatus(it)
                unregisterDownloadContentObserver()
                downloadStatus.takeIf { status -> status != DownloadStatus.UNKNOWN }?.run {
                    getDownloadNotificator().notify(downloadFileName, downloadStatus)
                }
            }
        }
    }

    // Query Status Download
    private fun DownloadManager.queryStatus(id: Long): DownloadStatus {
        query(DownloadManager.Query().setFilterById(id)).use {
            with(it) {
                if (this != null && moveToFirst()) {
                    return when (getInt(getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                        DownloadManager.STATUS_SUCCESSFUL -> DownloadStatus.SUCCESSFUL
                        DownloadManager.STATUS_FAILED -> DownloadStatus.FAILED
                        else -> DownloadStatus.UNKNOWN
                    }
                }
                return DownloadStatus.UNKNOWN
            }
        }
    }

    private fun unregisterDownloadContentObserver() {
        downloadContentObserver?.let {
            contentResolver.unregisterContentObserver(it)
            downloadContentObserver = null
        }
    }

    private fun getDownloadNotificator(): NotificationDownload = when (downloadNotificator) {
        null -> NotificationDownload(this, lifecycle).also { downloadNotificator = it }
        else -> downloadNotificator!!
    }

    private fun download() {
        with(getDownloadManager()) {
            downloadID.takeIf { it != NO_DOWNLOAD }?.run {
                remove(downloadID)
                unregisterDownloadContentObserver()
                downloadID = NO_DOWNLOAD
            }

            val request =
                DownloadManager.Request(Uri.parse(URL))
                    .setTitle(getString(R.string.app_name))
                    .setDescription(getString(R.string.app_description))
                    .setRequiresCharging(false)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)

            downloadID = enqueue(request)// enqueue puts the download request in the queue.

            createAndRegisterDownloadContentObserver()
        }
    }

    private fun DownloadManager.createAndRegisterDownloadContentObserver() {
        object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                downloadContentObserver?.run { queryProgress() }
            }
        }.also {
            downloadContentObserver = it
            contentResolver.registerContentObserver(
                "content://downloads/my_downloads".toUri(),
                true,
                downloadContentObserver!!
            )
        }
    }

    private fun DownloadManager.queryProgress() {
        query(DownloadManager.Query().setFilterById(downloadID)).use {
            with(it) {
                if (this != null && moveToFirst()) {
                    val id = getInt(getColumnIndex(DownloadManager.COLUMN_ID))
                    when (getInt(getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                        DownloadManager.STATUS_FAILED -> {
                            viewBinding.contentMain.customButton.changeButtonState(ButtonState.Completed)
                        }
                        DownloadManager.STATUS_RUNNING -> {
                            viewBinding.contentMain.customButton.changeButtonState(ButtonState.Loading)
                        }
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            viewBinding.contentMain.customButton.changeButtonState(ButtonState.Completed)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        unregisterDownloadContentObserver()
        downloadNotificator = null
    }


    companion object {
        private const val URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val NO_DOWNLOAD = 0L
    }
}

fun Context.getDownloadManager(): DownloadManager = ContextCompat.getSystemService(
    this,
    DownloadManager::class.java
) as DownloadManager
