package com.example.mediafileselector

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ctw.mediaselector.MediaSelectorActivity


class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE_CHOOSE = 200
    private lateinit var btnSelect: Button
    private lateinit var txtPath: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        btnSelect = findViewById(R.id.btn_select_file)
        txtPath = findViewById(R.id.txt_path)

        btnSelect.setOnClickListener {
            // 确保启动的是我们自己的Activity
            val intent = Intent(this, MediaSelectorActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_CHOOSE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            val selectedFiles = data?.getStringArrayListExtra("selected_files")
            txtPath.text = selectedFiles?.joinToString("\n") ?: "未选择文件"
        }
    }
}

