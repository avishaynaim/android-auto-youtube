package com.autoyoutube.data

import android.content.Context
import com.autoyoutube.model.Video
import com.autoyoutube.model.Playlist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for demo video data
 */
class YouTubeRepository(private val context: Context) {

    companion object {
        private val DEMO_VIDEOS = listOf(
            Video("dQw4w9WgXcQ", "Rick Astley - Never Gonna Give You Up", "RickAstleyVEVO", "Music"),
            Video("9bZkp7q19f0", "PSY - GANGNAM STYLE", "officialpsy", "Music"),
            Video("jNQXAC9IVRw", "Me at the zoo", "jawed", "Entertainment"),
            Video("L_jWHffIx5E", "Smash Mouth - All Star", "Smash Mouth", "Music"),
            Video("3tmd-ClpJxA", "Top 10 Tech Trends 2024", "TechGuyWeb", "Tech")
        )
        
        private val DEMO_PLAYLISTS = listOf(
            Playlist("PL1", "Favorites", 45),
            Playlist("PL2", "Watch Later", 23),
            Playlist("PL3", "Driving Music", 30)
        )
    }

    fun getHomeContent(): List<Video> = DEMO_VIDEOS

    fun getTrendingContent(): List<Video> = DEMO_VIDEOS.shuffled()

    fun getPlaylists(): List<Playlist> = DEMO_PLAYLISTS

    suspend fun searchVideos(query: String): List<Video> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        DEMO_VIDEOS.filter { it.title.contains(query, ignoreCase = true) }
    }

    suspend fun getStreamUrl(videoId: String): String? = withContext(Dispatchers.IO) {
        listOf(
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
        )[videoId.hashCode().mod(2).let { if (it < 0) -it else it }]
    }
}
