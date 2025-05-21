package com.m335.wallpapergenerator

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.m335.wallpapergenerator.services.DatabaseService

class ImageViewActivity : AppCompatActivity() {
    private lateinit var imageUri: String
    private lateinit var imageDescription: String
    private lateinit var databaseService: DatabaseService
    private var isBound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view)

        // Service binden
        Intent(this, DatabaseService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStart() {
        super.onStart()

        imageUri = intent.getStringExtra("url") ?: ""
        imageDescription = intent.getStringExtra("description") ?: ""

        val image = findViewById<ImageView>(R.id.imageview_wallpaper)
        Glide.with(this)
            .load(imageUri.toUri())
            .placeholder(R.drawable.baseline_image_24)
            .error(R.drawable.baseline_image_not_supported_24)
            .into(image)

        val description = findViewById<TextView>(R.id.imageview_description)
        description.text = imageDescription
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as DatabaseService.LocalBinder
            databaseService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
        }
    }

    fun onButtonSetWallpaper(view: View) {
        Glide.with(this)
            .asBitmap()
            .load(imageUri)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                ) {
                    val wallpaperManager = WallpaperManager.getInstance(applicationContext)
                    try {
                        wallpaperManager.setBitmap(resource)
                        showToast("Wallpaper gesetzt!")
                        finish()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showToast("Fehler beim Setzen des Wallpapers.")
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    fun onButtonDelete(view: View) {
        if (!isBound) {
            showToast("Service nicht verbunden.")
            return
        }

        databaseService.deleteImage(imageUri)
        showToast("Bild gelöscht")
        finish()
    }

    fun onButtonBackToLibrary(view: View) {
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }
}
