package com.echoeyecodes.dobby.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import com.echoeyecodes.dobby.models.FileInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

        suspend fun exists(context: Context, path: String): Boolean {
            return getFileInfo(context, path).exists
        }

        suspend fun getFileSize(context: Context, path: String): Long {
            return getFileInfo(context, path).size
        }

        suspend fun getFileInfo(context: Context, path: String): FileInfo {
            return withContext(Dispatchers.IO) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val cursor =
                        context.contentResolver.query(Uri.parse(path), null, null, null, null)
                            ?: return@withContext FileInfo(false, 0L)
                    val fileExists = cursor.count > 0
                    val fileInfo = if (!fileExists) {
                        FileInfo(false, 0L)
                    } else {
                        val size =
                            context.contentResolver.openFileDescriptor(
                                Uri.parse(path),
                                "r"
                            )?.statSize
                                ?: 0L
                        FileInfo(true, size)
                    }
                    cursor.close()
                    fileInfo
                } else {
                    val file = File(path)
                    if (!file.exists()) {
                        FileInfo(false, 0L)
                    }
                    FileInfo(true, File(path).length())
                }
            }
        }
    }
}