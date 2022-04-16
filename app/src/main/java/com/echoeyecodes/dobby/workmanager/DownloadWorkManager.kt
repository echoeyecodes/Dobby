package com.echoeyecodes.dobby.workmanager

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.echoeyecodes.dobby.R
import com.echoeyecodes.dobby.activities.MainActivity
import com.echoeyecodes.dobby.callbacks.downloadmanagercallbacks.DownloadManagerCallbackImpl
import com.echoeyecodes.dobby.repository.FileRepository
import com.echoeyecodes.dobby.utils.DownloadManager
import com.echoeyecodes.dobby.utils.DownloadStatus
import kotlinx.coroutines.*

class DownloadWorkManager(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    private val downloadManager = DownloadManager.getInstance(context)
    private val fileRepository = FileRepository(context)
    private var shouldTerminate = false

    companion object {
        const val NOTIFICATION_ID = 4523156
        const val NOTIFICATION_CHANNEL_ID = "DOWNLOADING_DATA"
    }

    private fun showNotification(intent: PendingIntent): Notification {
        val builder = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Downloading files")
            .setSmallIcon(R.drawable.ic_logo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(intent)
            .setContentText("Your files are getting downloaded")
        return builder.build()
    }

    private suspend fun startDownload() {
        withContext(Dispatchers.IO) {
            val downloads = fileRepository.getActiveDownloads()
            val iterator = downloads.iterator()
            while (iterator.hasNext()) {
                val download = iterator.next()
                downloadManager.addTask(download.id, download.uri)
            }
            if (!downloadManager.hasActiveDownloads()) {
                terminateService()
            }
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val pendingIntent = Intent(applicationContext, MainActivity::class.java).let {
            PendingIntent.getActivity(applicationContext, 0, it, 0)
        }
        return ForegroundInfo(NOTIFICATION_ID, showNotification(pendingIntent))
    }

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            downloadManager.addDownloadManagerCallback(downloadManagerCallback)
            while (!shouldTerminate) {
                delay(3000)
                startDownload()
            }
            downloadManager.removeDownloadManagerCallback(downloadManagerCallback)
            Result.success()
        }
    }

    private fun terminateService() {
        shouldTerminate = true
    }

    private val downloadManagerCallback = object : DownloadManagerCallbackImpl {
        override fun onDownloadStarted(id: String) {
            super.onDownloadStarted(id)
            fileRepository.updateFileStatus(id, DownloadStatus.DOWNLOADING)
        }

        override fun onPathDetermined(id: String, path: String) {
            super.onPathDetermined(id, path)
            fileRepository.updateFilePath(id, path)
        }

        override fun onDownloadComplete(id: String) {
            super.onDownloadComplete(id)
            fileRepository.updateFileStatus(id, DownloadStatus.COMPLETE)
        }

        override fun onDownloadCancelled(id: String, exception: Exception) {
            super.onDownloadCancelled(id, exception)
            if (exception is CancellationException) {
                fileRepository.updateFileStatus(id, DownloadStatus.CANCELLED)
            } else {
                fileRepository.updateFileStatus(id, DownloadStatus.FAILED)
            }
        }

        override fun onSizeDetermined(id: String, size: Long) {
            super.onSizeDetermined(id, size)
            fileRepository.updateFileSize(id, size)
        }

        override fun onDownloadsFinished() {
            super.onDownloadsFinished()
            terminateService()
        }
    }

}