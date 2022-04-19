package com.echoeyecodes.dobby.utils

import androidx.recyclerview.widget.DiffUtil
import com.echoeyecodes.dobby.db.models.FileDBModel
import com.echoeyecodes.dobby.models.EmptyModel

class DefaultItemCallback : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }
}


class FileDBModelItemCallback : DiffUtil.ItemCallback<FileDBModel>() {
    override fun areItemsTheSame(oldItem: FileDBModel, newItem: FileDBModel): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FileDBModel, newItem: FileDBModel): Boolean {
        return oldItem == newItem && oldItem.fileSize == newItem.fileSize && oldItem.fileExists == newItem.fileExists
    }
}


class EmptyItemCallBack : DiffUtil.ItemCallback<EmptyModel>() {

    override fun areItemsTheSame(oldItem: EmptyModel, newItem: EmptyModel): Boolean {
        return oldItem.text == newItem.text
    }

    override fun areContentsTheSame(oldItem: EmptyModel, newItem: EmptyModel): Boolean {
        return oldItem == newItem
    }

}


class DownloadActionDiffUtilCallBack : DiffUtil.ItemCallback<DownloadAction>() {

    override fun areItemsTheSame(oldItem: DownloadAction, newItem: DownloadAction): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: DownloadAction, newItem: DownloadAction): Boolean {
        return oldItem == newItem
    }
}
