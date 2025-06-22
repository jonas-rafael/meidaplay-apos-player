package com.example.mediaplay.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediaplay.retrofit.M3UItem
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
    private var itemsPerPage = 50
    private var currentPage = 1

    fun loadM3U(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    allItems = com.example.mediaplay.utils.M3UParser.parse(body)

                    generateCategories()
                    filterList()
                }
            } catch (e: Exception) {
                // Log de erro, se quiser
            }
        }
    }

    fun setFullList(fullList: List<M3UItem>) {
        allItems = fullList
        generateCategories()
        filterList()
    }

    fun setCategory(category: String?) {
        _selectedCategory.value = category
        currentPage = 1
        filterList()
    }

    fun filterByText(query: String) {
        val textFilter = query.trim().lowercase()
        _filteredList.postValue(
            allItems.filter { item ->
                val matchesCategory = when (_selectedCategory.value) {
                    "Todas as Categorias", null -> true
                    "Favoritos" -> item.isFavorite
                    else -> item.groupTitle == _selectedCategory.value
                }
                matchesCategory && item.title.lowercase().contains(textFilter)
            }.take(currentPage * itemsPerPage)
        )
    }

    fun loadMore() {
        currentPage++
        filterList()
    }

    private fun filterList() {
        val category = _selectedCategory.value
        val filtered = when (category) {
            "Todas as Categorias", null -> allItems
            "Favoritos" -> allItems.filter { it.isFavorite }
            else -> allItems.filter { it.groupTitle == category }
        }

        _filteredList.postValue(filtered.take(currentPage * itemsPerPage))
    }

    fun toggleFavorite(item: M3UItem) {
        allItems = allItems.map {
            if (it.url == item.url) it.copy(isFavorite = !it.isFavorite) else it
        }
        filterList()
    }

    private fun generateCategories() {
        val uniqueCategories = allItems.mapNotNull { it.groupTitle }.distinct().sorted().toMutableList()
        uniqueCategories.add(0, "Favoritos")
        uniqueCategories.add(0, "Todas as Categorias")
        _categories.postValue(uniqueCategories)
    }
}
