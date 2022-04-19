package com.echoeyecodes.dobby.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.echoeyecodes.dobby.db.models.FileDBModel
import com.echoeyecodes.dobby.repository.FileRepository
import com.echoeyecodes.dobby.utils.FileUtils
import com.echoeyecodes.dobby.utils.NetworkState

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
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
                temp.clear()
                temp.addAll(newData)
                emit(newData)
            }
        }

    fun fetchData(url: String) {
        fileRepository.fetchFile(url)
    }

    fun getNetworkState(): LiveData<NetworkState> {
        return fileRepository.getNetworkState()
    }

    fun getFilesLiveData(): LiveData<List<FileDBModel>> {
        return files
    }
}