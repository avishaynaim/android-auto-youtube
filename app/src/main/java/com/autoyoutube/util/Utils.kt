package com.autoyoutube.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log

/**
 * App-wide logging utility
 */
object AppLogger {
    private const val TAG = "AutoYouTube"
    
    fun d(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message)
        }
    }
    
    fun i(message: String) {
        Log.i(TAG, message)
    }
    
    fun w(message: String) {
        Log.w(TAG, message)
    }
    
    fun e(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(TAG, message, throwable)
        } else {
            Log.e(TAG, message)
        }
    }
}

/**
 * Network utilities
 */
object NetworkUtils {
    
    /**
     * Check if device has internet connection
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    /**
     * Check if connected via WiFi
     */
    fun isWifiConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }
}

/**
 * App constants
 */
object AppConstants {
    // API
    const val YOUTUBE_API_BASE_URL = "https://www.googleapis.com/youtube/v3"
    const val YOUTUBE_API_QUOTA_LIMIT = 10000 // Free tier units per day
    
    // Cache
    const val CACHE_SIZE_MB = 50L
    const val CACHE_EXPIRY_HOURS = 24
    
    // Search
    const val SEARCH_DEBOUNCE_MS = 500L
    const val MAX_SEARCH_RESULTS = 25
    
    // Playback
    const val BUFFER_SIZE_MS = 30000
    const val SEEK_INCREMENT_MS = 10000
    
    // UI
    const val SPLASH_DELAY_MS = 1500L
    const val TOAST_DURATION_MS = 3000L
}

/**
 * Preference keys
 */
object PrefsKeys {
    const val API_KEY = "youtube_api_key"
    const val DEMO_MODE = "demo_mode"
    const val DARK_MODE = "dark_mode"
    const val AUTO_PLAY = "auto_play"
    const val LAST_SEARCH = "last_search"
    const val USER_ID = "user_id"
    const val IS_FIRST_LAUNCH = "is_first_launch"
}
