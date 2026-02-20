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
import kotlinx.coroutines.withContext

/**
 * Main Activity - Phone UI for YouTube
 * This is the traditional Android app interface
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var youTubeRepository: YouTubeRepository
    private lateinit var videoAdapter: VideoAdapter

    private var currentTab = Tab.HOME

    enum class Tab {
        HOME, TRENDING, SEARCH, PLAYLISTS
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        youTubeRepository = YouTubeRepository(this)
        
        setupUI()
        setupRecyclerView()
        setupBottomNavigation()
        
        // Load initial content
        loadContent(Tab.HOME)
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
            loadContent(currentTab)
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

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    currentTab = Tab.HOME
                    binding.titleText.text = getString(R.string.home)
                    loadContent(Tab.HOME)
                    true
                }
                R.id.nav_trending -> {
                    currentTab = Tab.TRENDING
                    binding.titleText.text = getString(R.string.trending)
                    loadContent(Tab.TRENDING)
                    true
                }
                R.id.nav_search -> {
                    currentTab = Tab.SEARCH
                    binding.titleText.text = getString(R.string.search)
                    binding.searchInput.visibility = View.VISIBLE
                    true
                }
                R.id.nav_playlists -> {
                    currentTab = Tab.PLAYLISTS
                    binding.titleText.text = getString(R.string.library)
                    loadContent(Tab.PLAYLISTS)
                    true
                }
                else -> false
            }
        }
    }

    private fun loadContent(tab: Tab) {
        binding.swipeRefresh.isRefreshing = true
        
        CoroutineScope(Dispatchers.Main).launch {
            val videos = withContext(Dispatchers.IO) {
                when (tab) {
                    Tab.HOME -> youTubeRepository.getHomeContent()
                    Tab.TRENDING -> youTubeRepository.getTrendingContent()
                    Tab.SEARCH -> youTubeRepository.searchVideos(binding.searchInput.text.toString())
                    Tab.PLAYLISTS -> youTubeRepository.getPlaylistVideos("PL1")
                }
            }
            
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
    }

    private fun searchVideos(query: String) {
        binding.swipeRefresh.isRefreshing = true
        
        CoroutineScope(Dispatchers.Main).launch {
            val videos = withContext(Dispatchers.IO) {
                youTubeRepository.searchVideos(query)
            }
            
            binding.swipeRefresh.isRefreshing = false
            videoAdapter.submitList(videos)
            
            if (videos.isEmpty()) {
                Toast.makeText(this@MainActivity, R.string.no_results, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onVideoClicked(video: Video) {
        // Open video detail or start playback
        Toast.makeText(this, "Playing: ${video.title}", Toast.LENGTH_SHORT).show()
        // In full implementation, start video player
    }
}
