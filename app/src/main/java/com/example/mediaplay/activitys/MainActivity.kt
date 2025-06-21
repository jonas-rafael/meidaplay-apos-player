package com.example.mediaplay

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mediaplay.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button.setOnClickListener {
            val urlDigitada = binding.edittextM3u.text.toString().trim()
            if (urlDigitada.isNotEmpty()) {
                val intent = Intent(this, LoadingActivity::class.java)
                intent.putExtra("url_m3u", urlDigitada)
                startActivity(intent)
            }
        }
    }
}
