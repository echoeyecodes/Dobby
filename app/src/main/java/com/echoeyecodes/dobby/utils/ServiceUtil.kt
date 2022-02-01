package com.echoeyecodes.dobby.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import com.echoeyecodes.dobby.services.DownloadService

class ServiceUtil {

    companion object {
        fun startDownloadService(context: Context) {
            Intent(context, DownloadService::class.java).also {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(it)
                } else {
                    context.startService(it)
                }
            }
        }
    }
}