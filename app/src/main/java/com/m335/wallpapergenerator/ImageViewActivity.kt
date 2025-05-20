package com.m335.wallpapergenerator

import android.Manifest
import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.m335.wallpapergenerator.data.ImageStorage

class ImageViewActivity : AppCompatActivity() {
    private lateinit var imageUri: String
    private lateinit var imageDescription: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view)

        imageUri = intent.getStringExtra("url") ?: ""
        imageDescription = intent.getStringExtra("description") ?: ""

        val image = findViewById<ImageView>(R.id.imageview_wallpaper)
        Glide.with(this)
            .load(imageUri.toUri())
            .placeholder(R.drawable.baseline_image_24)
            .error(R.drawable.baseline_image_not_supported_24)
            .into(image)

        findViewById<TextView>(R.id.imageview_description).text = imageDescription
    }

    fun onButtonSetWallpaper(view: View) {
        Glide.with(this)
            .asBitmap()
            .load(imageUri)
            .into(object : CustomTarget<Bitmap>() {
                @RequiresPermission(Manifest.permission.SET_WALLPAPER)
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    try {
                        val wallpaperManager = WallpaperManager.getInstance(applicationContext)
                        wallpaperManager.setBitmap(resource)
                        showToast("Wallpaper wurde erfolgreich gesetzt!")
                        finish()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showToast("Fehler beim Setzen des Wallpapers.")
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Kein Cleanup nötig
                }
            })
    }

    fun onButtonDelete(view: View) {
        ImageStorage.deleteImage(this, imageUri)
        showToast("Bild gelöscht.")
        finish()
    }

    fun onButtonBackToLibrary(view: View) {
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
