package com.m335.wallpapergenerator.pages

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.m335.wallpapergenerator.ImageViewActivity
import com.m335.wallpapergenerator.R
import com.m335.wallpapergenerator.adapters.WallpaperAdapter
import com.m335.wallpapergenerator.services.DatabaseService
import android.widget.ImageView
import com.m335.wallpapergenerator.adapters.TemplateAdapter

class CollectionPageFragment : Fragment() {
    private lateinit var databaseService: DatabaseService
    private var isBound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Intent(context, DatabaseService::class.java).also { intent ->
            context?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as DatabaseService.LocalBinder
            databaseService = binder.getService()
            isBound = true

            val wallpapers = databaseService.getImages()
            if (wallpapers.isNotEmpty()) {
                val recyclerView = view?.findViewById<RecyclerView>(R.id.library_wallpapers)
                recyclerView?.layoutManager = GridLayoutManager(requireContext(), 2)
                recyclerView?.adapter = WallpaperAdapter(requireContext(), wallpapers.toList())
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_collection_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    private fun drawImages(container: LinearLayout, images: Map<String, String>) {
        container.removeAllViews()
        images.forEach { (uri, description) ->
            val imageView = ImageView(requireContext()).apply {
                val layoutParams = LinearLayout.LayoutParams(
                    dpToPx(500),
                    dpToPx(500)
                )
                val margin = dpToPx(20)
                layoutParams.setMargins(margin, margin, margin, margin)
                this.layoutParams = layoutParams

                Glide.with(this@CollectionPageFragment)
                    .load(uri)
                    .placeholder(R.drawable.baseline_image_24)
                    .error(R.drawable.baseline_image_not_supported_24)
                        .into(this)

                setOnClickListener {
                    startImageViewActivity(uri, description)
                }
            }
            container.addView(imageView)
        }
    }

    private fun startImageViewActivity(url: String, description: String) {
        val intent = Intent(requireContext(), ImageViewActivity::class.java).apply {
            putExtra("url", url)
            putExtra("description", description)
        }
        startActivity(intent)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            context?.unbindService(connection)
            isBound = false
        }
    }
}
