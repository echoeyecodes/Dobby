package com.echoeyecodes.dobby.fragments.dialogfragments

import androidx.fragment.app.DialogFragment

abstract class BaseDialogFragment : DialogFragment() {

    abstract fun getDialogTag():String
}