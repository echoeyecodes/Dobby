package com.echoeyecodes.dobby.callbacks.downloadmanagercallbacks

interface DownloadManagerCallback {
    fun onDownloadStarted(id: String)
    fun onPathDetermined(id: String, path: String)
    fun onDownloadProgress(id: String, bytesRead: Long, size: Long)
    fun onDownloadComplete(id: String)
    fun onDownloadCancelled(id: String, exception: Exception)
    fun onDownloadsFinished()
    fun onSizeDetermined(id: String, size: Long)
}