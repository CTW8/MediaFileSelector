package com.ctw.mediaselector

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.ctw.mediaselector.databinding.ItemMediaBinding

class MediaAdapter(
    private var mediaList: List<MediaFile>,
    private val onItemSelected: (MediaFile, Boolean) -> Unit,
    private val onItemClicked: (MediaFile) -> Unit
) : RecyclerView.Adapter<MediaAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemMediaBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(mediaFile: MediaFile) {
            binding.apply {
                // Load thumbnail
                val requestOptions = RequestOptions()
                    .placeholder(R.drawable.ic_loading_placeholder)
                    .error(R.drawable.ic_error_thumbnail)
                    .override(300, 300)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)

                Glide.with(root.context)
                    .load(Uri.parse("file://${mediaFile.path}"))
                    .apply(requestOptions)
                    .into(ivThumbnail)

                // Show video indicator for video files
                ivVideoIndicator.isVisible = mediaFile.type == MediaType.VIDEO

                // Update selection state
                updateSelectionState(mediaFile.isSelected)

                // Handle click events
                vRipple.setOnClickListener {
                    // 单击预览文件
                    onItemClicked(mediaFile)
                }
                
                vRipple.setOnLongClickListener {
                    // 长按选择文件
                    val newState = !mediaFile.isSelected
                    onItemSelected(mediaFile, newState)
                    updateSelectionState(newState)
                    true
                }
                
                flSelectionIndicator.setOnClickListener {
                    // 点击选择框直接切换选择状态
                    val newState = !mediaFile.isSelected
                    onItemSelected(mediaFile, newState)
                    updateSelectionState(newState)
                }
            }
        }
        
        private fun updateSelectionState(isSelected: Boolean) {
            binding.apply {
                // Update selection overlay
                vSelectionOverlay.isVisible = isSelected
                
                // Update selection indicator
                flSelectionIndicator.isSelected = isSelected
                ivCheckMark.isVisible = isSelected
                
                // Update card elevation
                (root as? com.google.android.material.card.MaterialCardView)?.apply {
                    cardElevation = if (isSelected) {
                        root.context.resources.getDimension(R.dimen.elevation_fab)
                    } else {
                        root.context.resources.getDimension(R.dimen.elevation_card)
                    }
                    strokeWidth = if (isSelected) 2 else 0
                    strokeColor = if (isSelected) {
                        root.context.getColor(R.color.md_theme_light_primary)
                    } else {
                        root.context.getColor(android.R.color.transparent)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMediaBinding.inflate(
            LayoutInflater.from(parent.context), 
            parent, 
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mediaList[position])
    }

    override fun getItemCount() = mediaList.size

    fun getSelectedItems() = mediaList.filter { it.isSelected }

    fun updateData(newList: List<MediaFile>) {
        mediaList = newList
        notifyDataSetChanged()
    }
    
    fun getSelectedCount() = mediaList.count { it.isSelected }
} 