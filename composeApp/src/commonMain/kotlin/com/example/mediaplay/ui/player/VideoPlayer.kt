package com.example.mediaplay.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.mediaplay.viewmodels.PlayerAspectRatio

@Composable
expect fun VideoPlayer(
    url: String,
    modifier: Modifier = Modifier,
    aspectRatio: PlayerAspectRatio = PlayerAspectRatio.FIT,
    isPlaying: Boolean = true,
    seekPosition: Long? = null, // Novo: Posição para onde o player deve pular
    onProgress: (current: Long, total: Long) -> Unit = { _, _ -> },
    onFinished: () -> Unit = {}
)
