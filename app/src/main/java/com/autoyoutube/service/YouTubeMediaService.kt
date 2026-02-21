package com.autoyoutube.service

import android.os.Bundle
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaSessionCompat
import com.autoyoutube.data.YouTubeRepository

/**
 * Media Browser Service for Android Auto
 * This is the KEY SERVICE that makes the app visible in Android Auto!
 */
class YouTubeMediaService : MediaBrowserServiceCompat() {

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var youTubeRepository: YouTubeRepository

    companion object {
        const val MEDIA_ROOT_ID = "auto_youtube_root"
        const val MEDIA_ID_ROOT = "root"
        const val MEDIA_ID_HOME = "home"
        const val MEDIA_ID_TRENDING = "trending"
        const val MEDIA_ID_SEARCH = "search"
        const val MEDIA_ID_PLAYLISTS = "playlists"
        const val MEDIA_ID_MUSIC = "music"
        const val MEDIA_ID_GAMING = "gaming"
        const val MEDIA_ID_NEWS = "news"
    }

    override fun onCreate() {
        super.onCreate()
        
        // Initialize the media session
        mediaSession = MediaSessionCompat(this, "AutoYouTube").apply {
            setCallback(MediaSessionCallback())
            isActive = true
        }
        
        // Initialize YouTube data repository
        youTubeRepository = YouTubeRepository(applicationContext)
        
        // Set the session token for Android Auto to communicate
        sessionToken = mediaSession.sessionToken
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        // Allow all packages to browse (Android Auto will use this)
        // In production, you might want to restrict this
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowser.MediaItem>>
    ) {
        val items = mutableListOf<MediaBrowser.MediaItem>()

        try {
            when (parentId) {
                MEDIA_ROOT_ID -> {
                    // Root menu - these appear as tabs in Android Auto
                    items.add(createBrowsableItem(MEDIA_ID_HOME, "🏠 Home", "Your personalized home"))
                    items.add(createBrowsableItem(MEDIA_ID_TRENDING, "🔥 Trending", "Popular videos"))
                    items.add(createBrowsableItem(MEDIA_ID_MUSIC, "🎵 Music", "Music videos"))
                    items.add(createBrowsableItem(MEDIA_ID_SEARCH, "🔍 Search", "Search YouTube"))
                    items.add(createBrowsableItem(MEDIA_ID_PLAYLISTS, "📋 Playlists", "Your playlists"))
                }
                
                MEDIA_ID_HOME -> {
                    // Load home content
                    val videos = youTubeRepository.getHomeContentSync()
                    items.addAll(videos)
                    if (videos.isEmpty()) {
                        // Add placeholder content
                        items.add(createPlayableItem("demo1", "Welcome to AutoYouTube", "Tap to play demo"))
                        items.add(createPlayableItem("demo2", "Configure API key in Settings", "For real YouTube content"))
                    }
                }
                
                MEDIA_ID_TRENDING -> {
                    val videos = youTubeRepository.getTrendingContentSync()
                    items.addAll(videos)
                    if (videos.isEmpty()) {
                        items.add(createPlayableItem("trending_demo1", "🔥 Trending Video 1", "Most popular"))
                        items.add(createPlayableItem("trending_demo2", "🔥 Trending Video 2", "Viral content"))
                    }
                }
                
                MEDIA_ID_MUSIC -> {
                    // Music category
                    items.add(createPlayableItem("music1", "🎵 Music Video 1", "Official Music Video"))
                    items.add(createPlayableItem("music2", "🎵 Music Video 2", "Official Music Video"))
                    items.add(createPlayableItem("music3", "🎵 Music Video 3", "Official Music Video"))
                }
                
                MEDIA_ID_SEARCH -> {
                    // Search instructions
                    items.add(createBrowsableItem("search_tips", "💡 Search Tips", "How to search"))
                }
                
                MEDIA_ID_PLAYLISTS -> {
                    val playlists = youTubeRepository.getPlaylistsSync()
                    items.addAll(playlists)
                    if (playlists.isEmpty()) {
                        items.add(createBrowsableItem("playlist_demo1", "❤️ Favorites", "Your favorite videos"))
                        items.add(createBrowsableItem("playlist_demo2", "⏰ Watch Later", "Save for later"))
                    }
                }
                
                "search_tips" -> {
                    items.add(createPlayableItem("search_tip_1", "Say 'Hey Google, search YouTube for [query]'", "Voice search"))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Return error items
            items.add(createPlayableItem("error", "Error loading content", e.message ?: "Unknown error"))
        }

        result.sendResult(items)
    }

    override fun onLoadItem(itemId: String, result: Result<MediaBrowser.MediaItem>) {
        super.onLoadItem(itemId, result)
    }

    private fun createBrowsableItem(mediaId: String, title: String, subtitle: String): MediaBrowser.MediaItem {
        val description = androidx.media.MediaDescriptionCompat.Builder()
            .setMediaId(mediaId)
            .setTitle(title)
            .setSubtitle(subtitle)
            .setIconUri(android.net.Uri.parse("android.resource://${packageName}/drawable/ic_launcher_foreground"))
            .build()
        
        return MediaBrowser.MediaItem(description, MediaBrowser.MediaItem.FLAG_BROWSABLE)
    }

    private fun createPlayableItem(mediaId: String, title: String, subtitle: String): MediaBrowser.MediaItem {
        val description = androidx.media.MediaDescriptionCompat.Builder()
            .setMediaId(mediaId)
            .setTitle(title)
            .setSubtitle(subtitle)
            .setIconUri(android.net.Uri.parse("https://i.ytimg.com/vi/$mediaId/maxresdefault.jpg"))
            .setMediaUri(android.net.Uri.parse("https://www.youtube.com/watch?v=$mediaId"))
            .build()
        
        return MediaBrowser.MediaItem(description, MediaBrowser.MediaItem.FLAG_PLAYABLE)
    }

    override fun onDestroy() {
        mediaSession.release()
        super.onDestroy()
    }

    inner class MediaSessionCallback : MediaSessionCompat.Callback() {
        
        override fun onPlay() {
            // Start playback
        }

        override fun onPause() {
            // Pause playback
        }

        override fun onStop() {
            // Stop playback
        }

        override fun onSkipToNext() {
            // Skip to next track
        }

        override fun onSkipToPrevious() {
            // Skip to previous track
        }

        override fun onSeekTo(pos: Long) {
            // Seek to position
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            // Play specific media
            mediaId?.let {
                // Start playback
            }
        }

        override fun onSearch(query: String?, extras: Bundle?) {
            // Handle search from Android Auto
            // This is called when user uses voice search
            query?.let {
                // Perform search
            }
        }
    }
}
