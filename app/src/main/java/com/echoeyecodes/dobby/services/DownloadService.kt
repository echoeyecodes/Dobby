package com.echoeyecodes.dobby.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.echoeyecodes.dobby.R
import com.echoeyecodes.dobby.activities.MainActivity
import com.echoeyecodes.dobby.callbacks.downloadmanagercallbacks.DownloadManagerCallback
import com.echoeyecodes.dobby.repository.FileRepository
import com.echoeyecodes.dobby.utils.AndroidUtilities
import com.echoeyecodes.dobby.utils.DownloadManager
import com.echoeyecodes.dobby.utils.DownloadStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DownloadService : Service(), DownloadManagerCallback {
    private lateinit var downloadManager: DownloadManager
    private lateinit var fileRepository: FileRepository


    companion object {
        const val NOTIFICATION_ID = 4523156
        const val NOTIFICATION_CHANNEL_ID = "DOWNLOADING_DATA"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    fun terminateService() {
        AndroidUtilities.log("service stoppped")
        stopSelf()
    }

    private fun showNotification(intent: PendingIntent): Notification {
        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Downloading files")
            .setSmallIcon(R.drawable.ic_logo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(intent)
            .setContentText("Your files are getting downloaded")
        return builder.build()
    }

    override fun onCreate() {
        super.onCreate()
        val pendingIntent = Intent(applicationContext, MainActivity::class.java).let {
            PendingIntent.getActivity(applicationContext, 0, it, 0)
        }
        startForeground(NOTIFICATION_ID, showNotification(pendingIntent))

        fileRepository = FileRepository(applicationContext)
        downloadManager = DownloadManager.getInstance(applicationContext)
        downloadManager.addDownloadManagerCallback(this)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        CoroutineScope(Dispatchers.IO).launch {
            val downloads = fileRepository.getActiveDownloads()
            if (downloads.isNotEmpty()) {
                val iterator = downloads.iterator()
                while (iterator.hasNext()) {
                    val download = iterator.next()
                    downloadManager.addTask(download.id, download.uri)
                }
            } else {
                terminateService()
            }
        }
        return START_REDELIVER_INTENT
    }

    override fun onDownloadStarted(id: String) {
        fileRepository.updateFileStatus(id, DownloadStatus.DOWNLOADING)
    }

    override fun onPathDetermined(id: String, path: String) {
        fileRepository.updateFilePath(id, path)
    }

    override fun onDownloadProgress(id: String, bytesRead: Long, total: Long) {
        fileRepository.updateFileProgress(id, bytesRead, total)
    }

    override fun onDownloadComplete(id: String) {
        fileRepository.updateFileStatus(id, DownloadStatus.COMPLETE)
    }

    override fun onDownloadCancelled(id: String, exception: Exception) {
        if (exception is CancellationException) {
            fileRepository.updateFileStatus(id, DownloadStatus.CANCELLED)
        } else {
            fileRepository.updateFileStatus(id, DownloadStatus.FAILED)
        }
    }

    override fun onDownloadsFinished() {
        terminateService()
    }

    override fun onDestroy() {
        downloadManager.removeDownloadManagerCallback(this)
        super.onDestroy()
    }

}