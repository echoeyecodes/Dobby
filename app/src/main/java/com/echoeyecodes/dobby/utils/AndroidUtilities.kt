package com.echoeyecodes.dobby.utils

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.echoeyecodes.dobby.R
import com.echoeyecodes.dobby.fragments.dialogfragments.BaseDialogFragment
import com.google.android.material.snackbar.Snackbar
import java.io.File

class AndroidUtilities {

    companion object {
        fun getAppDirectory(context: Context) = File(
            Environment.getExternalStorageDirectory(), "/".plus(
                context.getString(
                    R.string.app_name
                )
            )
        )

        fun showSnackBar(view: View, message: String, action: (view: View) -> Unit = {}) {
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).setAction("Dismiss") {
                action(view)
            }.show()
        }

        fun showSnackBar(
            view: View,
            message: String,
            actionText: String,
            action: (view: View) -> Unit = {}
        ) {
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).setAction(actionText) {
                action(view)
            }.show()
        }

        fun getDrawable(context: Context, drawable: Int): Drawable? {
            return ResourcesCompat.getDrawable(context.resources, drawable, null)
        }

        fun showFragment(fragmentManager: FragmentManager, fragment: BaseDialogFragment) {
            if (!fragment.isAdded && !fragment.isVisible) {
                fragment.show(fragmentManager, fragment.getDialogTag())
            }
        }

        fun dismissFragment(fragment: DialogFragment) {
            if (fragment.isAdded) {
                fragment.dismiss()
            }
        }

        fun showToastMessage(context: Context, message: String) =
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

        fun log(message: String) = Log.d("CARRR", message)
        fun openFile(context: Context, path: String) {
            try {
                val type = FileUtils.getMimeType(context, path)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(Uri.parse(path), type)
                }
                context.startActivity(intent)
            } catch (exception: Exception) {
                showToastMessage(context, "Could not open file!")
            }

        }

        fun shareFile(context: Context, path: String) {
            try {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    putExtra(Intent.EXTRA_STREAM, Uri.parse(path))
                    type = "*/*"
                    putExtra(Intent.EXTRA_SUBJECT, "Send file")
                }
                context.startActivity(Intent.createChooser(intent, "Share file"))
            } catch (exception: Exception) {
                log(exception.message.toString())
                showToastMessage(context, "Could not share file")
            }
        }
    }
}