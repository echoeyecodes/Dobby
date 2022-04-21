package com.echoeyecodes.dobby.fragments.dialogfragments

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.echoeyecodes.dobby.utils.AndroidUtilities
import java.lang.Exception

abstract class BaseDialogFragment : DialogFragment() {

    abstract fun getDialogTag(): String

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            val transaction = manager.beginTransaction()
            transaction.add(this, tag)
            transaction.commitAllowingStateLoss()
        } catch (exception: Exception) {
            AndroidUtilities.showToastMessage(requireContext(), "An unexpected error occurred")
        }
    }
}