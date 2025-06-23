package com.example.mediaplay.activitys

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediaplay.R
import com.example.mediaplay.adapter.M3UAdapter
import com.example.mediaplay.databinding.ActivityPlaylistBinding
import com.example.mediaplay.holder.PlaylistHolder
import com.example.mediaplay.viewmodels.PlaylistViewModel
import com.google.android.material.navigation.NavigationView

class PlaylistActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityPlaylistBinding
    private lateinit var viewModel: PlaylistViewModel
    private lateinit var adapter: M3UAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[PlaylistViewModel::class.java]
        viewModel.setFullList(PlaylistHolder.playlist)

        setupRecyclerView()
        setupDrawer()
        setupLoadMoreButton()
        setupSearch()

        viewModel.filteredList.observe(this) { list ->
            Log.d("M3U_DEBUG", "Exibindo ${list.size} de ${viewModel.getTotalCount()} itens")
            adapter.submitList(list)
            binding.btnLoadMore.visibility =
                if (list.size < viewModel.getTotalCount()) View.VISIBLE else View.GONE
        }

        viewModel.categories.observe(this) { categories ->
            val menu = binding.navView.menu
            menu.clear()
            menu.add("Todas as Categorias")
            categories.forEach { category -> menu.add(category) }
        }
    }

    private fun setupRecyclerView() {
        adapter = M3UAdapter(
            onItemClick = { item -> openPlayer(item) },
            onFavoriteClick = { item -> viewModel.toggleFavorite(item) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupDrawer() {
        setSupportActionBar(binding.toolbar)

        val toggle = androidx.appcompat.app.ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)
    }

    private fun setupLoadMoreButton() {
        binding.btnLoadMore.setOnClickListener {
            viewModel.loadMore()
        }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterByText(newText.orEmpty())
                return true
            }
        })
    }

    private fun openPlayer(item: com.example.mediaplay.retrofit.M3UItem) {
        val intent = android.content.Intent(this, PlayerActivity::class.java)
        intent.putExtra("title", item.title)
        intent.putExtra("url", item.url)
        startActivity(intent)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        viewModel.setCategory(item.title.toString())
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
