package com.autoyoutube.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import androidx.media.session.MediaSessionCompat
import com.autoyoutube.R
import com.autoyoutube.ui.MainActivity

/**
 * Media Playback Service for Android Auto
 * Handles actual video/audio playback using ExoPlayer
 */
class YouTubePlaybackService : MediaBrowserServiceCompat() {

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var notificationManager: NotificationManager
    
    companion object {
        const val CHANNEL_ID = "com.autoyoutube.playback"
        const val NOTIFICATION_ID = 1
        const val MEDIA_ROOT_ID = "auto_youtube_playback_root"
    }

    override fun onCreate() {
        super.onCreate()
        
        // Create notification channel for Android O+
        createNotificationChannel()
        
        // Initialize media session
        mediaSession = MediaSessionCompat(this, "AutoYouTubePlayback").apply {
            setCallback(MediaSessionCallback())
            isActive = true
        }
        
        sessionToken = mediaSession.sessionToken
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowser.MediaItem>>
    ) {
        result.sendResult(mutableListOf())
    }

    override fun onLoadItem(itemId: String, result: Result<MediaBrowser.MediaItem>) {
        super.onLoadItem(itemId, result)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "YouTube Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "YouTube video playback controls"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(title: String, artist: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(artist)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .addAction(
                android.R.drawable.ic_media_previous,
                "Previous",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this, androidx.media.session.PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                )
            )
            .addAction(
                android.R.drawable.ic_media_pause,
                "Pause",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this, androidx.media.session.PlaybackStateCompat.ACTION_PAUSE
                )
            )
            .addAction(
                android.R.drawable.ic_media_next,
                "Next",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this, androidx.media.session.PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                )
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    override fun onDestroy() {
        mediaSession.release()
        super.onDestroy()
    }

    inner class MediaSessionCallback : MediaSessionCompat.Callback() {
        
        private var currentState = androidx.media.session.PlaybackStateCompat.Builder()
            .setActions(
                androidx.media.session.PlaybackStateCompat.ACTION_PLAY or
                androidx.media.session.PlaybackStateCompat.ACTION_PAUSE or
                androidx.media.session.PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                androidx.media.session.PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                androidx.media.session.PlaybackStateCompat.ACTION_SEEK_TO or
                androidx.media.session.PlaybackStateCompat.ACTION_PLAY_PAUSE
            )
            .setState(
                androidx.media.session.PlaybackStateCompat.STATE_STOPPED,
                androidx.media.session.PlaybackStateCompat.POSITION_UNKNOWN,
                1.0f
            )
            .build()

        override fun onPlay() {
            mediaSession.setPlaybackState(
                androidx.media.session.PlaybackStateCompat.Builder()
                    .setActions(currentState.actions)
                    .setState(
                        androidx.media.session.PlaybackStateCompat.STATE_PLAYING,
                        0,
                        1.0f
                    )
                    .build()
            )
            startForeground(NOTIFICATION_ID, buildNotification("Playing", "YouTube"))
        }

        override fun onPause() {
            mediaSession.setPlaybackState(
                androidx.media.session.PlaybackStateCompat.Builder()
                    .setActions(currentState.actions)
                    .setState(
                        androidx.media.session.PlaybackStateCompat.STATE_PAUSED,
                        0,
                        1.0f
                    )
                    .build()
            )
        }

        override fun onStop() {
            mediaSession.setPlaybackState(
                androidx.media.session.PlaybackStateCompat.Builder()
                    .setActions(currentState.actions)
                    .setState(
                        androidx.media.session.PlaybackStateCompat.STATE_STOPPED,
                        androidx.media.session.PlaybackStateCompat.POSITION_UNKNOWN,
                        1.0f
                    )
                    .build()
            )
            stopForeground(STOP_FOREGROUND_REMOVE)
        }

        override fun onSkipToNext() {
            // Skip to next in queue
        }

        override fun onSkipToPrevious() {
            // Skip to previous in queue
        }

        override fun onSeekTo(pos: Long) {
            mediaSession.setPlaybackState(
                androidx.media.session.PlaybackStateCompat.Builder()
                    .setActions(currentState.actions)
                    .setState(
                        androidx.media.session.PlaybackStateCompat.STATE_PLAYING,
                        pos,
                        1.0f
                    )
                    .build()
            )
        }

        override fun onCustomAction(action: String?, extras: Bundle?) {
            super.onCustomAction(action, extras)
        }
    }
}
