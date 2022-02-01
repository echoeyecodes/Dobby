package com.echoeyecodes.dobby.fragments.bottomsheets

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.echoeyecodes.dobby.R
import com.echoeyecodes.dobby.adapters.DownloadActionAdapter
import com.echoeyecodes.dobby.callbacks.dialogfragmentcallbacks.DownloadActionCallback
import com.echoeyecodes.dobby.utils.AndroidUtilities
import com.echoeyecodes.dobby.utils.DownloadAction
import com.echoeyecodes.dobby.viewmodel.DownloadActionBottomSheetViewModel
import com.echoeyecodes.dobby.viewmodel.DownloadActionViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class DownloadActionBottomSheetFragment : BottomSheetDialogFragment(), DownloadActionCallback {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModelProvider: DownloadActionViewModelProvider
    private lateinit var viewModel: DownloadActionBottomSheetViewModel

    companion object {
        fun newInstance(id: String) = DownloadActionBottomSheetFragment().apply {
            val bundle = Bundle()
            bundle.putString("id", id)
            arguments = bundle
        }

        const val TAG = "DOWNLOAD_ACTION_BOTTOM_SHEET_FRAGMENT"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = arguments?.getString("id") ?: return dismiss()
        viewModelProvider = DownloadActionViewModelProvider(id, requireContext())
        viewModel = ViewModelProvider(
            this,
            viewModelProvider
        ).get(DownloadActionBottomSheetViewModel::class.java)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_download_action, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)

        val adapter = DownloadActionAdapter(requireContext(), this)
        val layoutManager = LinearLayoutManager(requireContext())

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        viewModel.data.observe(this, {
            adapter.submitList(it)
        })

        return view
    }

    override fun onItemSelected(item: DownloadAction) {
        when (item) {
            DownloadAction.OPEN -> {
                lifecycleScope.launch {
                    val path = viewModel.getDownloadFilePath()
                    path?.let {
                        AndroidUtilities.openFile(requireContext(), it)
                    }
                }
            }
            DownloadAction.CANCEL -> viewModel.cancelDownload()
            DownloadAction.DELETE -> viewModel.deleteDownload()
            DownloadAction.RETRY -> viewModel.retryDownload()
            DownloadAction.SHARE -> {
                lifecycleScope.launch {
                    val path = viewModel.getDownloadFilePath()
                    path?.let {
                        AndroidUtilities.shareFile(requireContext(), it)
                    }
                }
            }
            DownloadAction.COPY -> {
                lifecycleScope.launch {
                    val clipboardManager =
                        requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val link = viewModel.getDownloadFileLink()
                    if (link != null) {
                        val clipData = ClipData.newPlainText("Share link", link)
                        clipboardManager.setPrimaryClip(clipData)
                        AndroidUtilities.showToastMessage(
                            requireContext(),
                            "Link copied to clipboard"
                        )
                    } else {
                        AndroidUtilities.showToastMessage(requireContext(), "Could not get link")
                    }
                }
            }
        }
        dismiss()
    }
}