package com.ctw.mediaselector

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.RecyclerView
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.app.AlertDialog
import android.os.Build

class MediaSelectorActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var btnCancel: Button
    private lateinit var btnConfirm: Button
    val selectedItems = mutableSetOf<String>()

    private val tabTitles = listOf("全部", "视频", "图片")

    // 添加权限请求合约
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            setupUI()
        } else {
            Toast.makeText(this, "需要存储权限才能使用此功能", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_selector)

        checkPermission()
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
                showPermissionExplanationDialog(requiredPermissions)
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

        // 初始化ViewPager和TabLayout
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager.adapter = MediaPagerAdapter(this)
        
        // 设置TabLayout联动
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when(position) {
                0 -> "全部"
                1 -> "视频"
                2 -> "图片"
                else -> ""
            }
        }.attach()

        // 确认按钮点击处理
        findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            returnSelectedResults()
        }

        // 取消按钮点击处理
        findViewById<Button>(R.id.btnCancel).setOnClickListener {
            cancelSelection()
        }

        setupViewPager()
    }

    private fun setupViewPager() {
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                // 暂停前一个Fragment的加载
                (supportFragmentManager.findFragmentByTag("f${viewPager.currentItem}") as? MediaFragment)?.let {
                    it.recyclerView?.adapter = null
                }
                super.onPageSelected(position)
            }
        })
    }

    private fun returnSelectedResults() {
        val resultIntent = Intent().apply {
            putStringArrayListExtra("selected_files", ArrayList(selectedItems))
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun cancelSelection() {
        setResult(RESULT_CANCELED)
        finish()
    }

    fun updateSelectedItems(path: String, isSelected: Boolean) {
        if (isSelected) {
            selectedItems.add(path)
        } else {
            selectedItems.remove(path)
        }
    }

    private fun showPermissionExplanationDialog(permissions: Array<String>) {
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
        return ContextCompat.checkSelfPermission(
            this,
            requiredPermissions[0]
        ) == PackageManager.PERMISSION_GRANTED
    }
}

class MediaPagerAdapter(activity: AppCompatActivity) : androidx.viewpager2.adapter.FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): androidx.fragment.app.Fragment {
        return when (position) {
            0 -> MediaFragment.newInstance(MediaType.ALL)
            1 -> MediaFragment.newInstance(MediaType.VIDEO)
            2 -> MediaFragment.newInstance(MediaType.IMAGE)
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}

enum class MediaType { ALL, VIDEO, IMAGE }