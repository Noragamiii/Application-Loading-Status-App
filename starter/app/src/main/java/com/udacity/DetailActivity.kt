package com.udacity

import android.os.Bundle
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.udacity.databinding.ActivityDetailBinding
import com.udacity.databinding.ContentDetailBinding
import com.udacity.util.DownloadStatus
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : AppCompatActivity() {
    private val unknown by lazy { getString(R.string.unknown) }

    private val fileName by lazy {
        intent?.extras?.getString(EXTRA_FILE_NAME, unknown) ?: unknown
    }
    private val downloadStatus by lazy {
        intent?.extras?.getString(EXTRA_DOWNLOAD_STATUS, unknown) ?: unknown
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityDetailBinding>(this, R.layout.activity_detail)
            .apply {
                setSupportActionBar(toolbar)
                detailContent.initializeView()
            }
    }

    private fun ContentDetailBinding.initializeView() {
        fileNameText.text = fileName
        downloadStatusText.text = downloadStatus
        okButton.setOnClickListener { finish() }
        changeViewForDownloadStatus()
    }

    private fun ContentDetailBinding.changeViewForDownloadStatus() {
        when (downloadStatusText.text) {
            DownloadStatus.SUCCESSFUL.statusText -> {
                changeDownloadStatusImageTo(R.drawable.ic_download_done)
            }
            DownloadStatus.FAILED.statusText -> {
                changeDownloadStatusImageTo(R.drawable.ic_download_error)
            }
        }
    }


    private fun ContentDetailBinding.changeDownloadStatusImageTo(@DrawableRes imageRes: Int) {
        downloadStatusImage.setImageResource(imageRes)
    }

    companion object {
        private const val EXTRA_FILE_NAME = "${BuildConfig.APPLICATION_ID}.FILE_NAME"
        private const val EXTRA_DOWNLOAD_STATUS = "${BuildConfig.APPLICATION_ID}.DOWNLOAD_STATUS"

        /**
         * Creates a [Bundle] with given parameters and pass as data to [DetailActivity].
         */
        fun bundleExtrasOf(
            fileName: String,
            downloadStatus: DownloadStatus
        ) = bundleOf(
            EXTRA_FILE_NAME to fileName,
            EXTRA_DOWNLOAD_STATUS to downloadStatus.statusText
        )
    }
}
