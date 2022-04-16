package com.echoeyecodes.dobby.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.echoeyecodes.dobby.db.models.FileDBModel
import com.echoeyecodes.dobby.repository.FileRepository
import com.echoeyecodes.dobby.utils.FileUtils
import com.echoeyecodes.dobby.utils.NetworkState

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    private val fileRepository = FileRepository(application)
    private val files: LiveData<List<FileDBModel>> = fileRepository.getFilesLiveData()

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