package com.autoyoutube.data

import android.content.Context
import androidx.media.MediaDescriptionCompat
import androidx.media.MediaBrowser.MediaItem
import com.autoyoutube.model.Video
import com.autoyoutube.model.Playlist
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.YouTubeScopes
import com.google.api.services.youtube.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Repository for fetching YouTube data
 * Handles authentication and API calls
 */
class YouTubeRepository(private val context: Context) {

    private var httpTransport: NetHttpTransport? = null
    private var youTube: YouTube? = null
    
    // API Key for simple access (for testing)
    private var apiKey: String? = null
    
    // OAuth2 for user authentication
    private var googleApiClient: GoogleApiClient? = null

    companion object {
        // Extended demo videos for better testing
        private val DEMO_VIDEOS = listOf(
            Video("dQw4w9WgXcQ", "Rick Astley - Never Gonna Give You Up", "RickAstleyVEVO", "🎵 Music", 213000, 1500000000L),
            Video("9bZkp7q19f0", "PSY - GANGNAM STYLE (강남스타일)", "officialpsy", "🎵 Music", 252000, 4800000000L),
            Video("jNQXAC9IVRw", "Me at the zoo", "jawed", "📺 Entertainment", 19000, 280000000L),
            Video("L_jWHffIx5E", "Smash Mouth - All Star (Shrek)", "Smash Mouth", "🎵 Music", 230000, 1200000000L),
            Video("3tmd-ClpJxA", "Top 10 Tech Trends 2024", "TechGuyWeb", "📱 Tech", 890000, 5000000L),
            Video("ktvTqknDw6U", "The Kid LAROI, Justin Bieber - STAY", "TheKidLAROI", "🎵 Music", 137000, 2800000000L),
            Video("hT_nvWreIhg", "Katy Perry - Roar", "katyperryvevo", "🎵 Music", 270000, 3900000000L),
            Video("CevxZvSJLk8", "Miley Cyrus - Wrecking Ball", "MileyCyrus", "🎵 Music", 351000, 2700000000L),
            Video("RgKAFK5djSk", "Wiz Khalifa - See You Again", "WizKhalifa", "🎵 Music", 237000, 5700000000L),
            Video("lXMskKTw3Bc", "K-pop mix 2024", "KpopWorld", "🎶 K-Pop", 3600000, 8000000L)
        )
        
        private val DEMO_PLAYLISTS = listOf(
            Playlist("PLF_yEUHjEgZGCs67x_Os3z3IKaCrqJQe5", "Favorites", 45, "My favorite videos"),
            Playlist("PLWzQVCzVEI1laZ-bZZ4ZVT7FKZ4e4Vnt", "Driving Music", 30, "Music for the road"),
            Playlist("PLd5I58C0z0AXTgT7C4gE4Z7xX4Z7xZ4Z", "Watch Later", 23, "Saved videos"),
            Playlist("PL3x7Z7x7x7x7x7x7x7x7x7x7x7x7", "Tech Reviews", 15, "Tech reviews and tutorials"),
            Playlist("PL4x4x4x4x4x4x4x4x4x4x4x4", "Podcasts", 50, "Podcasts to listen to")
        )

        // Trending categories
        private val CATEGORIES = listOf(
            "Music" to "🎵",
            "Gaming" to "🎮",
            "News" to "📰",
            "Sports" to "⚽",
            "Tech" to "📱",
            "Cooking" to "🍳",
            "Science" to "🔬",
            "Entertainment" to "🎬"
        )
    }

    init {
        loadApiKey()
    }

    private fun loadApiKey() {
        try {
            apiKey = context.getString(com.autoyoutube.R.string.youtube_api_key)
            if (apiKey == "YOUR_API_KEY_HERE") {
                apiKey = null // Use demo mode
            }
        } catch (e: Exception) {
            // Not configured - will use demo data
        }
    }

