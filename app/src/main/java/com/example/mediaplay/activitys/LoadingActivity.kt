package com.example.mediaplay

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.mediaplay.activitys.PlaylistActivity
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

        val url = intent.getStringExtra("url_m3u") ?: run {
            Log.e("M3U_DEBUG", "URL não recebida via Intent!")
            finish()
            return
        }

        Log.d("M3U_DEBUG", "URL recebida na LoadingActivity: $url")

        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
            Log.d("M3U_DEBUG", "Loading Status: $isLoading")
        }

        viewModel.playlist.observe(this) { items ->
            Log.d("M3U_DEBUG", "Itens parseados com OkHttp puro: ${items.size}")
            Log.d("M3U_DEBUG", "Itens recebidos na LoadingActivity: ${items.size}")

            PlaylistHolder.playlist = items

            Log.d("M3U_DEBUG", "Abrindo PlaylistActivity com ${items.size} itens")
            val intent = Intent(this, PlaylistActivity::class.java)
            startActivity(intent)
            finish()
        }

        viewModel.fetchM3U(url)
    }
}
