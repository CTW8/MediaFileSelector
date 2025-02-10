package com.ctw.mediaselector

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.ctw.mediaselector.MediaAdapter.ViewHolder

class MediaAdapter(
    private var items: List<String>,
    private val mediaType: MediaType,
    private val selectedItems: Set<String>,
    private val onItemSelected: (String, Boolean) -> Unit
) : RecyclerView.Adapter<MediaAdapter.ViewHolder>() {

    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivThumbnail: ImageView = itemView.findViewById(R.id.ivThumbnail)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_media, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val uri = Uri.parse(items[position])
        val currentPosition = position
        
        // 使用Glide的异步加载
        when {
            isImage(currentPosition) -> loadImageThumbnail(holder, uri)
            isVideo(currentPosition) -> loadVideoThumbnail(holder, uri)
            else -> showDefaultThumbnail(holder)
        }

        holder.checkBox.setOnCheckedChangeListener(null) // 清除旧监听器
        holder.checkBox.isChecked = selectedItems.contains(items[currentPosition])
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            onItemSelected(items[currentPosition], isChecked)
        }
    }

    private fun isImage(position: Int) = items[position].contains("image")
    private fun isVideo(position: Int) = items[position].contains("video")

    private fun loadImageThumbnail(holder: ViewHolder, uri: Uri) {
        Glide.with(holder.itemView.context)
            .asBitmap()
            .load(uri)
            .override(250, 250)
            .centerCrop()
            .into(holder.ivThumbnail)
    }

    private fun loadVideoThumbnail(holder: ViewHolder, uri: Uri) {
        Glide.with(holder.itemView)
            .asBitmap()
            .load(uri)
            .frame(1_000_000)
            .placeholder(R.drawable.ic_loading_placeholder)
            .error(R.drawable.ic_error_thumbnail)
            .override(300, 300)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .centerCrop()
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    holder.ivThumbnail.setImageBitmap(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    holder.ivThumbnail.setImageDrawable(placeholder)
                }
            })
    }

    private fun showDefaultThumbnail(holder: ViewHolder) {
        holder.ivThumbnail.setImageResource(R.drawable.ic_default_thumbnail)
    }

    override fun getItemCount(): Int = items.size

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        Glide.with(holder.itemView).clear(holder.ivThumbnail)
    }

    fun updateData(newItems: List<String>) {
        items = newItems
        notifyDataSetChanged()
    }
} 