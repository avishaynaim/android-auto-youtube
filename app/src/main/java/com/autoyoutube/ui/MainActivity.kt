package com.autoyoutube.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.autoyoutube.R
import com.autoyoutube.data.YouTubeRepository
import com.autoyoutube.databinding.ActivityMainBinding
import com.autoyoutube.model.Video
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main Activity - Phone UI for YouTube
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var youTubeRepository: YouTubeRepository
    private lateinit var videoAdapter: VideoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        youTubeRepository = YouTubeRepository(this)
        
        setupUI()
        setupRecyclerView()
        loadContent()
    }

    private fun setupUI() {
        binding.titleText.text = getString(R.string.app_name)
        
        binding.searchButton.setOnClickListener {
            val query = binding.searchInput.text.toString()
            if (query.isNotEmpty()) {
                searchVideos(query)
            }
        }
        
        binding.swipeRefresh.setOnRefreshListener {
            loadContent()
        }
    }

    private fun setupRecyclerView() {
        videoAdapter = VideoAdapter { video ->
            onVideoClicked(video)
        }
        
        binding.videoRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = videoAdapter
        }
    }

    private fun loadContent() {
        binding.swipeRefresh.isRefreshing = true
        
        val videos = youTubeRepository.getHomeContent()
        
        binding.swipeRefresh.isRefreshing = false
        videoAdapter.submitList(videos)
        
        if (videos.isEmpty()) {
            binding.emptyView.visibility = View.VISIBLE
            binding.videoRecyclerView.visibility = View.GONE
        } else {
            binding.emptyView.visibility = View.GONE
            binding.videoRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun searchVideos(query: String) {
        if (query.isEmpty()) {
            Toast.makeText(this, R.string.search_hint, Toast.LENGTH_SHORT).show()
            return
        }
        
        binding.swipeRefresh.isRefreshing = true
        
        CoroutineScope(Dispatchers.Main).launch {
            val videos = youTubeRepository.searchVideos(query)
            
            binding.swipeRefresh.isRefreshing = false
            videoAdapter.submitList(videos)
            
            if (videos.isEmpty()) {
                Toast.makeText(this@MainActivity, R.string.no_results, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onVideoClicked(video: VideoItem) {
        Toast.makeText(this, "Playing: ${video.title}", Toast.LENGTH_SHORT).show()
    }
}
