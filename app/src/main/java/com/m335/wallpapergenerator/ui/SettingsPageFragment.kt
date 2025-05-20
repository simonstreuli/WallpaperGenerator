package com.m335.wallpapergenerator.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.m335.wallpapergenerator.LoginActivity
import com.m335.wallpapergenerator.R
import com.m335.wallpapergenerator.data.ApiKeyStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsPageFragment : Fragment() {

    private lateinit var clearApiKeyButton: AppCompatButton
    private lateinit var setApiKeyButton: AppCompatButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_settings_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        clearApiKeyButton = view.findViewById(R.id.settings_button_reset_apikey)
        setApiKeyButton = view.findViewById(R.id.settings_button_set_apikey)

        clearApiKeyButton.setOnClickListener {
            lifecycleScope.launch {
                ApiKeyStore.setApiKey(requireContext(), "")
                Toast.makeText(requireContext(), "API-Schlüssel gelöscht", Toast.LENGTH_SHORT).show()
                updateButtonVisibility()
            }
        }

        setApiKeyButton.setOnClickListener {
            val loginActivityIntent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(loginActivityIntent)
        }

        updateButtonVisibility()
    }

    override fun onResume() {
        super.onResume()
        updateButtonVisibility()
    }

    private fun updateButtonVisibility() {
        lifecycleScope.launch {
            val apiKey = ApiKeyStore.getApiKey(requireContext()).first()
            val hasKey = apiKey.isNotEmpty()

            clearApiKeyButton.visibility = if (hasKey) View.VISIBLE else View.GONE
            setApiKeyButton.visibility = if (hasKey) View.GONE else View.VISIBLE
        }
    }
}
