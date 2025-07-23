package com.example.mediaplay.activitys

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mediaplay.adapters.GroupPagerAdapter
import com.example.mediaplay.databinding.ActivityGroupsBinding
import com.example.mediaplay.model.MediaItem
import com.google.android.material.tabs.TabLayoutMediator

class GroupsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGroupsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mediaItems = listOf(
            MediaItem("Breaking Bad", "http://url/serie1.m3u8", "Series"),
            MediaItem("Game of Thrones", "http://url/serie2.m3u8", "Series"),
            MediaItem("Velozes e Furiosos", "http://url/filme1.m3u8", "Filmes")
        )

        val grouped = mediaItems.groupBy { it.groupTitle }
        val adapter = GroupPagerAdapter(this, grouped)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = adapter.getTabTitle(position)
        }.attach()
    }
}
