#!/bin/bash
# Android Auto Configuration Verification Script
# This script verifies that the app is correctly configured to show in Android Auto

set -e

echo "=============================================="
echo "  AutoYouTube - Android Auto Config Checker"
echo "=============================================="
echo ""

ERRORS=0
WARNINGS=0

check_file() {
    if [ -f "$1" ]; then
        echo "✓ $1 exists"
    else
        echo "✗ $1 MISSING"
        ((ERRORS++))
    fi
}

check_content() {
    if grep -q "$2" "$1" 2>/dev/null; then
        echo "✓ $1 contains '$2'"
    else
        echo "✗ $1 missing '$2'"
        ((ERRORS++))
    fi
}

check_content_contains() {
    if grep -q "$2" "$1" 2>/dev/null; then
        return 0
    else
        echo "✗ $1 should contain: $2"
        return 1
    fi
}

echo "=== 1. Checking Required Files ==="
check_file "app/src/main/AndroidManifest.xml"
check_file "app/src/main/res/xml/automotive_app_desc.xml"
check_file "app/src/main/java/com/autoyoutube/service/YouTubeMediaService.kt"

echo ""
echo "=== 2. Checking AndroidManifest.xml Configuration ==="

# Check for MediaBrowserService
if grep -q 'android:name=".service.YouTubeMediaService"' app/src/main/AndroidManifest.xml; then
    echo "✓ MediaBrowserService declared"
else
    echo "✗ MediaBrowserService NOT declared"
    ((ERRORS++))
fi

# Check for MediaBrowserService intent filter
if grep -q 'android.media.browse.MediaBrowserService' app/src/main/AndroidManifest.xml; then
    echo "✓ MediaBrowserService intent filter present"
else
    echo "✗ MediaBrowserService intent filter MISSING"
    ((ERRORS++))
fi

# Check for Android Auto meta-data
if grep -q 'com.google.android.gms.car.application' app/src/main/AndroidManifest.xml; then
    echo "✓ Android Auto meta-data present"
else
    echo "✗ Android Auto meta-data MISSING"
    ((ERRORS++))
fi

# Check automotive_app_desc.xml
echo ""
echo "=== 3. Checking automotive_app_desc.xml ==="
if grep -q '<uses name="media"/>' app/src/main/res/xml/automotive_app_desc.xml; then
    echo "✓ Media app type declared"
else
    echo "✗ Media app type NOT declared"
    ((ERRORS++))
fi

# Check service is exported
echo ""
echo "=== 4. Checking Service Configuration ==="
if grep -q 'android:exported="true"' app/src/main/AndroidManifest.xml; then
    echo "✓ Service is exported (required for Auto)"
else
    echo "✗ Service should be exported"
    ((ERRORS++))
fi

# Check Internet permission
echo ""
echo "=== 5. Checking Permissions ==="
if grep -q 'android.permission.INTERNET' app/src/main/AndroidManifest.xml; then
    echo "✓ INTERNET permission declared"
else
    echo "✗ INTERNET permission MISSING"
    ((ERRORS++))
fi

# Check for build.gradle dependencies
echo ""
echo "=== 6. Checking Dependencies ==="
if grep -q 'play-services-car' app/build.gradle; then
    echo "✓ play-services-car dependency present"
else
    echo "✗ play-services-car dependency MISSING"
    ((ERRORS++))
fi

if grep -q 'car.app' app/build.gradle; then
    echo "✓ car.app dependency present"
else
    echo "✗ car.app dependency MISSING"
    ((ERRORS++))
fi

# Summary
echo ""
echo "=============================================="
echo "  VERIFICATION SUMMARY"
echo "=============================================="

if [ $ERRORS -eq 0 ]; then
    echo -e "\033[0;32m✓ ALL CHECKS PASSED\033[0m"
    echo ""
    echo "The app is correctly configured to show in Android Auto!"
    echo ""
    echo "To test:"
    echo "  1. Build the APK: ./gradlew assembleDebug"
    echo "  2. Install on phone: adb install app/build/outputs/apk/debug/app-debug.apk"
    echo "  3. Open Android Auto - 'AutoYouTube' should appear"
    echo ""
    echo "Note: The app will work with demo data without a YouTube API key."
    echo "      Add your API key in strings.xml for real YouTube content."
    exit 0
else
    echo -e "\033[0;31m✗ $ERRORS ERROR(S) FOUND\033[0m"
    echo ""
    echo "Please fix the issues above before building."
    exit 1
fi
