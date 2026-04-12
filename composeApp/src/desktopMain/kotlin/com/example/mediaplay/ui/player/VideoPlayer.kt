package com.example.mediaplay.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.mediaplay.viewmodels.PlayerAspectRatio

@Composable
actual fun VideoPlayer(
    url: String,
    modifier: Modifier,
    aspectRatio: PlayerAspectRatio,
    isPlaying: Boolean,
    onProgress: (current: Long, total: Long) -> Unit,
    onFinished: () -> Unit
) {
    Box(
        modifier = modifier.background(Color.DarkGray),
        contentAlignment = Alignment.Center
    ) {
        Text("Desktop Player - $aspectRatio", color = Color.White)
    }
}
