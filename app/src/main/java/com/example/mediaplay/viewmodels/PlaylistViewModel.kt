package com.example.mediaplay.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediaplay.retrofit.M3UItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlaylistViewModel : ViewModel() {

    private val _filteredList = MutableLiveData<List<M3UItem>>()
    val filteredList: LiveData<List<M3UItem>> get() = _filteredList

    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> get() = _categories

    private val _selectedCategory = MutableLiveData<String?>()
    private val _searchText = MutableLiveData<String?>()

    private var allItems: List<M3UItem> = emptyList()
    private var itemsDisplayed = 50  // Controle do "Ver mais"

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

    fun filterByText(text: String) {
        _searchText.value = if (text.isBlank()) null else text
        filterList()
    }

    private fun filterList() {
        viewModelScope.launch(Dispatchers.Default) {
            var filtered = allItems

            _selectedCategory.value?.let { category ->
                filtered = filtered.filter { it.groupTitle == category }
            }

            _searchText.value?.let { query ->
                filtered = filtered.filter { it.title.contains(query, ignoreCase = true) }
            }

            // Limite inicial para performance
            _filteredList.postValue(filtered.take(itemsDisplayed))
        }
    }

    fun loadMore() {
        itemsDisplayed += 50
        filterList()
    }
}
