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

    private var allItems: List<M3UItem> = emptyList()
    private var currentLimit = 500

    fun setFullList(fullList: List<M3UItem>) {
        allItems = fullList
        generateCategories()
        filterList()
    }

    fun setCategory(category: String?) {
        _selectedCategory.value = if (category == "Todas as Categorias") null else category
        currentLimit = 500
        filterList()
    }

    fun loadMore() {
        currentLimit += 500
        filterList()
    }

    private fun filterList() {
        viewModelScope.launch(Dispatchers.Default) {
            val filtered = if (_selectedCategory.value.isNullOrEmpty()) {
                allItems.take(currentLimit)
            } else {
                allItems.filter { it.groupTitle == _selectedCategory.value }.take(currentLimit)
            }
            _filteredList.postValue(filtered)
        }
    }

    private fun generateCategories() {
        viewModelScope.launch(Dispatchers.Default) {
            val uniqueCategories = allItems.mapNotNull { it.groupTitle }.distinct().sorted()
            _categories.postValue(uniqueCategories)
        }
    }
}
