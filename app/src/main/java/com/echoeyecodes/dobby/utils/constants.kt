package com.echoeyecodes.dobby.utils

enum class DownloadStatus{
    WAITING,
    DOWNLOADING,
    COMPLETE,
    FAILED,
    CANCELLED
}

enum class DownloadAction{
    COPY,
    CANCEL,
    DELETE,
    RETRY,
    OPEN,
    SHARE
}


enum class NetworkState{
    LOADING,
    COMPLETE,
    ERROR,
    NONE
}