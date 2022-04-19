package com.echoeyecodes.dobby.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.echoeyecodes.dobby.repository.FileRepository
import com.echoeyecodes.dobby.utils.DownloadAction
import com.echoeyecodes.dobby.utils.DownloadStatus
import com.echoeyecodes.dobby.utils.FileUtils

class DownloadActionViewModelProvider(val id: String, val context: Context) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DownloadActionBottomSheetViewModel::class.java)) {
            return DownloadActionBottomSheetViewModel(
                id,
                context.applicationContext as Application
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}

class DownloadActionBottomSheetViewModel(val id: String, application: Application) :
    AndroidViewModel(application) {
    val data: LiveData<List<DownloadAction>>
    private val fileRepository = FileRepository(application)

    init {
        data = Transformations.switchMap(fileRepository.getFileLiveData(id)) {
            liveData<List<DownloadAction>> {
                if (it != null) {
                    val options = when (it.status) {
                        DownloadStatus.WAITING, DownloadStatus.DOWNLOADING -> listOf(
                            DownloadAction.CANCEL,
                            DownloadAction.DELETE
                        )
                        DownloadStatus.COMPLETE -> {
                            if (FileUtils.exists(application, it.path ?: "")) {
                                listOf(
                                    DownloadAction.OPEN,
                                    DownloadAction.COPY,
                                    DownloadAction.DELETE,
                                    DownloadAction.SHARE
                                )
                            } else {
                                listOf(
                                    DownloadAction.RETRY,
                                    DownloadAction.COPY,
                                    DownloadAction.DELETE
                                )
                            }
                        }
                        DownloadStatus.FAILED, DownloadStatus.CANCELLED -> listOf(
                            DownloadAction.RETRY,
                            DownloadAction.DELETE
                        )
                    }
                    emit(options)
                } else {
                    emit(ArrayList())
                }
            }
        }
    }

    fun retryDownload() {
        fileRepository.retryDownload(id)
    }

    fun deleteDownload() {
        fileRepository.deleteFile(id)
    }

    fun cancelDownload() {
        fileRepository.cancelDownload(id)
    }

    suspend fun getDownloadFilePath(): String? {
        return fileRepository.getFile(id)?.path
    }

    suspend fun getDownloadFileLink(): String? {
        return fileRepository.getFile(id)?.uri
    }

}