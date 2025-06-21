package com.example.mediaplay.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediaplay.retrofit.M3UItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.InputStreamReader

class LoadingViewModel : ViewModel() {

    private val client = OkHttpClient()

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _playlist = MutableLiveData<List<M3UItem>>()
    val playlist: LiveData<List<M3UItem>> get() = _playlist

    fun fetchM3U(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.postValue(true)

            try {
                val request = Request.Builder()
                    .url(url)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val itemList = mutableListOf<M3UItem>()
                        response.body?.byteStream()?.let { inputStream ->
                            val reader = BufferedReader(InputStreamReader(inputStream))
                            var line: String?
                            var currentTitle: String? = null
                            var currentImage: String? = null
                            var currentGroup: String? = null

                            while (reader.readLine().also { line = it } != null) {
                                val trimmedLine = line!!.trim()

                                if (trimmedLine.startsWith("#EXTINF:", ignoreCase = true)) {
                                    currentTitle = trimmedLine.substringAfter(",").trim()

                                    val logoRegex = """tvg-logo="([^"]*)"""".toRegex()
                                    val groupRegex = """group-title="([^"]*)"""".toRegex()

                                    currentImage = logoRegex.find(trimmedLine)?.groups?.get(1)?.value
                                    currentGroup = groupRegex.find(trimmedLine)?.groups?.get(1)?.value
                                } else if (trimmedLine.startsWith("http", ignoreCase = true) && currentTitle != null) {
                                    itemList.add(
                                        M3UItem(
                                            title = currentTitle,
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
                        }

                        Log.d("M3U_DEBUG", "Itens parseados com OkHttp puro: ${itemList.size}")
                        _playlist.postValue(itemList)
                    } else {
                        Log.e("M3U_DEBUG", "Erro HTTP via OkHttp: ${response.code} - ${response.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e("M3U_DEBUG", "Exceção OkHttp direto: ${e.localizedMessage}")
            } finally {
                _loading.postValue(false)
            }
        }
    }
}
