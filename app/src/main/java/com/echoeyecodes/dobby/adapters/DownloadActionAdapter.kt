package com.echoeyecodes.dobby.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.echoeyecodes.dobby.R
import com.echoeyecodes.dobby.callbacks.dialogfragmentcallbacks.DownloadActionCallback
import com.echoeyecodes.dobby.utils.*

class DownloadActionAdapter(private val context:Context, val listener: DownloadActionCallback) : ListAdapter<DownloadAction, DownloadActionAdapter.DownloadActionViewHolder>(DownloadActionDiffUtilCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadActionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_download_action_item, parent, false)
        return DownloadActionViewHolder(view)
    }

    override fun onBindViewHolder(holder: DownloadActionViewHolder, position: Int) {
        val item = getItem(position)
        holder.bindData(item)
    }

    inner class DownloadActionViewHolder(private val view:View) : RecyclerView.ViewHolder(view){
        private val textView = view.findViewById<TextView>(R.id.text)

        @SuppressLint("SetTextI18n")
        fun bindData(item:DownloadAction){
            when(item){
                DownloadAction.RETRY -> setText("Re-download", R.drawable.ic_down_arrow)
                DownloadAction.CANCEL -> setText("Cancel Download", R.drawable.ic_baseline_close_24)
                DownloadAction.DELETE -> setText("Delete", R.drawable.ic_delete)
                DownloadAction.COPY -> setText("Copy Link", R.drawable.ic_link)
                DownloadAction.OPEN -> setText("Open", R.drawable.ic_play)
                DownloadAction.SHARE -> setText("Share", R.drawable.ic_share)
            }

            textView.setOnClickListener { listener.onItemSelected(item) }

        }

        private fun setText(text:String, icon:Int){
            textView.text = text
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(ResourcesCompat.getDrawable(context.resources, icon, context.theme), null, null, null)
        }
    }

}