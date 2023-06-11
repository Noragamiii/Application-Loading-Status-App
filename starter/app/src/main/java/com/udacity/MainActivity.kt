package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.udacity.databinding.ActivityMainBinding
import com.udacity.loading.ButtonState
import com.udacity.loading.Status
import com.udacity.util.createDownloadStatusChannel
import com.udacity.util.sendNotification
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding

    private var downloadID: Long = 0
    private var fileName = ""
    private var downloadStatus = Status.UNKNOWN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        setSupportActionBar(viewBinding.toolbar)
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        setupButtonClickListener()
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadID == id) {
                custom_button.changeButtonState(ButtonState.Completed)
                downloadStatus = Status.SUCCESS
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
    }

    private fun download() {
        val request = DownloadManager.Request(Uri.parse(URL))
            .setTitle(getString(R.string.app_name))
            .setDescription(getString(R.string.app_description))
            .setRequiresCharging(false)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID = downloadManager.enqueue(request)
        queryRequest(downloadID, downloadManager)
    }

    private fun setupButtonClickListener() {
        val customButton = viewBinding.contentMain.customButton
        val downloadOptionRadioGroup = viewBinding.contentMain.downloadOptionRadioGroup

        customButton.setOnClickListener {
            val checkedRadioButtonId = downloadOptionRadioGroup.checkedRadioButtonId
            if (checkedRadioButtonId == -1) {
                Toast.makeText(
                    this@MainActivity,
                    R.string.message,
                    Toast.LENGTH_LONG
                ).show()
            } else {
                custom_button.changeButtonState(ButtonState.Loading)
                fileName =
                    findViewById<RadioButton>(viewBinding.contentMain.downloadOptionRadioGroup.checkedRadioButtonId).text.toString()
                download()
            }
        }
    }

    private fun queryRequest(downloadID: Long, downloadManager: DownloadManager) {
        val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadID))
            if (cursor.moveToFirst()) {
                when (cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) {
                    DownloadManager.STATUS_FAILED -> {
                        custom_button.changeButtonState(ButtonState.Completed)
                        downloadStatus = Status.FAILURE
                    }
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        custom_button.changeButtonState(ButtonState.Completed)
                        downloadStatus = Status.SUCCESS
                    }
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    companion object {
        private const val URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
    }
}

fun Context.getNotificationManager(): NotificationManager = ContextCompat.getSystemService(
    this,
    NotificationManager::class.java
) as NotificationManager
