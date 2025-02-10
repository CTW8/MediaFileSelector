package com.ctw.mediaselector

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ctw.mediaselector.databinding.ItemMediaBinding

class MediaAdapter(
    private var items: List<String>,
    private val onItemSelected: (String, Boolean) -> Unit
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
        val uri = Uri.parse(items[position])
        holder.binding.apply {
            Glide.with(root.context)
                .load(uri)
                .override(250, 250)
                .centerCrop()
                .into(ivThumbnail)

            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = false
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                onItemSelected(items[position], isChecked)
            }

            root.setOnClickListener {
                checkBox.isChecked = !checkBox.isChecked
            }
        }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<String>) {
        items = newItems
        notifyDataSetChanged()
    }
} 