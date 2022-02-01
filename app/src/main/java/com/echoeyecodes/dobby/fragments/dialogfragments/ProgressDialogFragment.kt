package com.echoeyecodes.dobby.fragments.dialogfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.echoeyecodes.dobby.R

class ProgressDialogFragment : BaseDialogFragment() {

    companion object {
        const val TAG = "PROGRESS_DIALOG_FRAGMENT"
        fun newInstance() = ProgressDialogFragment()
    }

    override fun getDialogTag(): String {
        return TAG
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
        setStyle(STYLE_NORMAL, R.style.CustomAlertDialog)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_progress_dialog, container, false)
    }

}