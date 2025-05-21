package com.m335.wallpapergenerator.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.m335.wallpapergenerator.R
import com.m335.wallpapergenerator.adapters.TemplateAdapter

class TemplatePageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_template_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val templates = listOf(
            "https://images.unsplash.com/photo-1506744038136-46273834b3fb" to "Berglandschaft mit See",
            "https://images.unsplash.com/photo-1507525428034-b723cf961d3e" to "Sonnenuntergang am Meer",
            "https://images.unsplash.com/photo-1470770841072-f978cf4d019e" to "Waldweg im Nebel",
            "https://images.unsplash.com/photo-1501785888041-af3ef285b470" to "Wüstenlandschaft mit Dünen",
            "https://images.unsplash.com/photo-1504384308090-c894fdcc538d" to "Abstrakte Lichtmalerei",
            "https://images.unsplash.com/photo-1496307042754-b4aa456c4a2d" to "Farbige Rauchwolken",
            "https://images.unsplash.com/photo-1501594907352-04cda38ebc29" to "Neonlichter in der Stadt",
        )

        val recyclerView = view.findViewById<RecyclerView>(R.id.template_recycler)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = TemplateAdapter(requireContext(), templates)
    }
}
