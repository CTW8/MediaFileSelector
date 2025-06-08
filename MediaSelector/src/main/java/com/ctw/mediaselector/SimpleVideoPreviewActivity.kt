package com.ctw.mediaselector

import android.content.Intent
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.ctw.mediaselector.databinding.ActivitySimpleVideoPreviewBinding
import java.io.File

class SimpleVideoPreviewActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySimpleVideoPreviewBinding
    private lateinit var mediaFile: MediaFile
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySimpleVideoPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 获取传递的媒体文件信息
        val filePath = intent.getStringExtra(EXTRA_FILE_PATH)
        val fileName = intent.getStringExtra(EXTRA_FILE_NAME)
        val fileType = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_FILE_TYPE, MediaType::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(EXTRA_FILE_TYPE) as? MediaType
        }
        
        if (filePath == null || fileName == null || fileType == null) {
            Toast.makeText(this, "参数错误", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        mediaFile = MediaFile(
            id = 0,
            name = fileName,
            path = filePath,
            type = fileType,
            dateAdded = 0
        )
        
        setupToolbar()
        setupPreview()
    }
    
    private fun setupToolbar() {
        binding.toolbar.apply {
            title = mediaFile.name
            setNavigationOnClickListener { finish() }
        }
    }
    
    private fun setupPreview() {
        when (mediaFile.type) {
            MediaType.IMAGE -> setupImagePreview()
            MediaType.VIDEO -> setupVideoPreview()
            MediaType.ALL -> {
                val extension = mediaFile.name.lowercase()
                when {
                    extension.endsWith(".jpg") || extension.endsWith(".jpeg") || 
                    extension.endsWith(".png") || extension.endsWith(".gif") || 
                    extension.endsWith(".webp") -> setupImagePreview()
                    else -> setupVideoPreview()
                }
            }
        }
    }
    
    private fun setupImagePreview() {
        binding.imageView.isVisible = true
        binding.videoThumbnailContainer.isVisible = false
        binding.loadingProgress.isVisible = true
        
        val requestOptions = RequestOptions()
            .error(R.drawable.ic_error_thumbnail)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        
        Glide.with(this)
            .load(Uri.parse("file://${mediaFile.path}"))
            .apply(requestOptions)
            .into(binding.imageView)
        
        binding.loadingProgress.isVisible = false
    }
    
    private fun setupVideoPreview() {
        binding.imageView.isVisible = false
        binding.videoThumbnailContainer.isVisible = true
        binding.loadingProgress.isVisible = true
        
        loadVideoThumbnail()
        
        binding.playButton.setOnClickListener {
            openVideoWithSystemPlayer()
        }
        
        binding.videoThumbnailContainer.setOnClickListener {
            openVideoWithSystemPlayer()
        }
    }
    
    private fun loadVideoThumbnail() {
        try {
            // 使用Glide加载视频缩略图
            val requestOptions = RequestOptions()
                .error(R.drawable.ic_error_thumbnail)
                .placeholder(R.drawable.ic_loading_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .override(800, 600)
                .centerCrop()
            
            Glide.with(this)
                .load(Uri.parse("file://${mediaFile.path}"))
                .apply(requestOptions)
                .into(binding.videoThumbnail)
            
            binding.loadingProgress.isVisible = false
        } catch (e: Exception) {
            binding.loadingProgress.isVisible = false
            Toast.makeText(this, "无法加载视频缩略图", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun openVideoWithSystemPlayer() {
        try {
            val file = File(mediaFile.path)
            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                file
            )
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "video/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                // 备用方案：使用file:// URI
                val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(Uri.parse("file://${mediaFile.path}"), "video/*")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(fallbackIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "无法打开视频播放器: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    companion object {
        const val EXTRA_FILE_PATH = "file_path"
        const val EXTRA_FILE_NAME = "file_name"
        const val EXTRA_FILE_TYPE = "file_type"
    }
} 