package com.udacity

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.udacity.loading.Status
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    private val unknown by lazy { getString(R.string.unknown) }

    private val fileName: String by lazy {
        intent?.extras?.getString(FILE_NAME, unknown) ?: unknown
    }
    private val downloadStatus: String by lazy {
        intent?.extras?.getString(DOWNLOAD_STATUS, unknown) ?: unknown
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)
        file_name_text.text = fileName
        status_text.text = downloadStatus
        val okButtonClickListener = View.OnClickListener {
            finish()
        }
        ok_button.setOnClickListener(okButtonClickListener)
    }

    companion object {
        private const val FILE_NAME = "FILE_NAME"
        private const val DOWNLOAD_STATUS = "DOWNLOAD_STATUS"

        fun bundleSharingData(fileName: String, downloadStatus: Status): Bundle {
            val extras = Bundle()
            extras.putString(FILE_NAME, fileName)
            extras.putString(DOWNLOAD_STATUS, downloadStatus.status)
            return extras
        }
    }
}