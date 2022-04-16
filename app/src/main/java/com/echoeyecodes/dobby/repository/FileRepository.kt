package com.echoeyecodes.dobby.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.echoeyecodes.dobby.api.ApiClient
import com.echoeyecodes.dobby.api.dao.DownloadDao
import com.echoeyecodes.dobby.db.dao.FileDatabase
import com.echoeyecodes.dobby.db.models.FileDBModel
import com.echoeyecodes.dobby.models.FileModel
import com.echoeyecodes.dobby.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class FileRepository(private val context: Context) {
    private val remoteDownloadDao =
        ApiClient.getInstance(context).getClient(DownloadDao::class.java)
    private val database = FileDatabase.getInstance(context)
    private val filesDao = database.fileDao()
    private val networkState = MutableLiveData<NetworkState>()

    fun getNetworkState(): LiveData<NetworkState> {
        return networkState
    }

    fun getFilesLiveData(): LiveData<List<FileDBModel>> {
        return filesDao.getFilesLiveData()
    }

    fun getFileLiveData(id: String): LiveData<FileDBModel?> {
        return filesDao.getFileLiveData(id)
    }

    suspend fun getFile(id: String): FileDBModel? {
        return withContext(Dispatchers.IO) { filesDao.getFile(id) }
    }

    fun fetchFile(url: String) {
        networkState.value = NetworkState.LOADING
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val files = remoteDownloadDao.getDownload(url)
                filesDao.addItems(files.map { it.toFileDBModel() })
                ServiceUtil.startDownloadService(context)
                networkState.postValue(NetworkState.COMPLETE)
            } catch (exception: Exception) {
                networkState.postValue(NetworkState.ERROR)
                AndroidUtilities.log(exception.message.toString())
            }
        }
    }

    private fun FileModel.toFileDBModel(): FileDBModel {
        return FileDBModel(
            UUID.randomUUID().toString(),
            this.filename,
            this.thumbnail,
            0,
            this.width,
            this.height,
            this.url,
            this.originalUrl,
            DownloadStatus.WAITING,
            null
        )
    }

    fun retryDownload(id: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val item = filesDao.getFile(id)
            if (item != null) {
                updateFileStatus(id, DownloadStatus.WAITING)
                ServiceUtil.startDownloadService(context)
            }
        }
    }

    fun deleteFile(id: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val item = filesDao.getFile(id)
            if (item != null) {
                val downloadManager = DownloadManager.getInstance(context)
                downloadManager.cancelTask(item.id)
                deleteFileFromDatabase(id)
            }
        }
    }

    fun cancelDownload(id: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val downloadManager = DownloadManager.getInstance(context)
            downloadManager.cancelTask(id)
            updateFileStatus(id, DownloadStatus.FAILED)
        }
    }

    fun updateFileSize(id: String, size: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            val file = filesDao.getFile(id)
            file?.let {
                filesDao.updateFileSize(id, size)
            }
        }
    }

    fun updateFileStatus(id: String, status: DownloadStatus) {
        CoroutineScope(Dispatchers.IO).launch {
            val file = filesDao.getFile(id)
            file?.let {
                filesDao.updateFileStatus(id, status.encode())
            }
        }
    }

    fun updateFilePath(id: String, path: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val file = filesDao.getFile(id)
            file?.let {
                filesDao.updateFilePath(id, path)
            }
        }
    }

    private fun deleteFileFromDatabase(id: String) {
        CoroutineScope(Dispatchers.IO).launch {
            filesDao.deleteFileById(id)
        }
    }

    suspend fun getActiveDownloads(): List<FileDBModel> {
        return filesDao.getActiveDownloads()
    }

}