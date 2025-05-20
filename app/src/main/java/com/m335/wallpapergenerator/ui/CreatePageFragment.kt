package com.m335.wallpapergenerator.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.m335.wallpapergenerator.GenerateActivity
import com.m335.wallpapergenerator.LoginActivity
import com.m335.wallpapergenerator.R
import com.m335.wallpapergenerator.data.ApiKeyStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CreatePageFragment : Fragment() {
    private lateinit var navController: NavController

    private val loginActivityResultListener =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_CANCELED) {
                navController.navigate(R.id.collectionPageFragment)
            }
        }

    private val generateActivityResultListener =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                navController.navigate(R.id.collectionPageFragment)
                view?.findViewById<EditText>(R.id.create_input_imagination)?.text?.clear()
            } else {
                Toast.makeText(requireActivity(), "Die Generierung wurde abgebrochen oder ist abgestürzt.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_create_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = view.findNavController()

        val generateWallpaperButton = view.findViewById<AppCompatButton>(R.id.create_button_wallpaper)
        val generateWidgetButton = view.findViewById<AppCompatButton>(R.id.create_button_widget)
        val inputImageImagination = view.findViewById<EditText>(R.id.create_input_imagination)

        generateWallpaperButton.setOnClickListener {
            if (validateImaginationInputText(inputImageImagination.text.toString())) {
                startGenerationActivity(inputImageImagination, true)
            }
        }

        generateWidgetButton.setOnClickListener {
            if (validateImaginationInputText(inputImageImagination.text.toString())) {
                startGenerationActivity(inputImageImagination, false)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // Prüfe ob API Key gesetzt ist
        lifecycleScope.launch {
            val apiKey = ApiKeyStore.getApiKey(requireContext()).first()
            if (apiKey.isEmpty()) {
                val intent = Intent(requireActivity(), LoginActivity::class.java)
                loginActivityResultListener.launch(intent)
            }
        }
    }

    private fun startGenerationActivity(inputImageImagination: EditText, isWallpaper: Boolean) {
        val generateActivityIntent = Intent(requireActivity(), GenerateActivity::class.java).apply {
            putExtra("description", inputImageImagination.text.toString())
            putExtra("isWallpaper", isWallpaper)
        }
        generateActivityResultListener.launch(generateActivityIntent)
    }

    private fun validateImaginationInputText(text: String): Boolean {
        return when {
            text.isEmpty() -> {
                showToast("Bitte gib deine kreative Vorstellung ein.")
                false
            }
            text.length < 20 -> {
                showToast("Bitte mindestens 20 Zeichen eingeben.")
                false
            }
            else -> true
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
