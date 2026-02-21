package com.autoyoutube.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.autoyoutube.R
import com.autoyoutube.data.YouTubeRepository
import com.autoyoutube.databinding.ActivityVideoPlayerBinding
import com.autoyoutube.model.Video
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Video Player Activity for playing YouTube videos
 * Uses ExoPlayer for media playback
 */
class VideoPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoPlayerBinding
    private var player: ExoPlayer? = null
    private lateinit var youTubeRepository: YouTubeRepository
    
    private var videoId: String = ""
    private var videoTitle: String = ""
    private var videoChannel: String = ""

    companion object {
        const val EXTRA_VIDEO_ID = "video_id"
        const val EXTRA_VIDEO_TITLE = "video_title"
        const val EXTRA_VIDEO_CHANNEL = "video_channel"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        youTubeRepository = YouTubeRepository(this)
        
        // Get intent extras
        videoId = intent.getStringExtra(EXTRA_VIDEO_ID) ?: ""
        videoTitle = intent.getStringExtra(EXTRA_VIDEO_TITLE) ?: "Unknown"
        videoChannel = intent.getStringExtra(EXTRA_VIDEO_CHANNEL) ?: "Unknown Channel"

        setupUI()
        loadVideo()
    }

    private fun setupUI() {
        binding.videoTitle.text = videoTitle
        binding.channelName.text = videoChannel
        
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.playPauseButton.setOnClickListener {
            player?.let {
                if (it.isPlaying) {
                    it.pause()
                } else {
                    it.play()
                }
            }
        }

        binding.fullscreenButton.setOnClickListener {
            // Toggle fullscreen
            Toast.makeText(this, "Fullscreen not available in demo", Toast.LENGTH_SHORT).show()
        }

        // Hide fullscreen button in demo mode
        binding.fullscreenButton.visibility = View.GONE
    }

    private fun loadVideo() {
        binding.loadingProgress.visibility = View.VISIBLE
        
        CoroutineScope(Dispatchers.Main).launch {
            // Try to get stream URL
            val streamUrl = withContext(Dispatchers.IO) {
                youTubeRepository.getStreamUrl(videoId)
            }
            
            binding.loadingProgress.visibility = View.GONE
            
            if (streamUrl != null) {
                initializePlayer(streamUrl)
            } else {
                // Show demo message
                binding.demoMessage.visibility = View.VISIBLE
                binding.demoMessage.text = getString(R.string.demo_playback_message)
                Toast.makeText(this@VideoPlayerActivity, R.string.demo_playback_notice, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initializePlayer(streamUrl: String) {
        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            binding.playerView.player = exoPlayer
            
            val mediaItem = MediaItem.fromUri(streamUrl)
            exoPlayer.setMediaItem(mediaItem)
            
            exoPlayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> {
                            binding.loadingProgress.visibility = View.VISIBLE
                        }
                        Player.STATE_READY -> {
                            binding.loadingProgress.visibility = View.GONE
                        }
                        Player.STATE_ENDED -> {
                            binding.playPauseButton.setImageResource(android.R.drawable.ic_media_play)
                        }
                        Player.STATE_IDLE -> {
                            // Do nothing
                        }
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) {
                        binding.playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
                    } else {
                        binding.playPauseButton.setImageResource(android.R.drawable.ic_media_play)
                    }
                }
            })
            
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    override fun onStart() {
        super.onStart()
        // Resume playback if needed
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            exoPlayer.release()
        }
        player = null
    }
}
