package com.example.mediaplay.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediaplay.database.*
import com.example.mediaplay.models.MediaItem
import com.example.mediaplay.utils.M3UStreamParser
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
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
    val selectedType: String = "LIVE", // "LIVE", "MOVIE", "SERIES"
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
    private val _uiState = MutableStateFlow(PlaylistUiState())
    val uiState = _uiState.asStateFlow()

    private val httpClient = HttpClient()
    private val database = getDatabaseBuilder().build()
    private val favoriteDao = database.favoriteDao()
    private val playlistDao = database.playlistDao()
    private val mediaItemDao = database.mediaItemDao()

    init {
        viewModelScope.launch {
            playlistDao.getAll().collect { lists ->
                _uiState.update { it.copy(playlists = lists) }
                if (_uiState.value.selectedPlaylist == null && lists.isNotEmpty()) {
                    selectPlaylist(lists.first())
                }
            }
        }

        viewModelScope.launch {
            favoriteDao.getAll().collect { favs ->
                _uiState.update { it.copy(favorites = favs.map { f -> f.url }) }
            }
        }

        // Observar Canais filtrados do Banco por TIPO e CATEGORIA
        viewModelScope.launch {
            _uiState.flatMapLatest { state ->
                if (state.selectedPlaylist == null) flowOf(emptyList())
                else mediaItemDao.getFiltered(
                    state.selectedPlaylist.id,
                    state.selectedType,
                    state.selectedCategory,
                    state.searchText,
                    state.visibleCount
                )
            }.collect { entities ->
                _uiState.update { it.copy(filteredItems = entities.map { e -> 
                    MediaItem(e.title, e.url, e.imageUrl, e.groupTitle, e.tvgId, e.tvgName, false, e.contentType)
                }) }
            }
        }

        // Observar Categorias dinâmicas baseadas no TIPO selecionado
        viewModelScope.launch {
            _uiState.flatMapLatest { state ->
                if (state.selectedPlaylist == null) flowOf(emptyList())
                else mediaItemDao.getCategories(state.selectedPlaylist.id, state.selectedType)
            }.collect { cats ->
                val list = listOf("Todas") + cats.filterNotNull().distinct().sorted()
                _uiState.update { it.copy(categories = list) }
            }
        }
    }

    fun setType(type: String) {
        _uiState.update { it.copy(selectedType = type, selectedCategory = "Todas", visibleCount = 100) }
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
        _uiState.update { it.copy(selectedPlaylist = playlist, selectedCategory = "Todas", visibleCount = 100) }
        loadM3U(playlist)
    }

    private fun loadM3U(playlist: PlaylistItem) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadProgress = 0f) }
            try {
                withContext(Dispatchers.Default) {
                    httpClient.prepareGet(playlist.url) {
                        onDownload { bytes, total ->
                            if (total != null && total > 0) {
                                _uiState.update { it.copy(loadProgress = bytes.toFloat() / total) }
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
                            M3UStreamParser.parse(lines).forEach { item ->
                                batch.add(MediaItemEntity(
                                    title = item.title, url = item.url, imageUrl = item.imageUrl,
                                    groupTitle = item.groupTitle, tvgId = item.tvgId, tvgName = item.tvgName,
                                    playlistId = playlist.id, contentType = item.contentType
                                ))
                                if (batch.size >= 500) {
                                    runBlocking { mediaItemDao.insertAll(batch.toList()) }
                                    batch.clear()
                                }
                            }
                            if (batch.isNotEmpty()) mediaItemDao.insertAll(batch)
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Erro ao carregar: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false, loadProgress = null) }
            }
        }
    }

    fun selectItem(item: MediaItem) { _uiState.update { it.copy(selectedItem = item) } }
    fun setFullscreen(en: Boolean) { _uiState.update { it.copy(isFullscreen = en) } }
    fun toggleAspectRatio() {
        _uiState.update { state ->
            val next = when (state.aspectRatio) {
                PlayerAspectRatio.FIT -> PlayerAspectRatio.FILL
                PlayerAspectRatio.FILL -> PlayerAspectRatio.SIXTEEN_NINE
                PlayerAspectRatio.SIXTEEN_NINE -> PlayerAspectRatio.FOUR_THREE
                PlayerAspectRatio.FOUR_THREE -> PlayerAspectRatio.FIT
            }
            state.copy(aspectRatio = next)
        }
    }
    fun setCategory(cat: String) { _uiState.update { it.copy(selectedCategory = cat, visibleCount = 100) } }
    fun filterByText(txt: String) { _uiState.update { it.copy(searchText = txt, visibleCount = 100) } }
    fun loadMore() { _uiState.update { it.copy(visibleCount = it.visibleCount + 100) } }
    fun nextChannel() { /* Lógica de pular canal */ }
    fun previousChannel() { /* Lógica de pular canal */ }
    fun toggleFavorite(item: MediaItem) {
        viewModelScope.launch {
            val isFav = _uiState.value.favorites.contains(item.url)
            val fav = FavoriteItem(item.url, item.title, item.imageUrl, item.groupTitle)
            if (isFav) favoriteDao.delete(fav) else favoriteDao.insert(fav)
        }
    }
}
