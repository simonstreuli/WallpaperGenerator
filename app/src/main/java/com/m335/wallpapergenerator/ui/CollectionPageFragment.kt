package com.m335.wallpapergenerator.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.m335.wallpapergenerator.ImageViewActivity
import com.m335.wallpapergenerator.R
import com.m335.wallpapergenerator.data.ImageStorage

class CollectionPageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_collection_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.collection_recycler)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        val images = ImageStorage.getImages(requireContext())

        if (images.isEmpty()) {
            view.findViewById<TextView>(R.id.collection_title).text = "Keine Bilder gefunden"
        }

        recyclerView.adapter = CollectionAdapter(images.toList()) { url, desc ->
            val intent = Intent(requireContext(), ImageViewActivity::class.java)
            intent.putExtra("url", url)
            intent.putExtra("description", desc)
            startActivity(intent)
        }
    }

    class CollectionAdapter(
        private val items: List<Pair<String, String>>,
        private val onClick: (String, String) -> Unit
    ) : RecyclerView.Adapter<CollectionAdapter.ImageViewHolder>() {

        class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val image = view.findViewById<android.widget.ImageView>(R.id.item_image)
            val title = view.findViewById<TextView>(R.id.item_title)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_collection_image, parent, false)
            return ImageViewHolder(view)
        }

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            val (url, desc) = items[position]
            Glide.with(holder.itemView)
                .load(url)
                .placeholder(R.drawable.baseline_image_24)
                .error(R.drawable.baseline_image_not_supported_24)
                .into(holder.image)

            holder.title.text = "Titel"
            holder.itemView.setOnClickListener { onClick(url, desc) }
        }

        override fun getItemCount(): Int = items.size
    }
}
