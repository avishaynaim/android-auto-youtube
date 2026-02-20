package com.autoyoutube.service

import android.os.Bundle
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaSessionCompat
import com.autoyoutube.data.YouTubeRepository

/**
 * Media Browser Service for Android Auto
 * This is the KEY SERVICE that makes the app visible in Android Auto!
 * 
 * Without this service properly configured, the app won't show up
 * in the Android Auto launcher.
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
        const val MEDIA_BROWSER_ROOT_ID = "auto_youtube_root"
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
        // This is critical! Return the root ID for Android Auto to build the menu
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowser.MediaItem>>
    ) {
        // Load children based on the parent ID
        // Android Auto will call this to get the menu items
        val items = mutableListOf<MediaBrowser.MediaItem>()

        when (parentId) {
            MEDIA_ROOT_ID -> {
                // Root menu - these appear as tabs in Android Auto
                items.add(createBrowsableItem(MEDIA_ID_HOME, "Home", "🏠"))
                items.add(createBrowsableItem(MEDIA_ID_TRENDING, "Trending", "📈"))
                items.add(createBrowsableItem(MEDIA_ID_SEARCH, "Search", "🔍"))
                items.add(createBrowsableItem(MEDIA_ID_PLAYLISTS, "Playlists", "📋"))
            }
            MEDIA_ID_HOME -> {
                // Load home content (recommended videos)
                // In a real app, this would fetch from YouTube API
                items.addAll(youTubeRepository.getHomeContent())
            }
            MEDIA_ID_TRENDING -> {
                // Load trending content
                items.addAll(youTubeRepository.getTrendingContent())
            }
            MEDIA_ID_SEARCH -> {
                // Search would be handled differently in Android Auto
                items.add(createBrowsableItem("search_trending", "Popular Searches", "🔥"))
            }
            MEDIA_ID_PLAYLISTS -> {
                // User's playlists
                items.addAll(youTubeRepository.getPlaylists())
            }
        }

        result.sendResult(items)
    }

    private fun createBrowsableItem(mediaId: String, title: String, icon: String): 
            MediaBrowser.MediaItem {
        val description = androidx.media.MediaDescriptionCompat.Builder()
            .setMediaId(mediaId)
            .setTitle(title)
            .setIconUri(android.net.Uri.parse("android.resource://${packageName}/drawable/ic_launcher"))
            .build()
        
        return MediaBrowser.MediaItem(description, MediaBrowser.MediaItem.FLAG_BROWSABLE)
    }

    override fun onDestroy() {
        mediaSession.release()
        super.onDestroy()
    }

    inner class MediaSessionCallback : MediaSessionCompat.Callback() {
        // Handle media controls from Android Auto
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
    }
}
