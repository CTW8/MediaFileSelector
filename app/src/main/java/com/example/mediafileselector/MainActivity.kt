package com.example.mediafileselector

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ctw.mediaselector.MediaSelectorActivity
import java.io.File

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
                append("未选择任何文件")
                return
            }
            
            filePaths.forEachIndexed { index, path ->
                val fileName = File(path).name
                append("${index + 1}. $fileName\n")
                append("路径：$path\n\n")
            }
        }

        findViewById<android.widget.TextView>(R.id.tv_selected_files).apply {
            text = stringBuilder.toString()
        }
    }
}

