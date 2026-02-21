package com.autoyoutube.data

import android.content.Context
import android.net.Uri
import androidx.media.MediaDescriptionCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaSessionCompat
import com.autoyoutube.model.Video
import com.autoyoutube.model.Playlist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for fetching YouTube data
 * Uses demo data - real API integration can be added later
 */
class YouTubeRepository(private val context: Context) {

    companion object {
        // Extended demo videos for better testing
        private val DEMO_VIDEOS = listOf(
            Video("dQw4w9WgXcQ", "Rick Astley - Never Gonna Give You Up", "RickAstleyVEVO", "🎵 Music", "", "", 213000L, 1500000000L),
            Video("9bZkp7q19f0", "PSY - GANGNAM STYLE", "officialpsy", "🎵 Music", "", "", 252000L, 4800000000L),
            Video("jNQXAC9IVRw", "Me at the zoo", "jawed", "📺 Entertainment", "", "", 19000L, 280000000L),
            Video("L_jWHffIx5E", "Smash Mouth - All Star", "Smash Mouth", "🎵 Music", "", "", 230000L, 1200000000L),
            Video("3tmd-ClpJxA", "Top 10 Tech Trends 2024", "TechGuyWeb", "📱 Tech", "", "", 890000L, 5000000L),
            Video("ktvTqknDw6U", "The Kid LAROI - STAY", "TheKidLAROI", "🎵 Music", "", "", 137000L, 2800000000L),
            Video("hT_nvWreIhg", "Katy Perry - Roar", "katyperryvevo", "🎵 Music", "", "", 270000L, 3900000000L),
            Video("CevxZvSJLk8", "Miley Cyrus - Wrecking Ball", "MileyCyrus", "🎵 Music", "", "", 351000L, 2700000000L),
            Video("RgKAFK5djSk", "Wiz Khalifa - See You Again", "WizKhalifa", "🎵 Music", "", "", 237000L, 5700000000L),
            Video("lXMskKTw3Bc", "K-pop mix 2024", "KpopWorld", "🎶 K-Pop", "", "", 3600000L, 8000000L)
        )
        
        private val DEMO_PLAYLISTS = listOf(
            Playlist("PLF_yEUHjEgZGCs67x_Os3z3IKaCrqJQe5", "Favorites", 45, "My favorite videos"),
            Playlist("PLWzQVCzVEI1laZ-bZZ4ZVT7FKZ4e4Vnt", "Driving Music", 30, "Music for the road"),
            Playlist("PLd5I58C0z0AXTgT7C4gE4Z7xX4Z7xZ4Z", "Watch Later", 23, "Saved videos"),
            Playlist("PL3x7Z7x7x7x7x7x7x7x7x7x7", "Tech Reviews", 15, "Tech reviews"),
            Playlist("PL4x4x4x4x4x4x4x4x4x4x4x4", "Podcasts", 50, "Podcasts to listen to")
        )
    }

    /**
     * Get home content synchronously
     */
    fun getHomeContentSync(): List<MediaBrowserServiceCompat.MediaItem> {
        return DEMO_VIDEOS.take(5).map { video ->
            createMediaItem(video.id, video.title, video.channel, video.category)
        }
    }

    /**
     * Get trending content synchronously
     */
    fun getTrendingContentSync(): List<MediaBrowserServiceCompat.MediaItem> {
        return DEMO_VIDEOS.shuffled().take(5).map { video ->
            createMediaItem("trending_${video.id}", "🔥 ${video.title}", video.channel, video.category)
        }
    }

    /**
     * Get playlists synchronously
     */
    fun getPlaylistsSync(): List<MediaBrowserServiceCompat.MediaItem> {
        return DEMO_PLAYLISTS.map { playlist ->
            createPlaylistItem(playlist.id, playlist.name, "${playlist.videoCount} videos")
        }
    }

    /**
     * Get home content
     */
    suspend fun getHomeContent(): List<MediaBrowserServiceCompat.MediaItem> = withContext(Dispatchers.IO) {
        getHomeContentSync()
    }

    /**
     * Get trending videos
     */
    suspend fun getTrendingContent(): List<MediaBrowserServiceCompat.MediaItem> = withContext(Dispatchers.IO) {
        getTrendingContentSync()
    }

    /**
     * Get user's playlists
     */
    suspend fun getPlaylists(): List<MediaBrowserServiceCompat.MediaItem> = withContext(Dispatchers.IO) {
        getPlaylistsSync()
    }

    /**
     * Search videos
     */
    suspend fun searchVideos(query: String): List<MediaBrowserServiceCompat.MediaItem> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        
        DEMO_VIDEOS
            .filter { it.title.contains(query, ignoreCase = true) || it.channel.contains(query, ignoreCase = true) }
            .map { video -> createMediaItem(video.id, video.title, video.channel, video.category) }
    }

    /**
     * Get videos in a playlist
     */
    suspend fun getPlaylistVideos(playlistId: String): List<MediaBrowserServiceCompat.MediaItem> = withContext(Dispatchers.IO) {
        DEMO_VIDEOS.map { video -> createMediaItem(video.id, video.title, video.channel, video.category) }
    }

    /**
     * Get video stream URL for playback
     */
    suspend fun getStreamUrl(videoId: String): String? = withContext(Dispatchers.IO) {
        val sampleAudios = listOf(
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"
        )
        sampleAudios[videoId.hashCode().mod(3).let { if (it < 0) -it else it }]
    }

    private fun createMediaItem(id: String, title: String, subtitle: String, category: String): MediaBrowserServiceCompat.MediaItem {
        val description = MediaDescriptionCompat.Builder()
            .setMediaId(id)
            .setTitle(title)
            .setSubtitle("$subtitle • $category")
            .setIconUri(Uri.parse("https://i.ytimg.com/vi/$id/maxresdefault.jpg"))
            .setMediaUri(Uri.parse("https://www.youtube.com/watch?v=$id"))
            .build()
        
        return MediaBrowserServiceCompat.MediaItem(description, MediaBrowserServiceCompat.MediaItem.FLAG_PLAYABLE)
    }

    private fun createPlaylistItem(id: String, title: String, subtitle: String): MediaBrowserServiceCompat.MediaItem {
        val description = MediaDescriptionCompat.Builder()
            .setMediaId(id)
            .setTitle(title)
            .setSubtitle(subtitle)
            .setUri(Uri.parse("https://youtube.com/playlist?list=$id"))
            .build()
        
        return MediaBrowserServiceCompat.MediaItem(description, MediaBrowserServiceCompat.MediaItem.FLAG_BROWSABLE)
    }

    fun isAuthenticated(): Boolean = false
}
