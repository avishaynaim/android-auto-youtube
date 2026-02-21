package com.autoyoutube.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.autoyoutube.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.title = "Settings"
    }
}
