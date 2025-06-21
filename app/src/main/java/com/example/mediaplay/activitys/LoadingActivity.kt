package com.example.mediaplay

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.mediaplay.databinding.ActivityLoadingBinding
import com.example.mediaplay.holder.PlaylistHolder
import com.example.mediaplay.viewmodels.LoadingViewModel

class LoadingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoadingBinding
    private val viewModel: LoadingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val url = intent.getStringExtra("url") ?: return

        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        }

        viewModel.playlist.observe(this) { items ->
            Log.d("M3U_DEBUG", "Itens recebidos na LoadingActivity: ${items.size}")
            PlaylistHolder.playlist = items
            val intent = Intent(this, PlaylistActivity::class.java)
            startActivity(intent)
            finish()
        }

        viewModel.fetchM3U(url)
    }
}
