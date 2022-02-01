package com.echoeyecodes.dobby.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.echoeyecodes.dobby.db.models.FileDBModel

@Dao
abstract class FileDao : BaseDao<FileDBModel>() {

    @Query("SELECT * FROM files WHERE id = :id LIMIT 1")
    abstract suspend fun getFile(id: String): FileDBModel?

    @Query("SELECT * FROM files WHERE id = :id LIMIT 1")
    abstract fun getFileLiveData(id: String): LiveData<FileDBModel?>

    @Query("UPDATE files SET status = :status WHERE id = :id")
    abstract suspend fun updateFileStatus(id: String, status: Int)

    @Query("UPDATE files SET path = :path WHERE id = :id")
    abstract suspend fun updateFilePath(id: String, path: String?)

    @Query("UPDATE files SET bytesDownloaded = :bytesDownloaded, size = :size WHERE id = :id")
    abstract suspend fun updateFileProgress(id: String, bytesDownloaded: Long, size: Long)

    @Query("SELECT * FROM files ORDER BY timestamp DESC")
    abstract fun getFilesLiveData(): LiveData<List<FileDBModel>>

    @Query("DELETE FROM files WHERE id = :id")
    abstract suspend fun deleteFileById(id: String)

    @Query("DELETE FROM files")
    abstract suspend fun deleteFiles()

    @Query("SELECT * FROM files WHERE status == 0")
    abstract suspend fun getActiveDownloads(): List<FileDBModel>

}