    /**
     * Initialize YouTube API client
     */
    private fun initializeYouTubeClient() {
        if (apiKey.isNullOrEmpty()) return
        
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport()
            youTube = YouTube.Builder(httpTransport!!, GsonFactory.getDefaultInstance(), null)
                .setApplicationName("AutoYouTube")
                .build()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Get home content - recommended videos
     */
    suspend fun getHomeContent(): List<MediaItem> = withContext(Dispatchers.IO) {
        if (apiKey.isNullOrEmpty()) {
            return@withContext DEMO_VIDEOS.take(5).map { video ->
                createMediaItem(video.id, video.title, video.channel, video.category)
            }
        }

        try {
            initializeYouTubeClient()
            if (youTube == null) return@withContext emptyList()

            val search = youTube!!.search().list("snippet")
            search.key = apiKey
            search.type = "video"
            search.order = "relevance"
            search.maxResults = 10
            search.fields = "items(id,snippet(title,channelTitle,thumbnails))"
            
            val response = search.execute()
            
            response.items.mapNotNull { item ->
                val videoId = item.id.videoId
                val title = item.snippet.title
                val channel = item.snippet.channelTitle
                createMediaItem(videoId, title, channel, "Recommended")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            // Fallback to demo
            DEMO_VIDEOS.take(5).map { video ->
                createMediaItem(video.id, video.title, video.channel, video.category)
            }
        }
    }

    /**
     * Get trending videos
     */
    suspend fun getTrendingContent(): List<MediaItem> = withContext(Dispatchers.IO) {
        if (apiKey.isNullOrEmpty()) {
            return@withContext DEMO_VIDEOS.shuffled().map { video ->
                createMediaItem("trending_${video.id}", "🔥 ${video.title}", video.channel, video.category)
            }
        }
        
        try {
            initializeYouTubeClient()
            if (youTube == null) return@withContext emptyList()

            val search = youTube!!.search().list("snippet")
            search.key = apiKey
            search.type = "video"
            search.order = "viewCount"
            search.maxResults = 15
            search.fields = "items(id,snippet(title,channelTitle))"
            
            val response = search.execute()
            
            response.items.map { item ->
                createMediaItem(
                    item.id.videoId,
                    "🔥 ${item.snippet.title}",
                    item.snippet.channelTitle,
                    "Trending"
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
            DEMO_VIDEOS.shuffled().map { video ->
                createMediaItem("trending_${video.id}", "🔥 ${video.title}", video.channel, video.category)
            }
        }
    }

    /**
     * Get user's playlists
     */
    suspend fun getPlaylists(): List<MediaItem> = withContext(Dispatchers.IO) {
        if (apiKey.isNullOrEmpty() || !isAuthenticated()) {
            return@withContext DEMO_PLAYLISTS.map { playlist ->
                createPlaylistItem(playlist.id, playlist.name, "${playlist.videoCount} videos")
            }
        }
        
        try {
            initializeYouTubeClient()
            if (youTube == null) return@withContext emptyList()

            // Get user's playlists via OAuth
            val playlists = youTube!!.playlists().list("snippet,contentDetails")
            playlists.mine = true
            playlists.maxResults = 20
            
            val response = playlists.execute()
            
            response.items.map { item ->
                createPlaylistItem(
                    item.id,
                    item.snippet.title,
                    "${item.contentDetails.itemCount} videos"
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
            DEMO_PLAYLISTS.map { playlist ->
                createPlaylistItem(playlist.id, playlist.name, "${playlist.videoCount} videos")
            }
        }
    }

    /**
     * Search videos
     */
    suspend fun searchVideos(query: String): List<MediaItem> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        
        if (apiKey.isNullOrEmpty()) {
            return@withContext DEMO_VIDEOS
                .filter { it.title.contains(query, ignoreCase = true) || 
                          it.channel.contains(query, ignoreCase = true) }
                .map { video ->
                    createMediaItem(video.id, video.title, video.channel, video.category)
                }
        }
        
        try {
            initializeYouTubeClient()
            if (youTube == null) return@withContext emptyList()

            val search = youTube!!.search().list("snippet")
            search.key = apiKey
            search.q = query
            search.type = "video"
            search.order = "relevance"
            search.maxResults = 20
            
            val response = search.execute()
            
            response.items.map { item ->
                createMediaItem(
                    item.id.videoId,
                    item.snippet.title,
                    item.snippet.channelTitle,
                    "Search: $query"
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
            DEMO_VIDEOS
                .filter { it.title.contains(query, ignoreCase = true) }
                .map { video ->
                    createMediaItem(video.id, video.title, video.channel, video.category)
                }
        }
    }

    /**
     * Get videos in a playlist
     */
    suspend fun getPlaylistVideos(playlistId: String): List<MediaItem> = withContext(Dispatchers.IO) {
        if (apiKey.isNullOrEmpty()) {
            return@withContext DEMO_VIDEOS.map { video ->
                createMediaItem(video.id, video.title, video.channel, video.category)
            }
        }
        
        try {
            initializeYouTubeClient()
            if (youTube == null) return@withContext emptyList()

            val playlistItems = youTube!!.playlistItems().list("snippet,contentDetails")
            playlistItems.playlistId = playlistId
            playlistItems.maxResults = 50
            
            val response = playlistItems.execute()
            
            response.items.map { item ->
                createMediaItem(
                    item.contentDetails.videoId,
                    item.snippet.title,
                    item.snippet.channelTitle,
                    "Playlist"
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Get video stream URL for playback
     * Note: This returns a sample audio URL for demo purposes
     * Real implementation would use YouTube Android Player or stream extraction
     */
    suspend fun getStreamUrl(videoId: String): String? = withContext(Dispatchers.IO) {
        // In production, use one of these methods:
        // 1. YouTube Android Player API (requires YouTube app)
        // 2. NewPipe (open source, no API key)
        // 3. Custom stream extraction (against YouTube ToS)
        // 4. YouTube Premium for background play
        
        // For demo, return sample audio
        val sampleAudios = listOf(
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3"
        )
        
        sampleAudios[videoId.hashCode().mod(5).let { if (it < 0) -it else it }]
    }

    /**
     * Get video details
     */
    suspend fun getVideoDetails(videoId: String): Video? = withContext(Dispatchers.IO) {
        if (apiKey.isNullOrEmpty()) {
            return@withContext DEMO_VIDEOS.find { it.id == videoId }
        }
        
        try {
            initializeYouTubeClient()
            if (youTube == null) return@withContext null

            val videos = youTube!!.videos().list("snippet,statistics,contentDetails")
            videos.id = videoId
            videos.fields = "items(snippet(title,channelTitle,description),statistics(viewCount),contentDetails(duration)"
            
            val response = videos.execute()
            val item = response.items.firstOrNull() ?: return@withContext null
            
            Video(
                id = videoId,
                title = item.snippet.title,
                channel = item.snippet.channelTitle,
                category = "YouTube",
                description = item.snippet.description ?: "",
                duration = parseDuration(item.contentDetails.duration),
                viewCount = item.statistics.viewCount?.toLongOrNull() ?: 0
            )
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Parse ISO 8601 duration to seconds
     */
    private fun parseDuration(duration: String): Long {
        // PT1H2M10S -> 3730 seconds
        val regex = """PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?""".toRegex()
        val match = regex.find(duration) ?: return 0
        val hours = match.groupValues[1].toLongOrNull() ?: 0
        val minutes = match.groupValues[2].toLongOrNull() ?: 0
        val seconds = match.groupValues[3].toLongOrNull() ?: 0
        return hours * 3600 + minutes * 60 + seconds
    }

    private fun createMediaItem(id: String, title: String, subtitle: String, category: String): MediaItem {
        val description = MediaDescriptionCompat.Builder()
            .setMediaId(id)
            .setTitle(title)
            .setSubtitle("$subtitle • $category")
            .setIconUri(android.net.Uri.parse("https://i.ytimg.com/vi/$id/maxresdefault.jpg"))
            .setMediaUri(android.net.Uri.parse("https://www.youtube.com/watch?v=$id"))
            .build()
        
        return MediaItem(description, MediaItem.FLAG_PLAYABLE)
    }

    private fun createPlaylistItem(id: String, title: String, subtitle: String): MediaItem {
        val description = MediaDescriptionCompat.Builder()
            .setMediaId(id)
            .setTitle(title)
            .setSubtitle(subtitle)
            .setIconUri(android.net.Uri.parse("https://i.ytimg.com/vi/placeholder/playlist.jpg"))
            .build()
        
        return MediaItem(description, MediaItem.FLAG_BROWSABLE)
    }

    /**
     * Check if user is authenticated with Google
     */
    fun isAuthenticated(): Boolean {
        return try {
            GoogleSignIn.getLastSignedInAccount(context) != null
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get Google Sign In client for OAuth
     */
    fun getSignInClient(): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(YouTubeScopes.YOUTUBE_READONLY)
            .requestEmail()
            .build()
    }
}
