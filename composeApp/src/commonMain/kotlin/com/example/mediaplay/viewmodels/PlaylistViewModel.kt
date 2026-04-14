package com.example.mediaplay.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediaplay.database.AppDatabase
import com.example.mediaplay.database.FavoriteItem
import com.example.mediaplay.database.FavoriteDao
import com.example.mediaplay.database.PlaylistDao
import com.example.mediaplay.database.MediaItemDao
import com.example.mediaplay.database.PlaylistItem
import com.example.mediaplay.database.MediaItemEntity
import com.example.mediaplay.database.getDatabaseBuilder
import com.example.mediaplay.models.MediaItem
import com.example.mediaplay.utils.M3UStreamParser
import com.example.mediaplay.ui.showInterstitialAd
import com.example.mediaplay.utils.getCurrentTimeMillis
import io.ktor.client.HttpClient
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

enum class PlayerAspectRatio { FIT, FILL, SIXTEEN_NINE, FOUR_THREE }

data class PlaylistUiState(
    val filteredItems: List<MediaItem> = emptyList(),
    val playlists: List<PlaylistItem> = emptyList(),
    val categories: List<String> = emptyList(),
    val favorites: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val loadProgress: Float? = null,
    val selectedType: String = "LIVE",
    val selectedCategory: String = "Todas",
    val selectedItem: MediaItem? = null,
    val selectedPlaylist: PlaylistItem? = null,
    val isFullscreen: Boolean = false,
    val aspectRatio: PlayerAspectRatio = PlayerAspectRatio.FIT,
    val searchText: String = "",
    val errorMessage: String? = null,
    val visibleCount: Int = 100
)

class PlaylistViewModel : ViewModel() {
    private val _uiState: MutableStateFlow<PlaylistUiState> = MutableStateFlow(PlaylistUiState())
    val uiState: StateFlow<PlaylistUiState> = _uiState.asStateFlow()

    private val httpClient = HttpClient()
    private val database: AppDatabase = getDatabaseBuilder().build()
    private val favoriteDao: FavoriteDao = database.favoriteDao()
    private val playlistDao: PlaylistDao = database.playlistDao()
    private val mediaItemDao: MediaItemDao = database.mediaItemDao()

    private var lastAdTimestamp: Long = 0L
    private var channelSwitchCount: Int = 0
    private var isFirstSelection: Boolean = true
    private val AD_COOLDOWN_MS: Int = 180_000 
    private val AD_SWITCH_THRESHOLD: Int = 3 

    init {
        viewModelScope.launch {
            playlistDao.getAll().collect { lists: List<PlaylistItem> ->
                _uiState.update { state: PlaylistUiState -> state.copy(playlists = lists) }
                val current = _uiState.value
                if (current.selectedPlaylist == null && lists.isNotEmpty()) {
                    selectPlaylist(lists.first())
                }
            }
        }

        viewModelScope.launch {
            favoriteDao.getAll().collect { favs: List<FavoriteItem> ->
                val urls: List<String> = favs.map { it.url }
                _uiState.update { state: PlaylistUiState -> state.copy(favorites = urls) }
            }
        }

        viewModelScope.launch {
            _uiState.flatMapLatest { state: PlaylistUiState ->
                val pl = state.selectedPlaylist
                if (pl == null) flowOf(emptyList<MediaItemEntity>())
                else mediaItemDao.getFiltered(
                    pl.id,
                    state.selectedType,
                    state.selectedCategory,
                    state.searchText,
                    state.visibleCount
                )
            }.collect { entities: List<MediaItemEntity> ->
                _uiState.update { state: PlaylistUiState ->
                    val mappedItems: List<MediaItem> = entities.map { e: MediaItemEntity -> 
                        MediaItem(
                            title = e.title,
                            url = e.url,
                            imageUrl = e.imageUrl,
                            groupTitle = e.groupTitle,
                            tvgId = e.tvgId,
                            tvgName = e.tvgName,
                            isFavorite = false,
                            contentType = e.contentType
                        )
                    }
                    state.copy(filteredItems = mappedItems)
                }
            }
        }

        viewModelScope.launch {
            _uiState.flatMapLatest { state: PlaylistUiState ->
                val pl = state.selectedPlaylist
                if (pl == null) flowOf(emptyList<String?>())
                else mediaItemDao.getCategories(pl.id, state.selectedType)
            }.collect { cats: List<String?> ->
                val list: List<String> = listOf("Todas") + cats.filterNotNull().distinct().sorted()
                _uiState.update { state: PlaylistUiState -> state.copy(categories = list) }
            }
        }
    }

    fun setType(type: String) {
        _uiState.update { state: PlaylistUiState -> state.copy(selectedType = type, selectedCategory = "Todas", visibleCount = 100) }
    }

    fun addPlaylist(name: String, url: String) {
        viewModelScope.launch { playlistDao.insert(PlaylistItem(name = name, url = url)) }
    }

    fun addXtreamPlaylist(name: String, server: String, user: String, pass: String) {
        val baseUrl = if (server.endsWith("/")) server else "$server/"
        val constructedUrl = "${baseUrl}get.php?username=$user&password=$pass&output=m3u8"
        viewModelScope.launch {
            playlistDao.insert(PlaylistItem(
                name = name,
                url = constructedUrl,
                serverUrl = server,
                username = user,
                password = pass
            ))
        }
    }

    fun deletePlaylist(playlist: PlaylistItem) {
        viewModelScope.launch {
            mediaItemDao.deleteByPlaylist(playlist.id)
            playlistDao.delete(playlist)
        }
    }

