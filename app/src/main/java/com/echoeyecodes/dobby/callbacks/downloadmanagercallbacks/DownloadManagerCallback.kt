package com.echoeyecodes.dobby.callbacks.downloadmanagercallbacks

interface DownloadManagerCallback {
    fun onDownloadStarted(id:String)
    fun onPathDetermined(id:String, path:String)
    fun onDownloadProgress(id:String, bytesRead:Long, total:Long)
    fun onDownloadComplete(id:String)
    fun onDownloadCancelled(id:String, exception: Exception)
    fun onDownloadsFinished()
}