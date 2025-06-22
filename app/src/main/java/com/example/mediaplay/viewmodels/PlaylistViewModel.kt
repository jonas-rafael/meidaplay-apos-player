package com.example.mediaplay.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediaplay.retrofit.M3UItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlaylistViewModel : ViewModel() {

    private var allItems: List<M3UItem> = emptyList()
    private val _filteredList = MutableLiveData<List<M3UItem>>()
    val filteredList: LiveData<List<M3UItem>> get() = _filteredList

    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> get() = _categories

    private var selectedCategory: String? = null
    private var currentSearchText: String = ""

    private var itemsToShow = 50

    fun setFullList(fullList: List<M3UItem>) {
        allItems = fullList
        generateCategories()
        filterList()
    }

    fun setCategory(category: String) {
        selectedCategory = if (category == "Todas as Categorias") null else category
        itemsToShow = 50
        filterList()
    }

    fun filterByText(text: String) {
        currentSearchText = text
        itemsToShow = 50
        filterList()
    }

    fun loadMore() {
        itemsToShow += 50
        filterList()
    }

    private fun generateCategories() {
        viewModelScope.launch(Dispatchers.Default) {
            val uniqueCategories = allItems.mapNotNull { it.groupTitle }.distinct().sorted()
            _categories.postValue(uniqueCategories)
        }
    }

    private fun filterList() {
        viewModelScope.launch(Dispatchers.Default) {
            var filtered = allItems

            // Filtro por categoria
            selectedCategory?.let { category ->
                filtered = filtered.filter { it.groupTitle == category }
            }

            // Filtro por texto
            if (currentSearchText.isNotEmpty()) {
                filtered = filtered.filter {
                    it.title.contains(currentSearchText, ignoreCase = true)
                }
            }

            // Aplicar paginação (Load More)
            val limitedList = filtered.take(itemsToShow)

            _filteredList.postValue(limitedList)
        }
    }
}
