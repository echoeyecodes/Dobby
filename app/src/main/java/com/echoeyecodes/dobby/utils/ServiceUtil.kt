package com.echoeyecodes.dobby.utils

import android.content.Context
import androidx.work.*
import com.echoeyecodes.dobby.workmanager.DownloadWorkManager

class ServiceUtil {

    companion object {
        fun startDownloadService(context: Context) {
            val workRequest = OneTimeWorkRequestBuilder<DownloadWorkManager>()
                .addTag("DOWNLOAD").setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).build()
            WorkManager.getInstance(context).enqueueUniqueWork("DOWNLOAD", ExistingWorkPolicy.KEEP, workRequest)
        }
    }
}