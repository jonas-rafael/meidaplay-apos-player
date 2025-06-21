package com.example.mediaplay.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mediaplay.retrofit.M3UItem

class PlaylistViewModel : ViewModel() {

    private val _selectedCategory = MutableLiveData<String?>()
    val selectedCategory: LiveData<String?> get() = _selectedCategory

    private val _filteredList = MutableLiveData<List<M3UItem>>()
    val filteredList: LiveData<List<M3UItem>> get() = _filteredList

    private var allItems: List<M3UItem> = emptyList()
    private var currentSearchQuery: String = ""

    fun setFullList(fullList: List<M3UItem>) {
        allItems = fullList
        filterList()
    }

    fun setCategory(category: String?) {
        _selectedCategory.value = category
        filterList()
    }

    fun setSearchQuery(query: String) {
        currentSearchQuery = query
        filterList()
    }

    private fun filterList() {
        var result = allItems

        // Filtro por categoria
        _selectedCategory.value?.let { category ->
            if (category.isNotEmpty()) {
                result = result.filter { it.groupTitle == category }
            }
        }

        // Filtro por busca
        if (currentSearchQuery.isNotEmpty()) {
            result = result.filter { it.title.contains(currentSearchQuery, ignoreCase = true) }
        }

        _filteredList.postValue(result)
    }
}
