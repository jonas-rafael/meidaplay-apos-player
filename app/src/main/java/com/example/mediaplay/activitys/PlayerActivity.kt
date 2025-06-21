package com.example.mediaplay.activitys

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mediaplay.databinding.ActivityPlayerBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var player: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val url = intent.getStringExtra("url") ?: return
        val title = intent.getStringExtra("title") ?: "Reprodução"

        setTitle(title)
        setupPlayer(url)
    }

    private fun setupPlayer(url: String) {
        player = ExoPlayer.Builder(this).build()
        binding.playerView.player = player

        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
    }

    override fun onStop() {
        super.onStop()
        player.release()
    }
}
