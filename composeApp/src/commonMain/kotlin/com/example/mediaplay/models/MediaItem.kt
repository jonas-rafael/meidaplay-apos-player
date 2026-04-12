package com.example.mediaplay.models

import kotlinx.serialization.Serializable

@Serializable
data class MediaItem(
    val title: String,
    val url: String,
    val imageUrl: String? = null,
    val groupTitle: String? = null,
    val tvgId: String? = null,
    val tvgName: String? = null,
    val isFavorite: Boolean = false,
    val contentType: String = "LIVE" // "LIVE", "MOVIE", "SERIES"
)
