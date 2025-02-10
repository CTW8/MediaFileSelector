package com.example.mediafileselector

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ctw.mediaselector.MediaSelectorActivity

class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE_CHOOSE = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        findViewById<android.widget.Button>(R.id.btn_select_file).setOnClickListener {
            startActivityForResult(
                Intent(this, MediaSelectorActivity::class.java),
                REQUEST_CODE_CHOOSE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            val selectedFiles = data?.getStringArrayListExtra("selected_files")
            // 处理选择结果
        }
    }
}

