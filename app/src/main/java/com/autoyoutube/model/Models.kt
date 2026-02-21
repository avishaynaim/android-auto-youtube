package com.autoyoutube.model

/**
 * YouTube Video model
 */
data class Video(
    val id: String,
    val title: String,
    val channel: String,
    val category: String,
    val description: String = "",
    val thumbnailUrl: String = "",
    val duration: Long = 0,
    val viewCount: Long = 0,
    val publishedAt: String = ""
)

/**
 * YouTube Playlist model
 */
data class Playlist(
    val id: String,
    val name: String,
    val videoCount: Int,
    val description: String = "",
    val thumbnailUrl: String = "",
    val owner: String = ""
)

/**
 * Search result model
 */
data class SearchResult(
    val videos: List<Video>,
    val playlists: List<Playlist>,
    val channels: List<Channel>
)

/**
 * YouTube Channel model
 */
data class Channel(
    val id: String,
    val name: String,
    val description: String = "",
    val thumbnailUrl: String = "",
    val subscriberCount: Long = 0
)

/**
 * Playback state
 */
data class PlaybackState(
    val isPlaying: Boolean = false,
    val currentVideo: Video? = null,
    val position: Long = 0,
    val duration: Long = 0,
    val playbackSpeed: Float = 1.0f,
    val queue: List<Video> = emptyList()
)

/**
 * API Response wrapper
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int = 0) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

/**
 * Video category for browsing
 */
data class Category(
    val id: String,
    val name: String,
    val icon: String
)

/**
 * Home screen section
 */
data class HomeSection(
    val title: String,
    val type: SectionType,
    val videos: List<Video>
)

enum class SectionType {
    CONTINUE_WATCHING,
    RECOMMENDED,
    TRENDING,
    NEW_UPLOADS,
    PLAYLISTS
}
