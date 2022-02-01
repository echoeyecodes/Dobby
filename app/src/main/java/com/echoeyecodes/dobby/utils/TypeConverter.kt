package com.echoeyecodes.dobby.utils

import androidx.room.TypeConverter

class TypeConverter {

    @TypeConverter
    fun toStatus(status:Int) = status.decode()

    @TypeConverter
    fun fromStatus(status: DownloadStatus) = status.encode()
}