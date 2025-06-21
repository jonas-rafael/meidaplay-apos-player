package com.example.mediaplay.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mediaplay.R
import com.example.mediaplay.activitys.PlayerActivity
import com.example.mediaplay.retrofit.M3UItem

class M3UAdapter(private var filteredList: List<M3UItem>) : RecyclerView.Adapter<M3UAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.text_title)
        val thumbnail: ImageView = view.findViewById(R.id.image_thumbnail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_m3u, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = filteredList[position]

        Log.d("M3U_DEBUG", "Exibindo posição: $position - ${item.title}")

        holder.title.text = item.title

        Glide.with(holder.thumbnail.context)
            .load(item.imageUrl ?: R.drawable.placeholder)
            .placeholder(R.drawable.placeholder)
            .into(holder.thumbnail)

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putExtra("title", item.title)
            intent.putExtra("url", item.url)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = filteredList.size

    fun updateList(newList: List<M3UItem>) {
        filteredList = newList
        notifyDataSetChanged()
    }
}
