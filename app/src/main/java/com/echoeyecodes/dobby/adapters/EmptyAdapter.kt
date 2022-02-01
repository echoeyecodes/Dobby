package com.echoeyecodes.dobby.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.echoeyecodes.dobby.R
import com.echoeyecodes.dobby.callbacks.adaptercallbacks.EmptyAdapterCallback
import com.echoeyecodes.dobby.models.EmptyModel
import com.echoeyecodes.dobby.utils.EmptyItemCallBack
import com.google.android.material.button.MaterialButton

class EmptyAdapter(private val callback:EmptyAdapterCallback) : ListAdapter<EmptyModel, EmptyAdapter.EmptyViewHolder>(EmptyItemCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmptyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_empty, parent, false)
        return EmptyViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmptyViewHolder, position: Int) {
            holder.bindView(getItem(position))
    }

    inner class EmptyViewHolder(val view: View):RecyclerView.ViewHolder(view){
        private val imageView = view.findViewById<ImageView>(R.id.image_view)
        private val textView = view.findViewById<TextView>(R.id.text_view)
        private val button = view.findViewById<MaterialButton>(R.id.button)

        fun bindView(model:EmptyModel){
            textView.text = model.text
            Glide.with(view).load(model.image).into(imageView)
            if(model.buttonText == null){
                button.visibility = View.GONE
            }else{
                button.text = model.buttonText
                button.visibility = View.VISIBLE
                button.setOnClickListener { callback.onButtonPressed() }
            }
        }
    }
}