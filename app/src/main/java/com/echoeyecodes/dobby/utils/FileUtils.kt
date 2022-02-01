package com.echoeyecodes.dobby.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import java.io.File

class FileUtils {

    companion object {
        fun getMimeType(context: Context, path: String): String? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.contentResolver.getType(Uri.parse(path))
            } else {
                MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(path))
            }
        }

        fun exists(context: Context, path: String): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val cursor = context.contentResolver.query(Uri.parse(path), null, null, null, null)
                    ?: return false
                val exists = cursor.count > 0
                cursor.close()
                exists
            } else {
                File(path).exists()
            }
        }
    }
}