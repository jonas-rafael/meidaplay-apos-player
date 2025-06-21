package com.example.mediaplay.retrofit
import java.io.Serializable

data class M3UItem(
    val title: String,
    val url: String,
    val imageUrl: String? = null,
    val groupTitle: String? = null
) : Serializable
