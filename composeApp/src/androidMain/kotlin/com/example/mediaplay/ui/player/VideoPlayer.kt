package com.example.mediaplay.ui.player

import androidx.annotation.OptIn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.mediaplay.viewmodels.PlayerAspectRatio
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
actual fun VideoPlayer(
    url: String,
    modifier: Modifier,
    aspectRatio: PlayerAspectRatio,
    isPlaying: Boolean,
    seekPosition: Long?,
    onProgress: (current: Long, total: Long) -> Unit,
    onFinished: () -> Unit
) {
    val context = LocalContext.current

    val exoPlayer = remember {
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
            .setAllowCrossProtocolRedirects(true)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .build().apply {
                playWhenReady = true
            }
    }

    // Lógica de Seek (Pular para um tempo)
    LaunchedEffect(seekPosition) {
        seekPosition?.let {
            exoPlayer.seekTo(it)
        }
    }

    // Gerenciar Play/Pause
    LaunchedEffect(isPlaying) {
        if (isPlaying) exoPlayer.play() else exoPlayer.pause()
    }

    // Reportar progresso
    LaunchedEffect(exoPlayer, isPlaying) {
        while (true) {
            if (exoPlayer.isPlaying) {
                onProgress(exoPlayer.currentPosition, exoPlayer.duration.coerceAtLeast(0L))
            }
            delay(1000)
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) onFinished()
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    LaunchedEffect(url) {
        if (url.isNotBlank()) {
            val mediaItem = MediaItem.fromUri(url)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
                useController = false 
            }
        },
        update = { playerView ->
            playerView.resizeMode = when (aspectRatio) {
                PlayerAspectRatio.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                PlayerAspectRatio.FILL -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                PlayerAspectRatio.SIXTEEN_NINE -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                PlayerAspectRatio.FOUR_THREE -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
            }
        }
    )
}
