package com.echoeyecodes.dobby.api.dao

import com.echoeyecodes.dobby.models.FileModel
import retrofit2.http.GET
import retrofit2.http.Query

interface DownloadDao {

    @GET("api/v1/download")
    suspend fun getDownload(@Query("url") url:String):List<FileModel>

}