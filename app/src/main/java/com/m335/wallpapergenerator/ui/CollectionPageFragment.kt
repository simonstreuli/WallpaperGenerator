package com.m335.wallpapergenerator.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.m335.wallpapergenerator.ImageViewActivity
import com.m335.wallpapergenerator.R
import com.m335.wallpapergenerator.adapters.WallpaperAdapter
import com.m335.wallpapergenerator.services.DatabaseService

class CollectionPageFragment : Fragment() {
    private lateinit var databaseService: DatabaseService
    private var isBound = false
    private var rootView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as DatabaseService.LocalBinder
            databaseService = binder.getService()
            isBound = true
            tryLoadWallpapers()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_collection_page, container, false)
        rootView = view

        Intent(requireContext(), DatabaseService::class.java).also { intent ->
            requireContext().bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tryLoadWallpapers()
    }

    private fun tryLoadWallpapers() {
        if (!isBound || rootView == null) return

        val wallpapers = databaseService.getImages()
        val recyclerView = rootView!!.findViewById<RecyclerView>(R.id.library_wallpapers)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        if (wallpapers.isNotEmpty()) {
            recyclerView.adapter = WallpaperAdapter(requireContext(), wallpapers.toList())
        }
    }

    private fun startImageViewActivity(url: String, description: String) {
        val intent = Intent(requireContext(), ImageViewActivity::class.java).apply {
            putExtra("url", url)
            putExtra("description", description)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isBound) {
            context?.unbindService(connection)
            isBound = false
        }
        rootView = null
    }
}
