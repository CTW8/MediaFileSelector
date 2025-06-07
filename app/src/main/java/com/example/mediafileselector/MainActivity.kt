package com.example.mediafileselector

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ctw.mediaselector.MediaSelectorActivity
import java.io.File
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE_CHOOSE = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        findViewById<android.widget.Button>(R.id.btn_select_file).setOnClickListener {
            val intent = Intent(this, MediaSelectorActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_CHOOSE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            data?.getStringArrayListExtra("selected_files")?.let { paths ->
                showSelectedFiles(paths)
            }
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

