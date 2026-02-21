package com.autoyoutube.model

data class Video(
    val id: String,
    val title: String,
    val channel: String,
    val category: String,
    val description: String = "",
    val thumbnailUrl: String = "",
    val duration: Long = 0,
    val viewCount: Long = 0
)

data class Playlist(
    val id: String,
    val name: String,
    val videoCount: Int,
    val description: String = ""
)
