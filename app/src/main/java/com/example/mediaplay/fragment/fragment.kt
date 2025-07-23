package com.example.mediaplay.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mediaplay.R
import com.example.mediaplay.model.MediaItem

class GroupFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private var items: List<MediaItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            items = it.getParcelableArrayList("items") ?: emptyList()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_group, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = MediaAdapter(items)
        return view
    }

    companion object {
        fun newInstance(items: ArrayList<MediaItem>): GroupFragment {
            val fragment = GroupFragment()
            val args = Bundle()
            args.putParcelableArrayList("items", items)
            fragment.arguments = args
            return fragment
        }
    }
}
