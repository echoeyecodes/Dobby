package com.echoeyecodes.dobby.callbacks.downloadmanagercallbacks

interface DownloadManagerCallbackImpl : DownloadManagerCallback {

    override fun onDownloadStarted(id: String) {

    }

    override fun onPathDetermined(id: String, path: String) {

    }

    override fun onDownloadProgress(id: String, bytesRead: Long, size: Long) {

    }

    override fun onSizeDetermined(id: String, size: Long) {

    }

    override fun onDownloadComplete(id: String) {

    }

    override fun onDownloadCancelled(id: String, exception: Exception) {

    }

    override fun onDownloadsFinished() {

    }
}