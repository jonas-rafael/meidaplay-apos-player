package com.example.mediaplay.activitys

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediaplay.R
import com.example.mediaplay.adapter.M3UAdapter
import com.example.mediaplay.databinding.ActivityPlaylistBinding
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

        setupRecyclerView()
        setupNavigationDrawer()

        // Observa lista filtrada
        viewModel.filteredList.observe(this) { list ->
            adapter.submitList(list)
        }

        // Observa categorias
        viewModel.categories.observe(this) { categories ->
            val menu = binding.navView.menu
            menu.clear()
            menu.add("Todas as Categorias")
            categories.forEach { category ->
                menu.add(category)
            }
        }

        // Inicializa lista completa recebida da LoadingActivity
        viewModel.setFullList(com.example.mediaplay.holder.PlaylistHolder.playlist)
    }

    private fun setupRecyclerView() {
        adapter = M3UAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupNavigationDrawer() {
        setSupportActionBar(binding.toolbar)

        val toggle = ActionBarDrawerToggle(
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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        viewModel.setCategory(item.title.toString())
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
