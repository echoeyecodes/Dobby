package com.echoeyecodes.dobby.fragments.dialogfragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.echoeyecodes.dobby.R
import com.echoeyecodes.dobby.callbacks.dialogfragmentcallbacks.AddUrlDialogFragmentCallback
import com.echoeyecodes.dobby.databinding.FragmentAddUrlBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class AddUrlDialogFragment : BaseDialogFragment() {

    private lateinit var doneBtn: MaterialButton
    private lateinit var textInputEditText: TextInputEditText
    private lateinit var binding: FragmentAddUrlBinding

    companion object {
        const val TAG = "ADD_URL_DIALOG_FRAGMENT"
        fun newInstance() = AddUrlDialogFragment()
    }

    override fun getDialogTag(): String {
        return TAG
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        val view = inflater.inflate(R.layout.fragment_add_url, container, false)
        binding = FragmentAddUrlBinding.bind(view)
        doneBtn = binding.doneBtn
        textInputEditText = binding.urlField

        doneBtn.setOnClickListener { onSubmit() }
        return view
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun onSubmit() {
        val url = textInputEditText.text
        if (!url.isNullOrEmpty()) {
            (requireContext() as AddUrlDialogFragmentCallback).onUrlAdded(url.toString())
            textInputEditText.setText("")
        }
    }
}