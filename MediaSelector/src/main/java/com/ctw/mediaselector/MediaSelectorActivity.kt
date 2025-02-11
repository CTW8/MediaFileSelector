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
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.net.Uri
import android.content.ContentUris
import kotlinx.coroutines.withContext
import com.ctw.mediaselector.MediaLoader


class MediaSelectorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMediaSelectorBinding
    private lateinit var mediaAdapter: MediaAdapter
    internal val selectedItems = mutableSetOf<String>()
    private var currentMediaType = MediaType.ALL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaSelectorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTabLayout()
        setupRecyclerView()
        setupButtons()
        checkPermissions()
    }

    private fun setupTabLayout() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("全部"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("视频"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("图片"))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
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
        binding.recyclerView.layoutManager = GridLayoutManager(this, 4)
        mediaAdapter = MediaAdapter(emptyList()) { mediaFile, isSelected ->
            mediaFile.isSelected = isSelected
        }
        binding.recyclerView.adapter = mediaAdapter
    }

    private fun setupButtons() {
        binding.btnCancel.setOnClickListener { finish() }
        binding.btnConfirm.setOnClickListener {
            val selectedFiles = mediaAdapter.getSelectedItems().map { it.path }
            Intent().apply {
                putStringArrayListExtra(EXTRA_SELECTED_FILES, ArrayList(selectedFiles))
            }.let {
                setResult(RESULT_OK, it)
            }
            finish()
        }
    }

    private fun checkPermissions() {
        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (hasPermissions(*requiredPermissions)) {
            loadMediaFiles()
        } else {
            requestNeededPermissions(requiredPermissions)
        }
    }

    private fun hasPermissions(vararg permissions: String): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun loadMediaFiles() {
        lifecycleScope.launch {
            val files = withContext(Dispatchers.IO) {
                MediaLoader.loadMediaFiles(this@MediaSelectorActivity, currentMediaType)
            }
            mediaAdapter.updateData(files)
        }
    }

    private fun requestNeededPermissions(permissions: Array<String>) {
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            loadMediaFiles()
        } else {
            Toast.makeText(this, "需要权限才能使用此功能", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
        const val REQUEST_CODE_MEDIA_SELECT = 1001
        const val EXTRA_SELECTED_FILES = "selected_files"
    }
}