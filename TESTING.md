# AutoYouTube - Testing Guide

## Quick Verification (No Build Required)

Run the configuration checker:
```bash
cd android-auto-youtube
./verify-config.sh
```

This verifies:
- ✅ MediaBrowserService is declared
- ✅ Android Auto meta-data is present
- ✅ Required permissions are granted
- ✅ Dependencies are configured

## Building the APK

### Option 1: Android Studio (Recommended)
1. Open Android Studio
2. File → Open → Select `android-auto-youtube` folder
3. Wait for Gradle sync to complete
4. Build → Build Bundle(s) / APK(s) → Build APK(s)

### Option 2: Command Line
```bash
# Install Android SDK first (see SETUP.md)
export ANDROID_HOME=~/android-sdk
./gradlew assembleDebug
```

APK location: `app/build/outputs/apk/debug/app-debug.apk`

## Testing in Android Auto

### Method 1: Desktop Head Unit (Recommended for testing)
1. Download Android Auto Desktop Head Unit from:
   https://developer.android.com/training/automotive-media/testing

2. Enable developer mode on your phone:
   - Open Android Auto settings
   - Tap the version number 10 times
   - Enable "Unknown sources" in developer settings

3. Connect phone via USB

4. Launch Desktop Head Unit on computer

5. Look for "AutoYouTube" in the app launcher

### Method 2: In-Car Testing
1. Install APK on your phone:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

2. Connect phone to car via USB

3. Open Android Auto on phone (or it launches automatically)

4. The app should appear in the app list

### Method 3: Android Auto Emulator
1. Create Android Virtual Device (AVD) in Android Studio
2. Install Google Play Services for Auto
3. Launch Android Auto emulator
4. Install and test the app

## Troubleshooting

### App Not Showing in Android Auto

**Check 1: Is the app installed?**
```bash
adb shell pm list packages | grep autoyoutube
```

**Check 2: Is MediaBrowserService running?**
```bash
adb shell dumpsys media.browser
```

**Check 3: Check Android Auto logs**
```bash
adb logcat | grep -i auto
```

**Common fixes:**
- Clear Android Auto cache: Settings → Apps → Android Auto → Clear Cache
- Restart phone
- Check developer mode is enabled

### Build Errors

**"SDK location not found"**
- Set ANDROID_HOME environment variable
- Or edit `local.properties` with correct SDK path

**"Java version mismatch"**
- Install Java 17
- Set JAVA_HOME to Java 17 path

**"Gradle daemon issues"**
```bash
./gradlew --stop
./gradlew clean
```

## Demo Mode vs Real YouTube

### Demo Mode (Default)
The app works out of the box with demo content:
- 5 sample videos
- Sample playlists
- Search works on demo data
- No API key needed

### Real YouTube (Requires API Key)
1. Get API key: https://console.cloud.google.com/
2. Enable "YouTube Data API v3"
3. Add key to `app/src/main/res/values/strings.xml`:
```xml
<string name="youtube_api_key">YOUR_API_KEY_HERE</string>
```

### OAuth (For personalized content)
To access user's subscriptions/playlists:
1. Configure OAuth consent screen in Google Cloud Console
2. Add OAuth client ID to the app
3. User signs in via Google

## Project Structure

```
android-auto-youtube/
├── app/
│   ├── src/main/
│   │   ├── java/com/autoyoutube/
│   │   │   ├── ui/           # Phone UI
│   │   │   ├── service/      # Android Auto services
│   │   │   ├── data/         # API integration
│   │   │   └── model/        # Data models
│   │   └── res/
│   │       ├── xml/          # Android Auto descriptors
│   │       └── ...
│   └── build.gradle
├── build.gradle
├── verify-config.sh          # Config verification
├── setup.sh                  # Environment setup
└── README.md
```

## Next Steps After Testing

1. ✅ App shows in Android Auto
2. ⬜ Add YouTube API key for real content
3. ⬜ Configure OAuth for user data
4. ⬜ Implement actual video playback
5. ⬜ Add error handling
6. ⬜ Publish to Google Play Store

## Known Limitations

1. **Video Playback**: Full video playback in Android Auto is restricted. The app provides audio streaming only.
2. **API Quotas**: YouTube Data API has daily limits (10,000 units/day free)
3. **OAuth**: Requires app verification for production use
