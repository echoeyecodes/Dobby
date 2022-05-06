package com.echoeyecodes.dobby.fragments.tabfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.echoeyecodes.dobby.R
import com.echoeyecodes.dobby.activities.MainActivity
import com.echoeyecodes.dobby.adapters.EmptyAdapter
import com.echoeyecodes.dobby.adapters.FileItemAdapter
import com.echoeyecodes.dobby.callbacks.adaptercallbacks.EmptyAdapterCallback
import com.echoeyecodes.dobby.callbacks.adaptercallbacks.FileItemAdapterCallback
import com.echoeyecodes.dobby.callbacks.downloadmanagercallbacks.DownloadManagerCallbackImpl
import com.echoeyecodes.dobby.databinding.FragmentDownloadListBinding
import com.echoeyecodes.dobby.db.models.FileDBModel
import com.echoeyecodes.dobby.fragments.bottomsheets.DownloadActionBottomSheetFragment
import com.echoeyecodes.dobby.models.EmptyModel
import com.echoeyecodes.dobby.utils.*
import com.echoeyecodes.dobby.viewmodel.DownloadListViewModel
import com.echoeyecodes.dobby.viewmodel.DownloadListViewModelFactory

class DownloadListFragment : Fragment(), EmptyAdapterCallback,
    FileItemAdapterCallback {
    private lateinit var binding: FragmentDownloadListBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: DownloadListViewModel
    private lateinit var fileAdapter: FileItemAdapter
    private val downloadManager by lazy { DownloadManager.getInstance(requireContext()) }

    companion object {
        fun getInstance(pageType: DownloadListPageType) = DownloadListFragment().apply {
            arguments = Bundle().apply {
                putSerializable("type", pageType)
            }

        }
    }

    private fun getPageType(): DownloadListPageType {
        return arguments?.getSerializable("type") as DownloadListPageType?
            ?: DownloadListPageType.ACTIVE
    }

    private fun getEmptyModel(): EmptyModel {
        return if (getPageType() == DownloadListPageType.ACTIVE) {
            EmptyModel(
                R.drawable.ic_empty,
                "Dedicated space for active and pending downloads",
                "Add File"
            )
        } else {
            EmptyModel(
                R.drawable.ic_empty,
                "All your complete downloads will appear here",
                null
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val layout = inflater.inflate(R.layout.fragment_download_list, container, false)
        binding = FragmentDownloadListBinding.bind(layout)
        recyclerView = binding.root
        val pageType = getPageType()
        val viewModelFactory = DownloadListViewModelFactory(requireContext(), pageType)
        viewModel = ViewModelProvider(this, viewModelFactory)[DownloadListViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        downloadManager.addDownloadManagerCallback(downloadManagerCallback)

        val itemDecoration = CustomItemDecoration(0, 0)
        val layoutManager = LinearLayoutManager(requireContext())
        fileAdapter = FileItemAdapter(this)
        val emptyAdapter = EmptyAdapter(this)

        val emptyModel = getEmptyModel()
        val adapter = ConcatAdapter(emptyAdapter, fileAdapter)

        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(itemDecoration)
        recyclerView.adapter = adapter

        viewModel.getFilesLiveData().observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                emptyAdapter.submitList(listOf(emptyModel))
            } else {
                emptyAdapter.submitList(ArrayList())
            }
            fileAdapter.submitList(it)
        }

    }

    override fun onDestroy() {
        downloadManager.removeDownloadManagerCallback(downloadManagerCallback)
        super.onDestroy()
    }

    override fun onMorePressed(id: String) {
        val actionBottomSheetFragment = DownloadActionBottomSheetFragment.newInstance(id)
        if (!actionBottomSheetFragment.isVisible) {
            actionBottomSheetFragment.show(
                parentFragmentManager,
                DownloadActionBottomSheetFragment.TAG
            )
        }
    }

    override fun onItemPressed(model: FileDBModel) {
        if (model.status != DownloadStatus.COMPLETE) {
            AndroidUtilities.showSnackBar(recyclerView, "Download not complete")
        } else {
            model.path?.let {
                AndroidUtilities.openFile(requireContext(), it)
            }
        }
    }

    private fun updateDownloadProgress(id: String, bytesRead: Long, size: Long) {
        requireActivity().runOnUiThread {
            val currentList = fileAdapter.currentList
            val position = currentList.indexOfFirst { it.id == id }
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
            viewHolder?.let {
                if (it is FileItemAdapter.FileItemAdapterViewHolder) {
                    it.updateProgress(bytesRead, size)
                }
            }
        }
    }

    private val downloadManagerCallback = object : DownloadManagerCallbackImpl {
        override fun onDownloadProgress(id: String, bytesRead: Long, size: Long) {
            super.onDownloadProgress(id, bytesRead, size)
            updateDownloadProgress(id, bytesRead, size)
        }
    }

    override fun onButtonPressed() {
        (requireActivity() as MainActivity).openInputUrlDialog()
    }
}