package com.example.mediaplay

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediaplay.adapter.M3UAdapter
import com.example.mediaplay.databinding.ActivityPlaylistBinding
import com.example.mediaplay.holder.PlaylistHolder
import com.example.mediaplay.viewmodels.PlaylistViewModel

class PlaylistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaylistBinding
    private val viewModel: PlaylistViewModel by viewModels()
    private lateinit var adapter: M3UAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = M3UAdapter(emptyList())
        binding.recyclerView.adapter = adapter

        // Passa a lista completa para a ViewModel
        val playlist = PlaylistHolder.playlist
        viewModel.setFullList(playlist)

        // Observa as atualizações filtradas
        viewModel.filteredList.observe(this) { items ->
            adapter.updateList(items)
        }

        // Faz o Drawer abrir ao clicar no botão Hamburguer
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(binding.navigationView)
        }

        // Preenche o Drawer com as categorias
        populateDrawerMenu()

        // Configura o filtro de busca por texto
        setupSearchFilter()
    }

    private fun populateDrawerMenu() {
        val categories = PlaylistHolder.playlist.mapNotNull { it.groupTitle }.distinct()
        val menu = binding.navigationView.menu
        menu.clear()

        // Adiciona opção "Todas"
        menu.add("Todas").setOnMenuItemClickListener {
            viewModel.setCategory(null)
            binding.drawerLayout.closeDrawers()
            true
        }

        categories.forEach { category ->
            menu.add(category).setOnMenuItemClickListener {
                viewModel.setCategory(category)
                binding.drawerLayout.closeDrawers()
                true
            }
        }
    }

    private fun setupSearchFilter() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                viewModel.setSearchQuery(s.toString())
            }
        })
    }
}
