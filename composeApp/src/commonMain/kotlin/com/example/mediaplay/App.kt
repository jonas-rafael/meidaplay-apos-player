package com.example.mediaplay

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.mediaplay.models.MediaItem
import com.example.mediaplay.viewmodels.PlaylistViewModel
import com.example.mediaplay.viewmodels.PlayerAspectRatio
import com.example.mediaplay.ui.player.VideoPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    viewModel: PlaylistViewModel = viewModel { PlaylistViewModel() }
) {
    val state by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            surface = Color(0xFF121212),
            background = Color(0xFF000000),
            primary = Color(0xFFBB86FC)
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                containerColor = Color.Black
            ) { padding ->
                Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                    if (state.playlists.isEmpty() && !state.isLoading) {
                        EmptyStateView(onAddClick = { showAddDialog = true })
                    } else {
                        if (state.isFullscreen && state.selectedItem != null) {
                            FullscreenPlayer(
                                item = state.selectedItem!!,
                                aspectRatio = state.aspectRatio,
                                onBack = { viewModel.setFullscreen(false) },
                                onToggleRatio = { viewModel.toggleAspectRatio() },
                                onNext = { viewModel.nextChannel() },
                                onPrevious = { viewModel.previousChannel() }
                            )
                        } else {
                            AdaptiveLayout(state, viewModel, onAddPlaylist = { showAddDialog = true })
                        }
                    }

                    if (state.isLoading) {
                        LoadingOverlay(state.loadProgress)
                    }
                }
            }

            if (showAddDialog) {
                AddPlaylistDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirmM3U = { name, url ->
                        viewModel.addPlaylist(name, url)
                        showAddDialog = false
                    },
                    onConfirmXtream = { name, server, user, pass ->
                        viewModel.addXtreamPlaylist(name, server, user, pass)
                        showAddDialog = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveLayout(
    state: com.example.mediaplay.viewmodels.PlaylistUiState,
    viewModel: PlaylistViewModel,
    onAddPlaylist: () -> Unit
) {
    BoxWithConstraints {
        val isMobile = maxWidth < 800.dp
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        if (isMobile) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        modifier = Modifier.width(300.dp),
                        drawerContainerColor = Color(0xFF1A1A1A)
                    ) {
                        Sidebar(
                            state = state,
                            onCategorySelected = { 
                                viewModel.setCategory(it)
                                scope.launch { drawerState.close() }
                            },
                            onTypeSelected = { viewModel.setType(it) },
                            onPlaylistSelected = { 
                                viewModel.selectPlaylist(it)
                                scope.launch { drawerState.close() }
                            },
                            onAddPlaylist = onAddPlaylist,
                            onDeletePlaylist = { viewModel.deletePlaylist(it) }
                        )
                    }
                }
            ) {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PlayCircleFilled, null, tint = Color(0xFFBB86FC), modifier = Modifier.size(24.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("MEDIAPLAY", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, null)
                                }
                            },
                            actions = {
                                IconButton(onClick = onAddPlaylist) {
                                    Icon(Icons.Default.PlaylistAdd, null)
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                        )
                    }
                ) { padding ->
                    Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                        if (state.selectedItem != null) {
                            Box(modifier = Modifier.fillMaxWidth().aspectRatio(16/9f).background(Color.Black)) {
                                VideoPlayer(url = state.selectedItem.url, modifier = Modifier.fillMaxSize(), aspectRatio = state.aspectRatio)
                                IconButton(
                                    onClick = { viewModel.setFullscreen(true) },
                                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).background(Color.Black.copy(0.5f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Fullscreen, null, tint = Color.White)
                                }
                            }
                        }
                        
                        ChannelListSection(
                            modifier = Modifier.fillMaxSize(),
                            channels = state.filteredItems,
                            favoriteUrls = state.favorites,
                            selectedItem = state.selectedItem,
                            searchText = state.searchText,
                            onSearchChanged = { viewModel.filterByText(it) },
                            onChannelSelected = { viewModel.selectItem(it) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onLoadMore = { viewModel.loadMore() }
                        )
                    }
                }
            }
        } else {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.width(300.dp)) {
                    Sidebar(
                        state = state,
                        onCategorySelected = { viewModel.setCategory(it) },
                        onTypeSelected = { viewModel.setType(it) },
                        onPlaylistSelected = { viewModel.selectPlaylist(it) },
                        onAddPlaylist = onAddPlaylist,
                        onDeletePlaylist = { viewModel.deletePlaylist(it) }
                    )
                }

                ChannelListSection(
                    modifier = Modifier.weight(1f),
                    channels = state.filteredItems,
                    favoriteUrls = state.favorites,
                    selectedItem = state.selectedItem,
                    searchText = state.searchText,
                    onSearchChanged = { viewModel.filterByText(it) },
                    onChannelSelected = { viewModel.selectItem(it) },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onLoadMore = { viewModel.loadMore() }
                )

                PreviewArea(
                    modifier = Modifier.weight(1.5f),
                    selectedItem = state.selectedItem,
                    aspectRatio = state.aspectRatio,
                    isFavorite = state.favorites.contains(state.selectedItem?.url),
                    onWatchNow = { viewModel.setFullscreen(true) },
                    onToggleFavorite = { state.selectedItem?.let { viewModel.toggleFavorite(it) } },
                    onToggleRatio = { viewModel.toggleAspectRatio() },
                    onNext = { viewModel.nextChannel() },
                    onPrevious = { viewModel.previousChannel() }
                )
            }
        }
    }
}

