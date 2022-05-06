package com.echoeyecodes.dobby.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.echoeyecodes.dobby.repository.FileRepository
import com.echoeyecodes.dobby.utils.NetworkState

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    private val fileRepository = FileRepository(application)

    fun fetchData(url: String) {
        fileRepository.fetchFile(url)
    }

    fun getNetworkState(): LiveData<NetworkState> {
        return fileRepository.getNetworkState()
    }
}