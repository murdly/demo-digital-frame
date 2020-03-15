package com.akarbowy.demodigitalframe

import android.net.Uri

data class MediaFile(
    val id: Long,
    val type: Type,
    val contentUri: Uri
) {
    enum class Type {
        IMAGE, VIDEO
    }
}