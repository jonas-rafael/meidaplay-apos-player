package com.example.mediaplay.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mediaplay.activitys.PlayerActivity
import com.example.mediaplay.databinding.ItemM3uBinding
import com.example.mediaplay.retrofit.M3UItem

class M3UAdapter : ListAdapter<M3UItem, M3UAdapter.M3UViewHolder>(DiffCallback()) {

    inner class M3UViewHolder(private val binding: ItemM3uBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: M3UItem) {
            binding.textViewTitle.text = item.title

            Glide.with(binding.imageThumbnail.context)
                .load(item.imageUrl ?: com.example.mediaplay.R.drawable.placeholder)
                .placeholder(com.example.mediaplay.R.drawable.placeholder)
                .into(binding.imageThumbnail)

            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, PlayerActivity::class.java)
                intent.putExtra("title", item.title)
                intent.putExtra("url", item.url)
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): M3UViewHolder {
        val binding = ItemM3uBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return M3UViewHolder(binding)
    }

    override fun onBindViewHolder(holder: M3UViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<M3UItem>() {
        override fun areItemsTheSame(oldItem: M3UItem, newItem: M3UItem): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: M3UItem, newItem: M3UItem): Boolean {
            return oldItem == newItem
        }
    }
}
