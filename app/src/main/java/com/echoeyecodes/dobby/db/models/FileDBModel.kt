package com.echoeyecodes.dobby.db.models

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.documentfile.provider.DocumentFile
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.echoeyecodes.dobby.utils.DownloadStatus
import com.echoeyecodes.dobby.utils.FileUtils
import java.io.File
import java.time.LocalDateTime

@Entity(tableName = "files")
data class FileDBModel(
    @PrimaryKey val id: String,
    val name: String,
    val thumbnail: String,
    val size: Long,
    val width: Int,
    val height: Int,
    val uri: String,
    val originalUrl: String,
    val status: DownloadStatus,
    val bytesDownloaded: Long,
    val path: String?,
    val timestamp: String = LocalDateTime.now().toString()
) {

    fun fileExists(context: Context): Boolean {
        if (path == null) {
            return false
        }
        return FileUtils.exists(context, path)
    }

    override fun toString(): String {
        return "DownloadMediaModel(id='$id', filename='$path', title='$name', size='$size', status=$status, url='$uri', originalUrl='$originalUrl', thumbnail='$thumbnail',  timestamp='$timestamp')"
    }
}