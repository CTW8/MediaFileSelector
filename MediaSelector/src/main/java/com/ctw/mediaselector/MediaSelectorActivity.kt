package com.ctw.mediaselector

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.app.AlertDialog
import android.os.Build
import com.bumptech.glide.Glide
import android.widget.Button
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ctw.mediaselector.databinding.ActivityMediaSelectorBinding


class MediaSelectorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMediaSelectorBinding
    internal val selectedItems = mutableSetOf<String>()
    private var currentMediaType = MediaType.ALL
    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnCancel: Button
    private lateinit var btnConfirm: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaSelectorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tabLayout = binding.tabLayout
        recyclerView = binding.recyclerView
        btnCancel = binding.btnCancel
        btnConfirm = binding.btnConfirm

        setupTabs()
        setupRecyclerView()
        setupButtons()
        loadMediaFiles()
    }

    private fun setupTabs() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentMediaType = when (tab?.position) {
                    0 -> MediaType.ALL
                    1 -> MediaType.VIDEO
                    2 -> MediaType.IMAGE
                    else -> MediaType.ALL
                }
                loadMediaFiles()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        val mediaAdapter = MediaAdapter(emptyList(), currentMediaType, selectedItems) { path, isSelected ->
            if (isSelected) selectedItems.add(path) else selectedItems.remove(path)
        }
        recyclerView.adapter = mediaAdapter
    }

    private fun setupButtons() {
        btnCancel.setOnClickListener { finish() }
        btnConfirm.setOnClickListener { confirmSelection() }
    }

    private fun loadMediaFiles() {
        val mediaList = queryMediaFiles()
        val mediaAdapter = recyclerView.adapter as? MediaAdapter
        mediaAdapter?.updateData(mediaList)
    }

    private fun queryMediaFiles(): List<String> {
        // 实现媒体文件查询逻辑（同原Fragment逻辑）
        return emptyList()
    }

    private fun checkPermission() {
        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        when {
            ContextCompat.checkSelfPermission(
                this,
                requiredPermissions[0]
            ) == PackageManager.PERMISSION_GRANTED -> {
                setupUI()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                requiredPermissions[0]
            ) -> {
                showPermissionExplanationDialog()
            }
            else -> {
                requestPermissions(requiredPermissions)
            }
        }
    }

    private fun requestPermissions(permissions: Array<String>) {
        val requestCode = 1001
        ActivityCompat.requestPermissions(this, permissions, requestCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                setupUI()
            } else {
                Toast.makeText(this, "需要媒体访问权限", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun setupUI() {
        setContentView(R.layout.activity_media_selector)
        
        if (!hasStoragePermission()) {
            Toast.makeText(this, "需要存储权限", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupTabs()
        setupRecyclerView()
        setupButtons()
        loadMediaFiles()
    }

    private fun showPermissionExplanationDialog() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        
        AlertDialog.Builder(this)
            .setTitle("需要媒体访问权限")
            .setMessage("此功能需要访问您的照片和视频来显示媒体内容")
            .setPositiveButton("授予权限") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissions(arrayOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO
                    ), 1001)
                } else {
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1001)
                }
            }
            .setNegativeButton("取消") { _, _ -> finish() }
            .show()
    }

    private fun hasStoragePermission(): Boolean {
        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        
        return requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun confirmSelection() {
        val result = ArrayList(selectedItems)
        setResult(Activity.RESULT_OK, Intent().putStringArrayListExtra("selected_files", result))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 清理所有Glide请求
        Glide.with(applicationContext).pauseAllRequests()
        Glide.get(this).clearMemory()
        Glide.get(this).clearDiskCache()
        selectedItems.clear()
    }
}

enum class MediaType { ALL, VIDEO, IMAGE }