    fun selectPlaylist(playlist: PlaylistItem) {
        _uiState.update { state: PlaylistUiState -> state.copy(selectedPlaylist = playlist, selectedCategory = "Todas", visibleCount = 100) }
        loadM3U(playlist)
    }

    private fun loadM3U(playlist: PlaylistItem) {
        viewModelScope.launch {
            _uiState.update { state: PlaylistUiState -> state.copy(isLoading = true, loadProgress = 0f) }
            try {
                withContext(Dispatchers.Default) {
                    httpClient.prepareGet(playlist.url) {
                        onDownload { bytes, total ->
                            if (total != null && total > 0) {
                                _uiState.update { state: PlaylistUiState -> state.copy(loadProgress = bytes.toFloat() / total) }
                            }
                        }
                    }.execute { response ->
                        if (response.status.value in 200..299) {
                            mediaItemDao.deleteByPlaylist(playlist.id)
                            val channel = response.bodyAsChannel()
                            val lines = sequence {
                                while (!channel.isClosedForRead) {
                                    val line = runBlocking { channel.readUTF8Line() }
                                    if (line != null) yield(line) else break
                                }
                            }
                            
                            val batch = mutableListOf<MediaItemEntity>()
                            M3UStreamParser.parse(lines).forEach { item: MediaItem ->
                                batch.add(MediaItemEntity(
                                    title = item.title, url = item.url, imageUrl = item.imageUrl,
                                    groupTitle = item.groupTitle, tvgId = item.tvgId, tvgName = item.tvgName,
                                    playlistId = playlist.id, contentType = item.contentType
                                ))
                                if (batch.size >= 500) {
                                    val currentBatch = batch.toList()
                                    runBlocking { mediaItemDao.insertAll(currentBatch) }
                                    batch.clear()
                                }
                            }
                            if (batch.isNotEmpty()) {
                                val finalBatch = batch.toList()
                                runBlocking { mediaItemDao.insertAll(finalBatch) }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state: PlaylistUiState -> state.copy(errorMessage = "Erro: ${e.message}") }
            } finally {
                _uiState.update { state: PlaylistUiState -> state.copy(isLoading = false, loadProgress = null) }
            }
        }
    }

    fun selectItem(item: MediaItem) { 
        _uiState.update { state: PlaylistUiState -> state.copy(selectedItem = item) }
        
        val currentTime = getCurrentTimeMillis()
        
        if (isFirstSelection) {
            showInterstitialAd()
            isFirstSelection = false
            lastAdTimestamp = currentTime
            channelSwitchCount = 0
        } else {
            channelSwitchCount++
            if (channelSwitchCount >= AD_SWITCH_THRESHOLD && (currentTime - lastAdTimestamp) >= AD_COOLDOWN_MS) {
                showInterstitialAd()
                lastAdTimestamp = currentTime
                channelSwitchCount = 0
            }
        }
    }

    fun setFullscreen(en: Boolean) { 
        _uiState.update { state: PlaylistUiState -> state.copy(isFullscreen = en) } 
    }
    
    fun toggleAspectRatio() {
        _uiState.update { state: PlaylistUiState ->
            val next = when (state.aspectRatio) {
                PlayerAspectRatio.FIT -> PlayerAspectRatio.FILL
                PlayerAspectRatio.FILL -> PlayerAspectRatio.SIXTEEN_NINE
                PlayerAspectRatio.SIXTEEN_NINE -> PlayerAspectRatio.FOUR_THREE
                PlayerAspectRatio.FOUR_THREE -> PlayerAspectRatio.FIT
                else -> PlayerAspectRatio.FIT
            }
            state.copy(aspectRatio = next)
        }
    }

    fun setCategory(cat: String) { 
        _uiState.update { state: PlaylistUiState -> state.copy(selectedCategory = cat, visibleCount = 100) } 
    }
    
    fun filterByText(txt: String) { 
        _uiState.update { state: PlaylistUiState -> state.copy(searchText = txt, visibleCount = 100) } 
    }
    
    fun loadMore() { 
        _uiState.update { state: PlaylistUiState -> state.copy(visibleCount = state.visibleCount + 100) } 
    }
    
    fun nextChannel() {
        val currentState = _uiState.value
        val currentList: List<MediaItem> = currentState.filteredItems
        val currentItem: MediaItem? = currentState.selectedItem
        if (currentList.isNotEmpty() && currentItem != null) {
            val currentIndex = currentList.indexOfFirst { m: MediaItem -> m.url == currentItem.url }
            val nextIndex = if (currentIndex == -1 || currentIndex == currentList.size - 1) 0 else currentIndex + 1
            selectItem(currentList[nextIndex])
        }
    }

    fun previousChannel() {
        val currentState = _uiState.value
        val currentList: List<MediaItem> = currentState.filteredItems
        val currentItem: MediaItem? = currentState.selectedItem
        if (currentList.isNotEmpty() && currentItem != null) {
            val currentIndex = currentList.indexOfFirst { m: MediaItem -> m.url == currentItem.url }
            val prevIndex = if (currentIndex <= 0) currentList.size - 1 else currentIndex - 1
            selectItem(currentList[prevIndex])
        }
    }

    fun toggleFavorite(item: MediaItem) {
        viewModelScope.launch {
            val isFav = _uiState.value.favorites.contains(item.url)
            val fav = FavoriteItem(item.url, item.title, item.imageUrl, item.groupTitle)
            if (isFav) favoriteDao.delete(fav) else favoriteDao.insert(fav)
        }
    }
}
