package com.ctw.mediaselector

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import android.database.Cursor
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import androidx.recyclerview.widget.GridLayoutManager
import androidx.lifecycle.lifecycleScope

class MediaFragment : Fragment() {
    internal lateinit var recyclerView: RecyclerView
    private lateinit var mediaType: MediaType
    private val mediaList = mutableListOf<String>()
    private var adapter: MediaAdapter? = null

    companion object {
        private const val ARG_MEDIA_TYPE = "media_type"

        fun newInstance(mediaType: MediaType): MediaFragment {
            val fragment = MediaFragment()
            val args = Bundle()
            args.putSerializable(ARG_MEDIA_TYPE, mediaType)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaType = arguments?.getSerializable(ARG_MEDIA_TYPE) as MediaType
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_media, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        
        // 初始化适配器
        adapter = MediaAdapter(
            emptyList(),
            mediaType,
            (activity as? MediaSelectorActivity)?.selectedItems ?: mutableSetOf()
        ) { path, isSelected ->
            (activity as? MediaSelectorActivity)?.let { activity ->
                if (isSelected) {
                    activity.selectedItems.add(path)
                } else {
                    activity.selectedItems.remove(path)
                }
            }
        }
        recyclerView.adapter = adapter
        
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun loadMedia() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                queryMedia()
            }
            mediaList.clear()
            mediaList.addAll(result)
            adapter?.updateData(mediaList)
        }
    }

    private suspend fun queryMedia(): List<String> {
        // 实现查询逻辑
        return emptyList()
    }

    private fun parseMediaUris(cursor: Cursor?, collection: Uri): List<String> {
        val mediaList = mutableListOf<String>()
        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val contentUri = when (mediaType) {
                    MediaType.IMAGE -> ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                    )
                    MediaType.VIDEO -> ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id
                    )
                    MediaType.ALL -> ContentUris.withAppendedId(collection, id)
                }
                mediaList.add(contentUri.toString())
            }
        }
        return mediaList
    }

    fun clearAdapter() {
        recyclerView.adapter = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 确保释放所有视图相关资源
        recyclerView.adapter = null
        Glide.with(this).pauseAllRequests()
    }
} 