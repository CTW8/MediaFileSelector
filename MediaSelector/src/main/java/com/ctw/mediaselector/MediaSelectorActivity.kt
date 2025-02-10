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
        mediaAdapter = MediaAdapter(emptyList()) { path, isSelected ->
            if (isSelected) selectedItems.add(path) else selectedItems.remove(path)
        }
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@MediaSelectorActivity, 3)
            adapter = mediaAdapter
        }
    }

    private fun setupButtons() {
        binding.btnCancel.setOnClickListener { finish() }
        binding.btnConfirm.setOnClickListener {
            val result = ArrayList<String>(selectedItems)
            setResult(Activity.RESULT_OK, Intent().putStringArrayListExtra("selected_files", result))
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
            val mediaList = withContext(Dispatchers.IO) {
                queryMediaFiles(currentMediaType)
            }
            mediaAdapter.updateData(mediaList)
        }
    }

    private fun queryMediaFiles(mediaType: MediaType): List<String> {
        val projection = arrayOf(
            when (mediaType) {
                MediaType.IMAGE -> MediaStore.Images.Media._ID
                MediaType.VIDEO -> MediaStore.Video.Media._ID
                MediaType.ALL -> MediaStore.Files.FileColumns._ID
            }
        )

        val (collection, selection) = when (mediaType) {
            MediaType.IMAGE -> Pair(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null // 图片库不需要额外筛选
            )
            MediaType.VIDEO -> Pair(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                null // 视频库不需要额外筛选
            )
            MediaType.ALL -> Pair(
                MediaStore.Files.getContentUri("external"),
                "${MediaStore.Files.FileColumns.MEDIA_TYPE} IN (" +
                    "${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE}," +
                    "${MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO})"
            )
        }

        return contentResolver.query(
            collection,
            projection,
            selection,
            null,
            "${MediaStore.MediaColumns.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(
                when (mediaType) {
                    MediaType.IMAGE -> MediaStore.Images.Media._ID
                    MediaType.VIDEO -> MediaStore.Video.Media._ID
                    MediaType.ALL -> MediaStore.Files.FileColumns._ID
                }
            )
            mutableListOf<String>().apply {
                while (cursor.moveToNext()) {
                    add(ContentUris.withAppendedId(collection, cursor.getLong(idColumn)).toString())
                }
            }
        } ?: emptyList()
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
    }
}

enum class MediaType { ALL, VIDEO, IMAGE }