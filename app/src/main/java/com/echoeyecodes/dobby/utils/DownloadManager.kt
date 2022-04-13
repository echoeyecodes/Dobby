package com.echoeyecodes.dobby.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.echoeyecodes.dobby.R
import com.echoeyecodes.dobby.callbacks.downloadmanagercallbacks.DownloadManagerCallback
import kotlinx.coroutines.*
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.Runnable
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.math.max

class DownloadManager(private val context: Context) {
    private val taskCache = HashMap<String, Job>()
    private val callbacks = ArrayList<DownloadManagerCallback>()

    fun addDownloadManagerCallback(callback: DownloadManagerCallback) {
        this.callbacks.add(callback)
    }

    companion object {
        private var instance: DownloadManager? = null
        fun getInstance(context: Context) = instance ?: synchronized(this) {
            val newInstance = DownloadManager(context)
            instance = newInstance
            return newInstance
        }

        const val MAXIMUM_POOL_SIZE = 4
        const val MINIMUM_POOL_SIZE = 4
    }

    private suspend fun downloadFile(id: String, downloadUrl: String) {
        withContext(Dispatchers.IO) {
            var inputStream: BufferedInputStream? = null
            var outputStream: OutputStream? = null
            var connection: HttpURLConnection? = null
            callbacks.forEach { it.onDownloadStarted(id) }

            try {
                val url = URL(downloadUrl)
                connection = url.openConnection() as HttpURLConnection
                connection.connect()
                inputStream = BufferedInputStream(url.openStream())

                val fileStream = createFile(connection.contentType)
                val itemUri = fileStream.first
                AndroidUtilities.log(itemUri.toString())

                if (itemUri != null) {
                    callbacks.forEach { it.onPathDetermined(id, itemUri.toString()) }

                    outputStream = fileStream.second
                    outputStream?.let {
                        val fileLength = max(connection.contentLength, 1)

                        val data = ByteArray(1024)
                        var total = 0L
                        var count: Int

                        while ((inputStream.read(data).also { count = it }) != -1) {
                            ensureActive()
                            total += count

                            outputStream.write(data, 0, count)
                            callbacks.forEach {
                                it.onDownloadProgress(
                                    id,
                                    total,
                                    fileLength.toLong()
                                )
                            }
                        }
                        callbacks.forEach { it.onDownloadComplete(id) }
                    }
                }
            } catch (exception: Exception) {
                callbacks.forEach { it.onDownloadCancelled(id, exception) }
            } finally {
                connection?.disconnect()
                outputStream?.close()
                inputStream?.close()
                cancelTask(id)
                endDownload()
            }
        }
    }

    private fun createFile(filetype: String): Pair<Uri?, OutputStream?> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues()
            contentValues.put(
                MediaStore.MediaColumns.DISPLAY_NAME,
                context.getString(R.string.app_name).plus("_${System.currentTimeMillis()}")
            )
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, filetype)
            contentValues.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DCIM.plus("/").plus(context.getString(R.string.app_name))
            )
            val uri = if (filetype.contains("video")) {
                context.contentResolver.insert(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )
            } else {
                context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )
            }
            val outputStream = uri?.let { context.contentResolver.openOutputStream(it) }
            Pair(uri, outputStream)
        } else {
            createFileLegacy(filetype)
        }
    }

    private fun createFileLegacy(filetype: String): Pair<Uri, OutputStream> {
        val file =
            File(Environment.getExternalStorageDirectory(), context.getString(R.string.app_name))
        if (!file.exists()) {
            file.mkdir()
        }
        val extension = if (filetype.contains("video")) {
            ".mp4"
        } else {
            ".jpg"
        }
        val pathFile = File(
            file,
            context.getString(R.string.app_name).plus("_${System.currentTimeMillis()}")
                .plus(extension)
        ).apply {
            if (exists()) {
                delete()
            }
        }
        val outputStream = FileOutputStream(pathFile)
        return Pair(pathFile.absolutePath.toUri(), outputStream)
    }

    fun addTask(id: String, url: String) {
        val key = id
        if (taskCache[key] == null) {
            val job = CoroutineScope(Dispatchers.IO).launch {
                downloadFile(id, url)
            }
            taskCache[key] = job
        }
    }

    fun cancelTask(key: String) {
        val job = taskCache[key]
        if (job != null) {
            job.cancel()
            taskCache.remove(key)
        }
    }

    private fun isExecutionComplete(): Boolean {
        return taskCache.size == 0
    }

    fun hasActiveDownloads():Boolean{
        return taskCache.size > 0
    }

    private fun endDownload() {
        if (isExecutionComplete()) {
            callbacks.forEach {
                it.onDownloadsFinished()
            }
        }
    }

    fun removeDownloadManagerCallback(callback: DownloadManagerCallback) {
        this.callbacks.remove(callback)
    }
}