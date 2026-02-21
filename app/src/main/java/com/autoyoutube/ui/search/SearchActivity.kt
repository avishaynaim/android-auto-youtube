package com.autoyoutube.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.autoyoutube.R
import com.autoyoutube.data.YouTubeRepository
import com.autoyoutube.databinding.ActivitySearchBinding
import com.autoyoutube.ui.VideoAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Search Activity - Dedicated search screen
 */
class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var youTubeRepository: YouTubeRepository
    private lateinit var videoAdapter: VideoAdapter
    private var searchJob: Job? = null

    companion object {
        private const val SEARCH_DELAY = 500L // Debounce delay
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        youTubeRepository = YouTubeRepository(this)
        
        setupUI()
        setupRecyclerView()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // Auto-search when text changes (with debounce)
        binding.searchInput.setOnEditorActionListener { _, _, _ ->
            performSearch()
            true
        }

        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s?.toString() ?: ""
                // Debounced search
                searchJob?.cancel()
                searchJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(SEARCH_DELAY)
                    if (text.isNotEmpty()) {
                        performSearchInternal(text)
                    }
                }
            }
        })

        binding.clearButton.setOnClickListener {
            binding.searchInput.text?.clear()
            videoAdapter.submitList(emptyList())
            binding.emptyView.visibility = View.VISIBLE
            binding.resultsRecyclerView.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        videoAdapter = VideoAdapter { videoItem ->
            // Handle video click - play video
            Toast.makeText(this, "Playing: ${videoItem.title}", Toast.LENGTH_SHORT).show()
        }
        
        binding.resultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = videoAdapter
        }
    }

    private fun performSearch() {
        val query = binding.searchInput.text?.toString() ?: ""
        if (query.isNotEmpty()) {
            performSearchInternal(query)
        }
    }

    private fun performSearchInternal(query: String) {
        binding.loadingProgress.visibility = View.VISIBLE
        binding.emptyView.visibility = View.GONE
        
        CoroutineScope(Dispatchers.Main).launch {
            val results = withContext(Dispatchers.IO) {
                youTubeRepository.searchVideos(query)
            }
            
            binding.loadingProgress.visibility = View.GONE
            
            if (results.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.resultsRecyclerView.visibility = View.GONE
                binding.emptyText.text = getString(R.string.no_results)
            } else {
                binding.emptyView.visibility = View.GONE
                binding.resultsRecyclerView.visibility = View.VISIBLE
                videoAdapter.submitList(results)
            }
        }
    }
}
