package com.example.mediaplay.activitys

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
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
        setupSearchView()

        viewModel.filteredList.observe(this) { list ->
            adapter.submitList(list)
            binding.btnLoadMore.visibility =
                if (list.size < PlaylistHolder.playlist.size) View.VISIBLE else View.GONE
        }

        viewModel.categories.observe(this) { categories ->
            val menu = binding.navView.menu
            menu.clear()
            menu.add("Todas as Categorias")
            categories.forEach { category -> menu.add(category) }
        }
    }

    private fun setupRecyclerView() {
        adapter = M3UAdapter()
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

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.filterByText(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterByText(newText.orEmpty())
                return true
            }
        })
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        viewModel.setCategory(item.title.toString())
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
