package com.echoeyecodes.dobby.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.echoeyecodes.dobby.db.models.FileDBModel
import com.echoeyecodes.dobby.repository.FileRepository
import com.echoeyecodes.dobby.utils.*


class DownloadListViewModelFactory(
    private val context: Context,
    private val pageType: DownloadListPageType
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DownloadListViewModel::class.java)) {
            return DownloadListViewModel(context.applicationContext as Application, pageType) as T
        }
        throw IllegalArgumentException("Unknown View Model class")
    }
}


class DownloadListViewModel(application: Application, private val pageType: DownloadListPageType) :
    AndroidViewModel(application) {
    private val fileRepository = FileRepository(application)
    private var temp = ArrayList<FileDBModel>()
    private var files: LiveData<List<FileDBModel>> =
        Transformations.switchMap(fileRepository.getFilesLiveData()) { mapData ->
            liveData<List<FileDBModel>> {
                if (temp.isNotEmpty()) {
                    val previousData = mapData.map { mData ->
                        val prev = temp.find { it.id == mData.id }
                        if (prev != null) {
                            mData.fileSize = prev.fileSize
                            mData.fileExists = prev.fileExists
                        }
                        mData
                    }
                    emit(previousData)
                }
                val newData = mapData.map {
                    val file = it
                    val fileInfo = FileUtils.getFileInfo(application, file.path ?: "")
                    file.fileExists = fileInfo.exists
                    file.fileSize = fileInfo.size
                    file
                }

                /** cache previously formatted data, to prerender before recalculating
                file metadata for new files
                 **/
                temp.clear()
                temp.addAll(newData)
                emit(newData)
            }
        }

    fun getFilesLiveData(): LiveData<List<FileDBModel>> {
        return files.map {
            if (pageType == DownloadListPageType.ACTIVE) {
                it.filter { it.status != DownloadStatus.COMPLETE }
            } else {
                it.filter { it.status == DownloadStatus.COMPLETE }
            }
        }
    }
}