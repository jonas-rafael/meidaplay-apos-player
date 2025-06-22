package com.example.mediaplay.retrofit
import java.io.Serializable

data class M3UItem(
    val title: String,
    val url: String,
    val groupTitle: String?,
    val imageUrl: String?,
    var isFavorite: Boolean = false
) : Serializable
