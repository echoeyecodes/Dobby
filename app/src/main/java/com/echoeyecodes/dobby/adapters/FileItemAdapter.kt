package com.echoeyecodes.dobby.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.echoeyecodes.dobby.R
import com.echoeyecodes.dobby.callbacks.adaptercallbacks.FileItemAdapterCallback
import com.echoeyecodes.dobby.databinding.LayoutFileItemBinding
import com.echoeyecodes.dobby.db.models.FileDBModel
import com.echoeyecodes.dobby.utils.*
import kotlin.math.max
import kotlin.math.min

class FileItemAdapter(private val callback: FileItemAdapterCallback) :
    ListAdapter<FileDBModel, FileItemAdapter.FileItemAdapterViewHolder>(FileDBModelItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileItemAdapterViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.layout_file_item, parent, false)
        return FileItemAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileItemAdapterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FileItemAdapterViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val binding = LayoutFileItemBinding.bind(view)
        private val imageView = binding.imageView
        private val status = binding.progress.downloadStatus
        private val size = binding.size
        private val moreBtn = binding.moreBtn
        private val title = binding.title
        private val indicator = binding.progress.progress
        private val indicatorContainer = binding.progress.indicatorContainer

        @SuppressLint("SetTextI18n")
        fun bind(model: FileDBModel) {
            title.text = model.name
            val fileSize = model.fileSize
            Glide.with(view).load(model.getPath(view.context)).sizeMultiplier(0.5f).into(imageView)

            updateDownloadStatus(model)
            moreBtn.setOnClickListener { callback.onMorePressed(model.id) }
            size.text = "${model.size.getFormattedSize()} | ${model.timestamp.getFormattedDate()}"

            view.setOnClickListener { callback.onItemPressed(model) }
            updateProgress(fileSize, model.size)
        }

        fun updateProgress(bytesDownloaded: Long, size: Long) {
            val progress = if (size == 0L) {
                0
            } else {
                min((bytesDownloaded.toDouble() / size.toDouble()) * 100, 100.0)
            }
            indicator.progress = progress.toInt()
        }

        @SuppressLint("SetTextI18n")
        private fun updateDownloadStatus(model: FileDBModel) {
            when (model.status) {
                DownloadStatus.WAITING -> {
                    indicatorContainer.visibility = View.VISIBLE
                    status.text = "Preparing..."
                }
                DownloadStatus.DOWNLOADING -> {
                    indicatorContainer.visibility = View.VISIBLE
                    status.text = "Downloading..."
                }
                DownloadStatus.CANCELLED -> {
                    indicatorContainer.visibility = View.VISIBLE
                    status.text = "Download cancelled"
                }
                DownloadStatus.FAILED -> {
                    indicatorContainer.visibility = View.VISIBLE
                    status.text = "Download failed"
                }
                DownloadStatus.COMPLETE -> {
                    indicatorContainer.visibility = View.GONE
                    if (model.fileExists) {
                        status.text = "Complete!"
                    } else {
                        status.text = "File moved or deleted"
                    }
                }
            }
        }
    }
}