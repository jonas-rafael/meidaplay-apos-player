package com.example.mediaplay.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediaplay.retrofit.M3UItem
import com.example.mediaplay.utils.M3UParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request

class PlaylistViewModel : ViewModel() {

    private val _filteredList = MutableLiveData<List<M3UItem>>()
    val filteredList: LiveData<List<M3UItem>> get() = _filteredList

    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> get() = _categories

    private val _selectedCategory = MutableLiveData<String?>()

    private var allItems: List<M3UItem> = emptyList()

    fun loadM3U(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("M3U_DEBUG", "Iniciando download da URL: $url")

                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""

                    Log.d("M3U_DEBUG", "Download concluído. Tamanho: ${body.length} caracteres")

                    // Limitar o tamanho pra evitar OutOfMemory
                    if (body.length > 10_000_000) {
                        Log.e("M3U_DEBUG", "Arquivo M3U muito grande! Cancelando parsing.")
                        return@launch
                    }

                    Log.d("M3U_DEBUG", "Iniciando parser M3U...")

                    allItems = M3UParser.parse(body)

                    Log.d("M3U_DEBUG", "Parsing finalizado. Total de itens: ${allItems.size}")

                    val uniqueCategories = allItems.mapNotNull { it.groupTitle }.distinct().sorted()
                    _categories.postValue(uniqueCategories)

                    filterList()
                } else {
                    Log.e("M3U_DEBUG", "Erro HTTP: ${response.code} - ${response.message}")
                }

            } catch (e: Exception) {
                Log.e("M3U_DEBUG", "Falha durante a requisição ou parsing: ${e.localizedMessage}")
            }
        }
    }

    fun setFullList(fullList: List<M3UItem>) {
        allItems = fullList
        val uniqueCategories = allItems.mapNotNull { it.groupTitle }.distinct().sorted()
        _categories.postValue(uniqueCategories)
        filterList()
    }

    fun setCategory(category: String?) {
        _selectedCategory.value = if (category == "Todas as Categorias") null else category
        filterList()
    }

    private fun filterList() {
        _filteredList.postValue(
            if (_selectedCategory.value.isNullOrEmpty()) allItems
            else allItems.filter { it.groupTitle == _selectedCategory.value }
        )
    }
}
