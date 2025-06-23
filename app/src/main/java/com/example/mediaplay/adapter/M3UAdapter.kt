package com.example.mediaplay.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mediaplay.R
import com.example.mediaplay.activitys.PlayerActivity
import com.example.mediaplay.databinding.ItemM3uBinding
import com.example.mediaplay.retrofit.M3UItem

class M3UAdapter(
    private val onItemClick: (M3UItem) -> Unit,
    private val onFavoriteClick: (M3UItem) -> Unit
) : ListAdapter<M3UItem, M3UAdapter.M3UViewHolder>(DiffCallback()) {

    inner class M3UViewHolder(private val binding: ItemM3uBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: M3UItem) {
            binding.textViewTitle.text = item.title
            binding.textViewCategory.text = item.groupTitle ?: ""

            Glide.with(binding.imageThumbnail.context)
                .load(item.imageUrl ?: R.drawable.placeholder)
                .placeholder(R.drawable.placeholder)
                .into(binding.imageThumbnail)

            // Ícone de favorito
            val iconRes = if (item.isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
            binding.imageFavorite.setImageResource(iconRes)

            // Clique para abrir o Player
            binding.root.setOnClickListener {
                onItemClick(item)
            }

            // Clique no coração para favoritar
            binding.imageFavorite.setOnClickListener {
                onFavoriteClick(item)
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
