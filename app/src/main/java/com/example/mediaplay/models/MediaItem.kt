package com.example.mediaplay.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaItem(
    val name: String,
    val url: String,
    val groupTitle: String
) : Parcelable
