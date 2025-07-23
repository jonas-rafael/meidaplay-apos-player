package com.example.mediaplay.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mediaplay.fragments.GroupFragment
import com.example.mediaplay.model.MediaItem

class GroupPagerAdapter(
    fa: FragmentActivity,
    private val groupedData: Map<String, List<MediaItem>>
) : FragmentStateAdapter(fa) {

    private val keys = groupedData.keys.toList()

    override fun getItemCount(): Int = keys.size

    override fun createFragment(position: Int): Fragment {
        val key = keys[position]
        val items = groupedData[key] ?: emptyList()
        return GroupFragment.newInstance(ArrayList(items))
    }

    fun getTabTitle(position: Int): String = keys[position]
}
