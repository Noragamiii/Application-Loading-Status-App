package com.udacity.util

// DownLoad Status
enum class DownloadStatus(val statusText: String) {
    SUCCESSFUL("Successful"),
    FAILED("Failed"),
    UNKNOWN("Unknown")
}