@Composable
fun Sidebar(
    state: com.example.mediaplay.viewmodels.PlaylistUiState,
    onCategorySelected: (String) -> Unit,
    onTypeSelected: (String) -> Unit,
    onPlaylistSelected: (com.example.mediaplay.database.PlaylistItem) -> Unit,
    onAddPlaylist: () -> Unit,
    onDeletePlaylist: (com.example.mediaplay.database.PlaylistItem) -> Unit
) {
    var showPlaylistList by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(Color(0xFF1A1A1A))
            .padding(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF252525)),
            onClick = { showPlaylistList = !showPlaylistList }
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PlaylistPlay, null, tint = Color(0xFFBB86FC))
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("PLAYLIST", color = Color.Gray, fontSize = 10.sp)
                    Text(state.selectedPlaylist?.name ?: "Selecionar", color = Color.White, fontSize = 14.sp, maxLines = 1)
                }
                Icon(if (showPlaylistList) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null, tint = Color.Gray)
            }
        }

        if (showPlaylistList) {
            LazyColumn(modifier = Modifier.heightIn(max = 200.dp).padding(bottom = 8.dp)) {
                items(state.playlists) { playlist ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPlaylistSelected(playlist); showPlaylistList = false }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(playlist.name, color = if (playlist.id == state.selectedPlaylist?.id) Color(0xFFBB86FC) else Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        IconButton(onClick = { onDeletePlaylist(playlist) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Delete, null, tint = Color.DarkGray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                item {
                    TextButton(onClick = onAddPlaylist, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Nova Playlist", fontSize = 12.sp)
                    }
                }
            }
            HorizontalDivider(color = Color(0xFF333333))
            Spacer(Modifier.height(8.dp))
        }

        Text("CONTEÚDO", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp, bottom = 8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val types = listOf("LIVE" to Icons.Default.Tv, "MOVIE" to Icons.Default.Movie, "SERIES" to Icons.Default.VideoLibrary)
            types.forEach { (type, icon) ->
                val isSelected = state.selectedType == type
                Surface(
                    modifier = Modifier.weight(1f).height(45.dp).clickable { onTypeSelected(type) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) Color(0xFFBB86FC) else Color(0xFF252525)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = if (isSelected) Color.Black else Color.Gray, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        Text("CATEGORIAS", color = Color(0xFFBB86FC), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(items = state.categories, key = { it }, contentType = { "category" }) { category ->
                val isSelected = category == state.selectedCategory
                Text(
                    text = category,
                    color = if (isSelected) Color.White else Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .clickable { onCategorySelected(category) }
                        .padding(12.dp)
                        .background(if (isSelected) Color(0xFF3700B3).copy(alpha = 0.5f) else Color.Transparent, shape = RoundedCornerShape(8.dp)),
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun ChannelListSection(
    modifier: Modifier = Modifier,
    channels: List<MediaItem>,
    favoriteUrls: List<String>,
    selectedItem: MediaItem?,
    searchText: String,
    onSearchChanged: (String) -> Unit,
    onChannelSelected: (MediaItem) -> Unit,
    onToggleFavorite: (MediaItem) -> Unit,
    onLoadMore: () -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && lastIndex >= channels.size - 5 && channels.size >= 100) {
                    onLoadMore()
                }
            }
    }

    Column(modifier = modifier.background(Color(0xFF121212)).padding(8.dp)) {
        OutlinedTextField(
            value = searchText,
            onValueChange = onSearchChanged,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            placeholder = { Text("Buscar...", color = Color.Gray, fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFBB86FC), unfocusedBorderColor = Color(0xFF333333), focusedTextColor = Color.White, unfocusedTextColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        LazyColumn(state = listState, modifier = Modifier.weight(1f)) {
            items(items = channels, key = { it.url }, contentType = { "channel" }) { channel ->
                val isSelected = channel.url == selectedItem?.url
                val isFavorite = favoriteUrls.contains(channel.url)
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onChannelSelected(channel) },
                    colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFF333333) else Color(0xFF1E1E1E)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(model = channel.imageUrl, contentDescription = null, modifier = Modifier.size(50.dp).background(Color.Black, RoundedCornerShape(8.dp)), contentScale = ContentScale.Fit)
                        Spacer(Modifier.width(12.dp))
                        Text(channel.title, color = Color.White, maxLines = 2, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        IconButton(onClick = { onToggleFavorite(channel) }) {
                            Icon(imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.Star, contentDescription = null, tint = if (isFavorite) Color(0xFFFFD700) else Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FullscreenPlayer(
    item: MediaItem,
    aspectRatio: PlayerAspectRatio,
    onBack: () -> Unit,
    onToggleRatio: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    var showControls by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var currentPos by remember { mutableStateOf(0L) }
    var totalDuration by remember { mutableStateOf(0L) }
    var seekRequest by remember { mutableStateOf<Long?>(null) }
    var isDragging by remember { mutableStateOf(false) }
    var dragPos by remember { mutableStateOf(0f) }
    
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && !isDragging) {
            delay(5000)
            showControls = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { showControls = !showControls }
    ) {
        VideoPlayer(
            url = item.url,
            modifier = Modifier.fillMaxSize(),
            aspectRatio = aspectRatio,
            isPlaying = isPlaying,
            seekPosition = seekRequest,
            onProgress = { current, total ->
                if (!isDragging) {
                    currentPos = current
                    totalDuration = total
                }
            }
        )

        LaunchedEffect(seekRequest) {
            if (seekRequest != null) {
                delay(100)
                seekRequest = null
            }
        }

        AnimatedVisibility(visible = showControls, enter = fadeIn(), exit = fadeOut()) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.3f))) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color.Black.copy(0.6f)).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                    Column {
                        Text(item.title, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(item.groupTitle ?: "", color = Color.LightGray, fontSize = 12.sp)
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onToggleRatio) { Icon(Icons.Default.AspectRatio, null, tint = Color.White) }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().align(Alignment.Center).padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPrevious, modifier = Modifier.size(56.dp).background(Color.Black.copy(0.4f), CircleShape)) {
                        Icon(Icons.Default.SkipPrevious, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    
                    IconButton(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier.size(80.dp).background(Color(0xFFBB86FC).copy(0.9f), CircleShape)
                    ) {
                        Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = Color.Black, modifier = Modifier.size(48.dp))
                    }

                    IconButton(onClick = onNext, modifier = Modifier.size(56.dp).background(Color.Black.copy(0.4f), CircleShape)) {
                        Icon(Icons.Default.SkipNext, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }

                if (totalDuration > 0) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.8f))))
                            .padding(horizontal = 16.dp, vertical = 32.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(formatTime(if (isDragging) dragPos.toLong() else currentPos), color = Color.White, fontSize = 12.sp)
                            Text(formatTime(totalDuration), color = Color.White, fontSize = 12.sp)
                        }
                        Slider(
                            value = if (isDragging) dragPos else currentPos.toFloat(),
                            onValueChange = { 
                                isDragging = true
                                dragPos = it
                            },
                            onValueChangeFinished = {
                                isDragging = false
                                seekRequest = dragPos.toLong()
                            },
                            valueRange = 0f..totalDuration.toFloat().coerceAtLeast(1f),
                            colors = SliderDefaults.colors(thumbColor = Color(0xFFBB86FC), activeTrackColor = Color(0xFFBB86FC))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PreviewArea(
    modifier: Modifier = Modifier,
    selectedItem: MediaItem?,
    aspectRatio: PlayerAspectRatio,
    isFavorite: Boolean,
    onWatchNow: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleRatio: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(true) }
    
    Box(modifier = modifier.fillMaxHeight().background(Color.Black).padding(24.dp), contentAlignment = Alignment.Center) {
        if (selectedItem != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.fillMaxWidth().aspectRatio(16/9f)) {
                    Card(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(16.dp)) {
                        VideoPlayer(
                            url = selectedItem.url, 
                            modifier = Modifier.fillMaxSize(), 
                            aspectRatio = aspectRatio,
                            isPlaying = isPlaying
                        )
                    }
                    Box(modifier = Modifier.fillMaxSize().clickable { isPlaying = !isPlaying }) {
                        if (!isPlaying) {
                            Icon(Icons.Default.PlayArrow, null, tint = Color.White.copy(0.7f), modifier = Modifier.size(64.dp).align(Alignment.Center))
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(model = selectedItem.imageUrl, contentDescription = null, modifier = Modifier.size(60.dp), contentScale = ContentScale.Fit)
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(selectedItem.title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(selectedItem.groupTitle ?: "Sem Grupo", color = Color(0xFFBB86FC), fontSize = 14.sp)
                    }
                    IconButton(onClick = onToggleFavorite) {
                        Icon(imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.Star, contentDescription = null, tint = if (isFavorite) Color(0xFFFFD700) else Color.Gray)
                    }
                }
                EpgPanel(item = selectedItem)
                Spacer(Modifier.height(24.dp))
                Button(onClick = onWatchNow, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBB86FC)), modifier = Modifier.height(50.dp).width(200.dp), shape = RoundedCornerShape(25.dp)) {
                    Text("ASSISTIR AGORA", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    } else {
        "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }
}

@Composable
fun EpgPanel(item: MediaItem) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("PROGRAMAÇÃO", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(Color.Green, CircleShape))
                Spacer(Modifier.width(8.dp))
                Text("NO AR:", color = Color.LightGray, fontSize = 12.sp)
                Spacer(Modifier.width(4.dp))
                Text(item.title, color = Color.White, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun LoadingOverlay(progress: Float?) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.85f)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(300.dp)) {
            CircularProgressIndicator(color = Color(0xFFBB86FC))
            Spacer(Modifier.height(24.dp))
            Text("Sincronizando Canais...", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            if (progress != null && progress > 0f) {
                val percentage = (progress * 100).toInt().coerceIn(0, 100)
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth().height(8.dp), color = Color(0xFFBB86FC), trackColor = Color(0xFF333333), strokeCap = StrokeCap.Round)
                Spacer(Modifier.height(8.dp))
                Text("$percentage%", color = Color(0xFFBB86FC), fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun EmptyStateView(onAddClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0xFF1A1A1A), Color(0xFF000000)))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Surface(modifier = Modifier.size(120.dp), shape = CircleShape, color = Color(0xFFBB86FC).copy(0.1f)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.LibraryAdd, null, modifier = Modifier.size(56.dp), tint = Color(0xFFBB86FC)) }
            }
            Spacer(Modifier.height(32.dp))
            Text("Sua biblioteca está vazia", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(12.dp))
            Text("Adicione uma playlist M3U para começar.", color = Color.Gray, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(Modifier.height(40.dp))
            Button(onClick = onAddClick, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBB86FC), contentColor = Color.Black)) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(12.dp))
                Text("ADICIONAR PRIMEIRA PLAYLIST", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AddPlaylistDialog(
    onDismiss: () -> Unit,
    onConfirmM3U: (String, String) -> Unit,
    onConfirmXtream: (String, String, String, String) -> Unit
) {
    var tabIndex by remember { mutableStateOf(0) }
    var name by remember { mutableStateOf("") }
    
    // M3U State
    var m3uUrl by remember { mutableStateOf("") }
    
    // Xtream State
    var serverUrl by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Nova Playlist", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                TabRow(
                    selectedTabIndex = tabIndex,
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFFBB86FC),
                    divider = {}
                ) {
                    Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }) {
                        Text("Link M3U", modifier = Modifier.padding(12.dp), fontSize = 14.sp)
                    }
                    Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }) {
                        Text("Login Xtream", modifier = Modifier.padding(12.dp), fontSize = 14.sp)
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da Lista") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(Modifier.height(16.dp))

                if (tabIndex == 0) {
                    OutlinedTextField(
                        value = m3uUrl,
                        onValueChange = { m3uUrl = it },
                        label = { Text("URL M3U") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Link, null, tint = Color(0xFFBB86FC)) },
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = { serverUrl = it },
                        label = { Text("Servidor (URL)") },
                        placeholder = { Text("http://exemplo.com:8080") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Usuário") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Senha") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        if (tabIndex == 0 && m3uUrl.isNotBlank()) onConfirmM3U(name, m3uUrl)
                        else if (tabIndex == 1 && serverUrl.isNotBlank() && username.isNotBlank() && password.isNotBlank()) {
                            onConfirmXtream(name, serverUrl, username, password)
                        }
                    }
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBB86FC), contentColor = Color.Black)
            ) {
                Text("SALVAR", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR", color = Color.Gray) }
        },
        containerColor = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(24.dp)
    )
}
