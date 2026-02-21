# AutoYouTube - Android Auto YouTube App

An Android Auto application that integrates YouTube for hands-free video browsing and playback while driving.

## ⚡ Quick Start

```bash
# Verify Android Auto configuration (no SDK needed)
./verify-config.sh

# If all checks pass, build and test:
./setup.sh
```

## Features

- 🚗 **Android Auto Integration** - Fully integrated with Android Auto's media browser
- 🔍 **Search** - Search YouTube videos (with debounced input)
- 📈 **Trending** - Browse trending videos
- 📋 **Playlists** - Access your YouTube playlists
- 🎵 **Media Controls** - Full media session support for car controls
- 🌙 **Dark Mode** - Support for dark theme
- ⚙️ **Settings** - Configure API key, demo mode, preferences
- 🎬 **Video Player** - ExoPlayer-based video playback
- 📱 **Phone UI** - Full phone interface alongside Auto

## App Screens

### Phone App
- **Splash Screen** - App loading with branding
- **Home** - Recommended videos
- **Trending** - Popular videos
- **Search** - Search with real-time results
- **Playlists** - View your playlists
- **Video Player** - Play videos with controls
- **Settings** - Configure app preferences

### Android Auto
- **Home Tab** - Recommended content
- **Trending Tab** - Popular videos
- **Music Tab** - Music videos
- **Search Tab** - Search YouTube
- **Playlists Tab** - Your playlists

## Demo Mode

The app works out of the box with demo data:
- 10 sample videos
- 5 sample playlists
- Search works on demo data
- No API key needed for testing

## Real YouTube Data

To enable real YouTube content:

1. Get API key from https://console.cloud.google.com/
2. Enable "YouTube Data API v3"
3. Add key to `app/src/main/res/values/strings.xml`:
```xml
<string name="youtube_api_key">YOUR_API_KEY_HERE</string>
```

## Why This Works in Android Auto

### 1. AndroidManifest.xml Configuration
```xml
<!-- Android Auto Media Browser Service -->
<service
    android:name=".service.YouTubeMediaService"
    android:exported="true">
    <intent-filter>
        <action android:name="android.media.browse.MediaBrowserService" />
    </intent-filter>
</service>

<!-- Android Auto App Declaration -->
<meta-data
    android:name="com.google.android.gms.car.application"
    android:resource="@xml/automotive_app_desc" />
```

### 2. automotive_app_desc.xml
```xml
<automotiveApp>
    <uses name="media"/>
</automotiveApp>
```

### 3. MediaBrowserService Implementation
The app implements `MediaBrowserServiceCompat` which is required for Android Auto to discover and communicate with the app.

## Testing Without Building

### Static Analysis (No SDK Required)

```bash
./verify-config.sh
```

This verifies:
- ✅ MediaBrowserService is declared
- ✅ Android Auto meta-data is present
- ✅ Required permissions are granted
- ✅ Dependencies are configured

### Build & Test

1. **Install Android SDK**: https://developer.android.com/studio
2. **Build**:
   ```bash
   ./gradlew assembleDebug
   ```
3. **Install**:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```
4. **Test in Android Auto**

## Project Structure

```
app/src/main/java/com/autoyoutube/
├── ui/
│   ├── MainActivity.kt         # Main phone UI
│   ├── VideoAdapter.kt         # Video list adapter
│   ├── activity/
│   │   └── VideoPlayerActivity.kt
│   ├── search/
│   │   └── SearchActivity.kt
│   ├── settings/
│   │   └── SettingsActivity.kt
│   └── splash/
│       └── SplashActivity.kt
├── service/
│   ├── YouTubeMediaService.kt     # Android Auto browser
│   └── YouTubePlaybackService.kt  # Media playback
├── data/
│   ├── YouTubeRepository.kt       # YouTube API client
│   └── YouTubeStreamExtractor.kt  # Stream URL extraction
├── model/
│   └── Models.kt                  # Data classes
├── util/
│   └── Utils.kt                   # Utilities
└── AutoYouTube.kt                 # Application class
```

## CI/CD

GitHub Actions workflow (`.github/workflows/android.yml`):
- Builds debug APK on every push
- Verifies Android Auto configuration
- Uploads APK as artifact

## Known Limitations

1. **Video Playback**: Full video playback in Android Auto is restricted. The app provides audio streaming only. For full video:
   - Use YouTube Premium for background audio
   - Use official YouTube app

2. **API Quotas**: YouTube Data API has daily limits (10,000 units/day free)

3. **OAuth**: Requires app verification for production use

## Tech Stack

- **Language**: Kotlin
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)
- **Build System**: Gradle 8.2
- **Key Libraries**:
  - AndroidX Media
  - Google Play Services (Car)
  - ExoPlayer
  - YouTube Data API
  - Google Auth Library

## License

MIT License
