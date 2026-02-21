package com.autoyoutube.ui.settings

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.autoyoutube.R
import com.autoyoutube.databinding.ActivitySettingsBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.youtube.YouTubeScopes

/**
 * Settings Activity for configuring the app
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var googleSignInClient: GoogleSignInClient

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

        // Sign in button
        binding.signInButton.setOnClickListener {
            signInWithGoogle()
        }

        // Sign out button
        binding.signOutButton.setOnClickListener {
            signOutGoogle()
        }

        // About button
        binding.aboutButton.setOnClickListener {
            showAboutDialog()
        }

        // Setup Google Sign-In
        setupGoogleSignIn()
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(YouTubeScopes.YOUTUBE_READONLY))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        updateAuthStatus()
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
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            binding.signInButton.isEnabled = false
            binding.signOutButton.isEnabled = true
            binding.authStatus.text = "Signed in as: ${account.email}"
            binding.authStatus.setTextColor(getColor(R.color.green))
        } else {
            binding.signInButton.isEnabled = true
            binding.signOutButton.isEnabled = false
            binding.authStatus.text = "Not signed in"
            binding.authStatus.setTextColor(getColor(R.color.red))
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, 9001)
    }

    private fun signOutGoogle() {
        googleSignInClient.signOut()
            .addOnCompleteListener(this) {
                updateAuthStatus()
                Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show()
            }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 9001) {
            updateAuthStatus()
        }
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
