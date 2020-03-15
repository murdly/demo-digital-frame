package com.akarbowy.demodigitalframe

import android.app.Application
import android.content.ContentUris
import android.database.ContentObserver
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class DigitalFrameViewModel(application: Application) : AndroidViewModel(application) {

    private val _files = MutableLiveData<List<MediaFile>>()
    val files: LiveData<List<MediaFile>> get() = _files

    private var contentObserver: ContentObserver? = null

    fun loadFiles() {
        viewModelScope.launch {
            val data = queryFiles()

            _files.postValue(data)

            if (contentObserver == null) {
                contentObserver = getApplication<Application>().contentResolver.registerObserver(
                    EXTERNAL_FILES_URI
                ) {
                    loadFiles()
                }
            }
        }
    }

    private suspend fun queryFiles(): List<MediaFile> {
        val files = mutableListOf<MediaFile>()

        withContext(Dispatchers.IO) {

            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.DATE_ADDED
            )

            val selection = (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                    + " OR "
                    + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)

            val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

            getApplication<Application>().contentResolver.query(
                EXTERNAL_FILES_URI,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->

                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val typeColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)

                while (cursor.moveToNext()) {

                    val id = cursor.getLong(idColumn)

                    val type = when (cursor.getInt(typeColumn)) {
                        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE -> MediaFile.Type.IMAGE
                        else -> MediaFile.Type.VIDEO
                    }

                    val contentUri = ContentUris.withAppendedId(
                        EXTERNAL_FILES_URI,
                        id
                    )

                    val file = MediaFile(id, type, contentUri)

                    files += file
                }
            }
        }

        return files
    }

    override fun onCleared() {
        contentObserver?.let {
            getApplication<Application>().contentResolver.unregisterContentObserver(it)
        }
    }

    companion object {

        private val EXTERNAL_FILES_URI = MediaStore.Files.getContentUri("external")

    }

}