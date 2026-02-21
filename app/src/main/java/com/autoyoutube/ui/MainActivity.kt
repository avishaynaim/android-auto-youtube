package com.autoyoutube.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.autoyoutube.R
import com.autoyoutube.data.YouTubeRepository
import com.autoyoutube.databinding.ActivityMainBinding
import com.autoyoutube.model.Video
import com.autoyoutube.ui.activity.VideoPlayerActivity
import com.autoyoutube.ui.settings.SettingsActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Main Activity - Phone UI for YouTube
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
        
        loadContent(Tab.HOME)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_refresh -> {
                loadContent(currentTab)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
        videoAdapter = VideoAdapter { videoItem ->
            onVideoClicked(videoItem)
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
                    binding.searchInput.visibility = View.GONE
                    loadContent(Tab.HOME)
                    true
                }
                R.id.nav_trending -> {
                    currentTab = Tab.TRENDING
                    binding.titleText.text = getString(R.string.trending)
                    binding.searchInput.visibility = View.GONE
                    loadContent(Tab.TRENDING)
                    true
                }
                R.id.nav_search -> {
                    currentTab = Tab.SEARCH
                    binding.titleText.text = getString(R.string.search)
                    binding.searchInput.visibility = View.VISIBLE
                    binding.searchInput.text?.clear()
                    true
                }
                R.id.nav_playlists -> {
                    currentTab = Tab.PLAYLISTS
                    binding.titleText.text = getString(R.string.library)
                    binding.searchInput.visibility = View.GONE
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
                    Tab.SEARCH -> {
                        val query = binding.searchInput.text.toString()
                        if (query.isEmpty()) {
                            youTubeRepository.getHomeContent()
                        } else {
                            youTubeRepository.searchVideos(query)
                        }
                    }
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
        if (query.isEmpty()) {
            Toast.makeText(this, R.string.search_hint, Toast.LENGTH_SHORT).show()
            return
        }
        
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

    private fun onVideoClicked(videoItem: VideoItem) {
        // Open video player
        val intent = Intent(this, VideoPlayerActivity::class.java).apply {
            putExtra(VideoPlayerActivity.EXTRA_VIDEO_ID, videoItem.id)
            putExtra(VideoPlayerActivity.EXTRA_VIDEO_TITLE, videoItem.title)
            putExtra(VideoPlayerActivity.EXTRA_VIDEO_CHANNEL, videoItem.channel)
        }
        startActivity(intent)
    }
}
