package com.autoyoutube.ui.settings

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.autoyoutube.R
import com.autoyoutube.databinding.ActivitySettingsBinding

/**
 * Settings Activity for configuring the app
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    companion object {
        const val PREFS_NAME = "AutoYouTubePrefs"
        const val PREF_API_KEY = "youtube_api_key"
        const val PREF_DARK_MODE = "dark_mode"
        const val PREF_AUTO_PLAY = "auto_play"
        const val PREF_DEMO_MODE = "demo_mode"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        loadSettings()
    }

    private fun setupUI() {
        binding.toolbar.title = "Settings"
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // API Key input
        binding.apiKeyInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                saveApiKey()
            }
        }

        // Demo mode switch
        binding.demoModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putBoolean(PREF_DEMO_MODE, isChecked)
                .apply()
            updateDemoModeStatus(isChecked)
        }

        // Dark mode switch
        binding.darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putBoolean(PREF_DARK_MODE, isChecked)
                .apply()
            Toast.makeText(this, "Dark mode will apply on restart", Toast.LENGTH_SHORT).show()
        }

        // Auto-play switch
        binding.autoPlaySwitch.setOnCheckedChangeListener { _, isChecked ->
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putBoolean(PREF_AUTO_PLAY, isChecked)
                .apply()
        }

        // Sign in button - disabled in demo mode
        binding.signInButton.isEnabled = false
        binding.signInButton.text = "Sign In (N/A in Demo)"

        // Sign out button
        binding.signOutButton.isEnabled = false

        // About button
        binding.aboutButton.setOnClickListener {
            showAboutDialog()
        }
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        // Load API key
        val apiKey = prefs.getString(PREF_API_KEY, "")
        binding.apiKeyInput.setText(apiKey)

        // Load switches
        binding.demoModeSwitch.isChecked = prefs.getBoolean(PREF_DEMO_MODE, true)
        binding.darkModeSwitch.isChecked = prefs.getBoolean(PREF_DARK_MODE, false)
        binding.autoPlaySwitch.isChecked = prefs.getBoolean(PREF_AUTO_PLAY, true)

        updateDemoModeStatus(binding.demoModeSwitch.isChecked)
        updateAuthStatus()
    }

    private fun saveApiKey() {
        val apiKey = binding.apiKeyInput.text.toString().trim()
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putString(PREF_API_KEY, apiKey)
            .apply()

        if (apiKey.isNotEmpty() && apiKey != "YOUR_API_KEY_HERE") {
            Toast.makeText(this, "API key saved. Restart app to apply.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateDemoModeStatus(enabled: Boolean) {
        binding.apiKeyInput.isEnabled = !enabled
        binding.apiKeyLayout.hint = if (enabled) {
            "Demo mode - API key not required"
        } else {
            "YouTube API Key"
        }
    }

    private fun updateAuthStatus() {
        binding.authStatus.text = "Demo mode - Sign in unavailable"
        binding.authStatus.setTextColor(getColor(R.color.gray))
    }

    private fun showAboutDialog() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
        dialog.setTitle(R.string.app_name)
        dialog.setMessage("""
            AutoYouTube v1.0
            
            YouTube integration for Android Auto
            
            Features:
            • Browse YouTube in your car
            • Search videos
            • View playlists
            • Media playback controls
            
            Note: Full video playback requires
            YouTube Premium for background audio.
            
            © 2024
        """.trimIndent())
        dialog.setPositiveButton("OK", null)
        dialog.show()
    }
}
