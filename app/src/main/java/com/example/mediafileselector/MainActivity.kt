package com.example.mediafileselector

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.ctw.mediaselector.MediaSelectorActivity
import com.ctw.mediaselector.MediaType
import java.io.File
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

class MainActivity : AppCompatActivity() {
    
    // 使用新的Activity Result API
    private val mediaSelectLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.getStringArrayListExtra("selected_files")?.let { paths ->
                showSelectedFiles(paths)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        // 普通文件选择
        findViewById<android.widget.Button>(R.id.btn_select_file).setOnClickListener {
            val intent = Intent(this, MediaSelectorActivity::class.java)
            mediaSelectLauncher.launch(intent)
        }
        
        // 只选择图片，最多3张
        findViewById<android.widget.Button>(R.id.btn_select_images).setOnClickListener {
            val intent = Intent(this, MediaSelectorActivity::class.java).apply {
                putExtra(MediaSelectorActivity.EXTRA_ALLOWED_TYPES, MediaType.IMAGE)
                putExtra(MediaSelectorActivity.EXTRA_MAX_SELECT_COUNT, 3)
            }
            mediaSelectLauncher.launch(intent)
        }
        
        // 只选择视频，最多1个
        findViewById<android.widget.Button>(R.id.btn_select_video).setOnClickListener {
            val intent = Intent(this, MediaSelectorActivity::class.java).apply {
                putExtra(MediaSelectorActivity.EXTRA_ALLOWED_TYPES, MediaType.VIDEO)
                putExtra(MediaSelectorActivity.EXTRA_MAX_SELECT_COUNT, 1)
            }
            mediaSelectLauncher.launch(intent)
        }
        
        // 选择任意文件，至少2个，最多5个
        findViewById<android.widget.Button>(R.id.btn_select_multiple).setOnClickListener {
            val intent = Intent(this, MediaSelectorActivity::class.java).apply {
                putExtra(MediaSelectorActivity.EXTRA_MIN_SELECT_COUNT, 2)
                putExtra(MediaSelectorActivity.EXTRA_MAX_SELECT_COUNT, 5)
            }
            mediaSelectLauncher.launch(intent)
        }
    }

    private fun showSelectedFiles(filePaths: List<String>) {
        val stringBuilder = StringBuilder().apply {
            if (filePaths.isEmpty()) {
                append("暂无选择的文件")
                return
            }
            
            append("共选择了 ${filePaths.size} 个文件：\n\n")
            
            filePaths.forEachIndexed { index, path ->
                val file = File(path)
                val fileName = file.name
                val fileSize = getReadableFileSize(file.length())
                val fileType = when {
                    fileName.lowercase().endsWith(".mp4") || 
                    fileName.lowercase().endsWith(".avi") || 
                    fileName.lowercase().endsWith(".mov") -> "📹 视频"
                    fileName.lowercase().endsWith(".jpg") || 
                    fileName.lowercase().endsWith(".jpeg") || 
                    fileName.lowercase().endsWith(".png") || 
                    fileName.lowercase().endsWith(".gif") -> "🖼️ 图片"
                    else -> "📄 文件"
                }
                
                append("${index + 1}. $fileType\n")
                append("📝 $fileName\n")
                append("📏 $fileSize\n")
                append("📂 ${file.parent}\n")
                if (index < filePaths.size - 1) {
                    append("\n")
                }
            }
        }

        findViewById<android.widget.TextView>(R.id.tv_selected_files).apply {
            text = stringBuilder.toString()
        }
    }
    
    private fun getReadableFileSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
    }
}

