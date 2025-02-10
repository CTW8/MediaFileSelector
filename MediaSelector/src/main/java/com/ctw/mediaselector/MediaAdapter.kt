package com.ctw.mediaselector

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ctw.mediaselector.databinding.ItemMediaBinding

class MediaAdapter(
    private var mediaList: List<MediaFile>,
    private val onItemSelected: (MediaFile, Boolean) -> Unit
) : RecyclerView.Adapter<MediaAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemMediaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMediaBinding.inflate(
            LayoutInflater.from(parent.context), 
            parent, 
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mediaFile = mediaList[position]
        holder.binding.apply {
            Glide.with(root.context)
                .load(Uri.parse("file://${mediaFile.path}"))
                .placeholder(R.drawable.ic_loading_placeholder)
                .error(R.drawable.ic_error_thumbnail)
                .override(250, 250)
                .centerCrop()
                .into(ivThumbnail)

            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = mediaFile.isSelected
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                onItemSelected(mediaFile, isChecked)
            }

            root.setOnClickListener {
                checkBox.isChecked = !checkBox.isChecked
            }
        }
    }

    override fun getItemCount() = mediaList.size

    fun getSelectedItems() = mediaList.filter { it.isSelected }

    fun updateData(newList: List<MediaFile>) {
        mediaList = newList
        notifyDataSetChanged()
    }
} 