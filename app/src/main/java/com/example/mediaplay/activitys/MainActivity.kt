package com.example.mediaplay

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mediaplay.databinding.ActivityMainBinding
import com.example.mediaplay.viewmodels.initialviewmodel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: initialviewmodel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.button.setOnClickListener {
            val urlDigitada = binding.edittextM3u.text.toString().trim()

            if (urlDigitada.isNotEmpty()) {
                Log.d("M3U_DEBUG", "Botão clicado! URL: $urlDigitada")

                val intent = Intent(this, LoadingActivity::class.java)
                intent.putExtra("url", urlDigitada)
                startActivity(intent)
            } else {
                Log.e("M3U_DEBUG", "URL vazia!")
            }
        }
    }
}
