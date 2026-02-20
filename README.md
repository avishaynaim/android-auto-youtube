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

- 📱 **Android Auto Integration** - Fully integrated with Android Auto's media browser
- 🔍 **Search** - Search YouTube videos
- 📈 **Trending** - Browse trending videos
- 📋 **Playlists** - Access your YouTube playlists
- 🎵 **Media Controls** - Full media session support for car controls

## Why This Works in Android Auto

The key issue with the previous version was missing Android Auto configuration. This version includes:

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

### Static Analysis (No Android SDK Required)

Verify the configuration is correct:

```bash
# Check AndroidManifest.xml has the required entries
grep -A3 "com.google.android.gms.car.application" app/src/main/AndroidManifest.xml

# Check automotive_app_desc.xml
cat app/src/main/res/xml/automotive_app_desc.xml

# Check MediaBrowserService is declared
grep -A5 "YouTubeMediaService" app/src/main/AndroidManifest.xml
```

### Build & Test (Requires Android SDK)

1. **Install Android SDK**: https://developer.android.com/studio
2. **Get YouTube API Key**: https://console.cloud.google.com/
   - Enable YouTube Data API v3
   - Create API credentials (API Key)
3. **Build**:
   ```bash
   ./gradlew assembleDebug
   ```
4. **Install on phone**:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```
5. **Test in Android Auto**:
   - Open Android Auto on your phone
   - The app should appear in the app launcher
   - Or use the Desktop Head Unit emulator

### Using Android Auto Desktop Head Unit

For testing without a car:

1. Download Android Auto Desktop Head Unit: https://developer.android.com/training/automotive-media/testing
2. Connect your phone via USB
3. The app should appear

## Configuration

### API Key Setup

1. Open `app/src/main/res/values/strings.xml`
2. Replace `YOUR_API_KEY_HERE` with your YouTube Data API v3 key:
```xml
<string name="youtube_api_key">your_actual_api_key</string>
```

Or set as environment variable:
```bash
export YOUTUBE_API_KEY="your_api_key"
```

## Project Structure

```
app/
├── src/main/
│   ├── java/com/autoyoutube/
│   │   ├── ui/           # Phone UI (MainActivity, VideoAdapter)
│   │   ├── service/      # Android Auto service (YouTubeMediaService)
│   │   ├── data/         # YouTube API integration
│   │   └── model/        # Data models
│   └── res/
│       ├── xml/          # Android Auto descriptors
│       ├── layout/       # UI layouts
│       └── values/       # Strings, themes
├── build.gradle          # App dependencies
└── proguard-rules.pro
```

## CI/CD

GitHub Actions workflow is included in `.github/workflows/android.yml`:
- Builds debug APK on every push
- Verifies Android Auto configuration
- Uploads APK as artifact

## Known Limitations

1. **Video Playback**: Full video playback in Android Auto has restrictions. The app currently shows the media browser. Actual video playback would require:
   - YouTube Premium for background audio
   - Custom streaming implementation with ExoPlayer
   - Android Auto media session support

2. **API Quotas**: YouTube Data API has quota limits. Consider implementing caching.

3. **Authentication**: OAuth is prepared but needs user configuration for personalized content.

## License

MIT License
