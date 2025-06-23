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

    private var allItems: List<M3UItem> = emptyList()
    private var currentFilter: String? = null
    private var searchText: String = ""
    private var showFavorites: Boolean = false
    private var currentVisibleCount = 20  // Para o botão "Ver mais"

    fun loadM3U(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    allItems = com.example.mediaplay.utils.M3UParser.parse(body)
                    updateCategories()
                    filterList()
                }
            } catch (_: Exception) { }
        }
    }

    fun setFullList(list: List<M3UItem>) {
        allItems = list
        updateCategories()
        filterList()
    }

    private fun updateCategories() {
        val uniqueCategories = allItems.mapNotNull { it.groupTitle }.distinct().sorted()
        _categories.postValue(uniqueCategories)
    }

    fun setCategory(category: String) {
        currentFilter = if (category == "Todas as Categorias") null else category
        currentVisibleCount = 20
        filterList()
    }

    fun filterByText(text: String) {
        searchText = text
        currentVisibleCount = 20
        filterList()
    }

    fun toggleFavorite(item: M3UItem) {
        allItems = allItems.map {
            if (it.url == item.url) it.copy(isFavorite = !it.isFavorite) else it
        }
        filterList()
    }

    fun showFavoritesOnly() {
        showFavorites = !showFavorites
        currentVisibleCount = 20
        filterList()
    }

    fun loadMore() {
        currentVisibleCount += 20
        filterList()
    }

    fun getTotalCount(): Int {
        return allItems.size
    }

    private fun filterList() {
        var result = allItems

        if (currentFilter != null) {
            result = result.filter { it.groupTitle == currentFilter }
        }

        if (searchText.isNotBlank()) {
            result = result.filter { it.title.contains(searchText, ignoreCase = true) }
        }

        if (showFavorites) {
            result = result.filter { it.isFavorite }
        }

        // Paginação (limita a quantidade inicial de itens)
        val limitedResult = if (result.size > currentVisibleCount) {
            result.take(currentVisibleCount)
        } else result

        _filteredList.postValue(limitedResult)
    }
}
