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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import android.database.Cursor

class MediaFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var mediaType: MediaType

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
        recyclerView.layoutManager = GridLayoutManager(context, 3)
        loadMediaFiles()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun loadMediaFiles() {
        viewLifecycleOwner.lifecycleScope.launch {
            val mediaList = withContext(Dispatchers.IO) {
                queryMediaFiles()
            }
            
            recyclerView.adapter = MediaAdapter(
                mediaList,
                mediaType,
                (activity as MediaSelectorActivity).selectedItems.toSet(),
                { path, isSelected ->
                    (activity as? MediaSelectorActivity)?.updateSelectedItems(path, isSelected)
                }
            )
        }
    }

    private suspend fun queryMediaFiles(): List<String> = withContext(Dispatchers.IO) {
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATE_ADDED
        )

        val collection = when (mediaType) {
            MediaType.IMAGE -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            MediaType.VIDEO -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            MediaType.ALL -> MediaStore.Files.getContentUri("external")
        }

        val selection = when (mediaType) {
            MediaType.IMAGE -> "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE}"
            MediaType.VIDEO -> "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO}"
            else -> null
        }

        val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

        return@withContext try {
            val cursor = requireContext().contentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder
            )
            
            parseMediaUris(cursor, collection)
        } catch (e: SecurityException) {
            emptyList<String>()
        }
    }

    private fun parseMediaUris(cursor: Cursor?, collection: Uri): List<String> {
        val mediaList = mutableListOf<String>()
        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val mediaTypeColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val type = it.getInt(mediaTypeColumn)
                val contentUri = when (type) {
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE -> ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                    )
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id
                    )
                    else -> continue
                }
                mediaList.add(contentUri.toString())
            }
        }
        return mediaList
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 确保释放所有视图相关资源
        recyclerView.adapter = null
    }
} 