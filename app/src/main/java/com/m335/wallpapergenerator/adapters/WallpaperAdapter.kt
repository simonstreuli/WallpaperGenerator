package com.m335.wallpapergenerator.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.m335.wallpapergenerator.ImageViewActivity
import com.m335.wallpapergenerator.R

class WallpaperAdapter(
    private val context: Context,
    private val items: List<Pair<String, String>>
) : RecyclerView.Adapter<WallpaperAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.wallpaper_image)

        init {
            imageView.setOnClickListener {
                val (url, description) = items[adapterPosition]
                val intent = Intent(context, ImageViewActivity::class.java).apply {
                    putExtra("url", url)
                    putExtra("description", description)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_wallpaper, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (url, _) = items[position]
        Glide.with(context)
            .load(url)
            .placeholder(R.drawable.baseline_image_24)
            .error(R.drawable.baseline_image_not_supported_24)
            .into(holder.imageView)
    }

    override fun getItemCount() = items.size
}
