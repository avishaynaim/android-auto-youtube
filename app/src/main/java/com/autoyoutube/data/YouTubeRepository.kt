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
    // User will provide their own API key
    private var apiKey: String? = null
    
    // OAuth2 for user authentication (for accessing user's data)
    private var googleApiClient: GoogleApiClient? = null

    companion object {
        // Demo videos for when API is not configured
        private val DEMO_VIDEOS = listOf(
            Video("dQw4w9WgXcQ", "Rick Astley - Never Gonna Give You Up", "RickAstley", "🎵 Music"),
            Video("9bZkp7q19f0", "PSY - GANGNAM STYLE", "officialpsy", "🎵 Music"),
            Video("jNQXAC9IVRw", "Me at the zoo", "jawed", "📺 Entertainment"),
            Video("3tmd-ClpJxA", "Top 10 Tech Trends 2024", "TechReviewer", "📱 Tech"),
            Video("L_jWHffIx5E", "Smash Mouth - All Star", "Smash Mouth", "🎵 Music")
        )
        
        private val DEMO_PLAYLISTS = listOf(
            Playlist("PL1", "Favorites", 45),
            Playlist("PL2", "Watch Later", 23),
            Playlist("PL3", "Music Mix", 100)
        )
    }

    init {
        // Load API key from resources or build config
        loadApiKey()
    }

    private fun loadApiKey() {
        try {
            // Try to get from BuildConfig or resources
            // In production, use local.properties or environment variable
            apiKey = context.getString(com.autoyoutube.R.string.youtube_api_key)
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
            // Return demo content when API not configured
            return@withContext DEMO_VIDEOS.map { video ->
                createMediaItem(video.id, video.title, video.channel, video.category)
            }
        }

        try {
            // Fetch from YouTube API
            // In real implementation, call YouTube Data API
            initializeYouTubeClient()
            
            // For now, return demo data
            // Real implementation would call:
            // val search = youTube!!.search().list("snippet")
            // search.key = apiKey
            // search.q = ""
            // search.order = "relevance"
            // ...parse response...
            
            DEMO_VIDEOS.map { video ->
                createMediaItem(video.id, video.title, video.channel, video.category)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Get trending videos
     */
    suspend fun getTrendingContent(): List<MediaItem> = withContext(Dispatchers.IO) {
        if (apiKey.isNullOrEmpty()) {
            return@withContext DEMO_VIDEOS.map { video ->
                createMediaItem("trending_${video.id}", "🔥 ${video.title}", video.channel, video.category)
            }
        }
        
        // Real implementation would fetch trending
        DEMO_VIDEOS.map { video ->
            createMediaItem("trending_${video.id}", "🔥 ${video.title}", video.channel, video.category)
        }
    }

    /**
     * Get user's playlists
     */
    suspend fun getPlaylists(): List<MediaItem> = withContext(Dispatchers.IO) {
        DEMO_PLAYLISTS.map { playlist ->
            createPlaylistItem(playlist.id, playlist.name, "${playlist.videoCount} videos")
        }
    }

    /**
     * Search videos
     */
    suspend fun searchVideos(query: String): List<MediaItem> = withContext(Dispatchers.IO) {
        if (apiKey.isNullOrEmpty()) {
            // Filter demo videos by query
            return@withContext DEMO_VIDEOS
                .filter { it.title.contains(query, ignoreCase = true) }
                .map { video ->
                    createMediaItem(video.id, video.title, video.channel, video.category)
                }
        }
        
        // Real implementation would call YouTube Search API
        DEMO_VIDEOS
            .filter { it.title.contains(query, ignoreCase = true) }
            .map { video ->
                createMediaItem(video.id, video.title, video.channel, video.category)
            }
    }

    /**
     * Get videos in a playlist
     */
    suspend fun getPlaylistVideos(playlistId: String): List<MediaItem> = withContext(Dispatchers.IO) {
        // Return demo videos for playlist
        DEMO_VIDEOS.map { video ->
            createMediaItem(video.id, video.title, video.channel, video.category)
        }
    }

    /**
     * Get video stream URL for playback
     */
    suspend fun getStreamUrl(videoId: String): String? = withContext(Dispatchers.IO) {
        // In production, use YouTube Android Player or stream extraction
        // For now, return null - playback not implemented
        null
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
        return googleApiClient?.isConnected == true || 
               GoogleSignIn.getLastSignedInAccount(context) != null
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
