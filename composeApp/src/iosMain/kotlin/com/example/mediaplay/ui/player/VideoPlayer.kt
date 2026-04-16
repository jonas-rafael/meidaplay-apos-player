package com.example.mediaplay.ui.player

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import com.example.mediaplay.viewmodels.PlayerAspectRatio
import platform.AVFoundation.*
import platform.AVKit.*
import platform.CoreMedia.*
import platform.Foundation.NSURL
import platform.UIKit.UIView
import platform.UIKit.UIDevice
import kotlinx.cinterop.CValue
import platform.CoreGraphics.CGRect

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
    val player = remember { AVPlayer() }
    val playerLayer = remember { AVPlayerLayer.playerLayerWithPlayer(player) }
    val playerViewController = remember { AVPlayerViewController() }

    LaunchedEffect(url) {
        val nsUrl = NSURL.URLWithString(url)
        if (nsUrl != null) {
            val playerItem = AVPlayerItem.playerItemWithURL(nsUrl)
            player.replaceCurrentItemWithPlayerItem(playerItem)
            player.play()
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) player.play() else player.pause()
    }

    LaunchedEffect(seekPosition) {
        seekPosition?.let {
            val time = CMTimeMake(it, 1000)
            player.seekToTime(time)
        }
    }

    // Loop de progresso
    LaunchedEffect(Unit) {
        while (true) {
            val current = player.currentTime()
            val duration = player.currentItem?.duration
            
            if (duration != null) {
                val curSecs = CMTimeGetSeconds(current)
                val durSecs = CMTimeGetSeconds(duration)
                if (!curSecs.isNaN() && !durSecs.isNaN() && durSecs > 0) {
                    onProgress((curSecs * 1000).toLong(), (durSecs * 1000).toLong())
                }
            }
            kotlinx.coroutines.delay(1000)
        }
    }

    UIKitView(
        factory = {
            val container = UIView()
            playerViewController.player = player
            playerViewController.showsPlaybackControls = false
            container.addSubview(playerViewController.view)
            container
        },
        modifier = modifier,
        update = { view ->
            playerViewController.view.setFrame(view.bounds)
            playerLayer.videoGravity = when (aspectRatio) {
                PlayerAspectRatio.FIT -> AVLayerVideoGravityResizeAspect
                PlayerAspectRatio.FILL -> AVLayerVideoGravityResizeAspectFill
                else -> AVLayerVideoGravityResizeAspect
            }
        }
    )
}
