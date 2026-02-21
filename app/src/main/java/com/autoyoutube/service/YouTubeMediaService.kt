package com.autoyoutube.service

import android.content.Intent
import android.os.Binder
import android.os.IBinder

/**
 * Stub service for Android Auto
 * Full implementation requires play-services-car library
 */
class YouTubeMediaService : android.app.Service() {
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): YouTubeMediaService = this@YouTubeMediaService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }
}
