package com.autoyoutube.service

import android.content.Intent
import android.os.Binder
import android.os.IBinder

/**
 * Stub playback service
 */
class YouTubePlaybackService : android.app.Service() {
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): YouTubePlaybackService = this@YouTubePlaybackService
    }

    override fun onBind(intent: Intent?): IBinder = binder
}
