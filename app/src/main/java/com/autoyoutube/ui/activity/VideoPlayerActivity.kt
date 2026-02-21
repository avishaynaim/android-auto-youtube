package com.autoyoutube.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.autoyoutube.databinding.ActivityVideoPlayerBinding

class VideoPlayerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
