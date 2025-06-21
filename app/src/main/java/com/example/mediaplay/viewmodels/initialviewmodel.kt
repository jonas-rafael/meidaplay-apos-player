package com.example.mediaplay.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediaplay.retrofit.M3UItem
import com.example.mediaplay.retrofit.RetrofitInstance
import kotlinx.coroutines.launch

class initialviewmodel : ViewModel() {

    fun fetchM3U(url: String, callback: (List<M3UItem>) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getM3UPlaylist(url)

                if (response.isSuccessful) {
                    response.body()?.byteStream()?.bufferedReader()?.useLines { lines ->
                        val itemList = mutableListOf<M3UItem>()
                        var currentTitle: String? = null
                        var currentImage: String? = null
                        var currentGroup: String? = null

                        lines.forEach { line ->
                            val trimmedLine = line.trim()
                            if (trimmedLine.startsWith("#EXTINF:", ignoreCase = true)) {
                                currentTitle = trimmedLine.substringAfter(",").trim()

                                val logoRegex = """tvg-logo="([^"]*)"""".toRegex()
                                val groupRegex = """group-title="([^"]*)"""".toRegex()

                                currentImage = logoRegex.find(trimmedLine)?.groups?.get(1)?.value
                                currentGroup = groupRegex.find(trimmedLine)?.groups?.get(1)?.value
                            } else if (trimmedLine.startsWith(
                                    "http",
                                    ignoreCase = true
                                ) && currentTitle != null
                            ) {
                                itemList.add(
                                    M3UItem(
                                        title = currentTitle ?: "Sem Título",
                                        url = trimmedLine,
                                        imageUrl = currentImage,
                                        groupTitle = currentGroup
                                    )
                                )
                                currentTitle = null
                                currentImage = null
                                currentGroup = null
                            }
                        }

                        Log.d("M3U_DEBUG", "Itens parseados via streaming seguro: ${itemList.size}")
                        callback(itemList)
                    }
                } else {
                    Log.e("M3U_DEBUG", "Erro HTTP: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("M3U_DEBUG", "Exceção durante requisição: ${e.localizedMessage}")
            }
        }
    }
}