#!/bin/bash
# AutoYouTube Android Auto Setup Script
# This script helps set up the development environment

set -e

echo "============================================"
echo "  AutoYouTube - Android Auto Setup Script"
echo "============================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if Android SDK is installed
check_android_sdk() {
    echo -n "Checking for Android SDK... "
    
    if [ -n "$ANDROID_HOME" ] && [ -d "$ANDROID_HOME" ]; then
        echo -e "${GREEN}Found at ANDROID_HOME${NC}"
        return 0
    elif [ -n "$ANDROID_SDK_ROOT" ] && [ -d "$ANDROID_SDK_ROOT" ]; then
        echo -e "${GREEN}Found at ANDROID_SDK_ROOT${NC}"
        return 0
    elif [ -d "$HOME/Android/Sdk" ]; then
        echo -e "${GREEN}Found at ~/Android/Sdk${NC}"
        return 0
    elif [ -d "/opt/android-sdk" ]; then
        echo -e "${GREEN}Found at /opt/android-sdk${NC}"
        return 0
    else
        echo -e "${YELLOW}Not found${NC}"
        return 1
    fi
}

# Install Android SDK if not present
install_android_sdk() {
    echo -e "${YELLOW}Android SDK not found. Would you like to install it? (y/n)${NC}"
    read -r response
    
    if [ "$response" = "y" ] || [ "$response" = "Y" ]; then
        echo "Installing Android SDK..."
        
        # Download command line tools
        mkdir -p "$HOME/android-sdk/cmdline-tools"
        cd "$HOME/android-sdk/cmdline-tools"
        
        if [ ! -f commandlinetools-linux.zip ]; then
            wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O commandlinetools-linux.zip
            unzip -q commandlinetools-linux.zip
            mv cmdline-tools latest
        fi
        
        # Accept licenses and install required packages
        yes | "$HOME/android-sdk/cmdline-tools/latest/bin/sdkmanager" --licenses > /dev/null 2>&1 || true
        "$HOME/android-sdk/cmdline-tools/latest/bin/sdkmanager" "platform-tools" "platforms;android-34" "build-tools;34.0.0"
        
        echo -e "${GREEN}Android SDK installed successfully!${NC}"
        echo "Add these to your ~/.bashrc:"
        echo "  export ANDROID_HOME=\$HOME/android-sdk"
        echo "  export PATH=\$PATH:\$ANDROID_HOME/cmdline-tools/latest/bin:\$ANDROID_HOME/platform-tools"
    else
        echo "Skipping SDK installation."
    fi
}

# Check Java
check_java() {
    echo -n "Checking for Java... "
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2)
        echo -e "${GREEN}Found ($JAVA_VERSION)${NC}"
        
        # Check for Java 17+
        if [[ "$JAVA_VERSION" < "17" ]]; then
            echo -e "${YELLOW}Warning: Java 17+ recommended for Android development${NC}"
        fi
    else
        echo -e "${RED}Not found - Please install Java 17+${NC}"
        exit 1
    fi
}

# Check Gradle
check_gradle() {
    echo -n "Checking for Gradle... "
    if [ -f "./gradlew" ]; then
        chmod +x ./gradlew
        echo -e "${GREEN}Found (wrapper)${NC}"
    elif command -v gradle &> /dev/null; then
        echo -e "${GREEN}Found (system)${NC}"
    else
        echo -e "${YELLOW}Not found - Will use Gradle wrapper${NC}"
    fi
}

# Configure YouTube API
configure_youtube_api() {
    echo ""
    echo "=== YouTube API Configuration ==="
    echo -n "Do you have a YouTube Data API v3 key? (y/n): "
    read -r response
    
    if [ "$response" = "y" ] || [ "$response" = "Y" ]; then
        echo -n "Enter your YouTube API key: "
        read -r API_KEY
        
        if [ -n "$API_KEY" ]; then
            # Update strings.xml
            sed -i "s/YOUR_API_KEY_HERE/$API_KEY/" app/src/main/res/values/strings.xml
            echo -e "${GREEN}API key configured!${NC}"
        fi
    else
        echo "You can get an API key from:"
        echo "  https://console.cloud.google.com/"
        echo ""
        echo "1. Create a project"
        echo "2. Enable YouTube Data API v3"
        echo "3. Create API credentials (API Key)"
        echo "4. Add the key to app/src/main/res/values/strings.xml"
    fi
}

# Build the app
build_app() {
    echo ""
    echo "=== Building Debug APK ==="
    
    if check_android_sdk; then
        export ANDROID_HOME="${ANDROID_HOME:-$HOME/android-sdk}"
        export ANDROID_SDK_ROOT="$ANDROID_HOME"
        
        ./gradlew assembleDebug
        
        if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
            echo -e "${GREEN}Build successful!${NC}"
            echo "APK location: app/build/outputs/apk/debug/app-debug.apk"
        else
            echo -e "${RED}Build failed!${NC}"
        fi
    else
        echo -e "${YELLOW}Cannot build without Android SDK${NC}"
    fi
}

# Main menu
main() {
    check_java
    check_gradle
    
    if ! check_android_sdk; then
        install_android_sdk
    fi
    
    echo ""
    echo "=== Setup Complete ==="
    echo ""
    echo "Next steps:"
    echo "  1. Configure YouTube API (optional - works with demo data without)"
    echo "  2. Run './gradlew assembleDebug' to build"
    echo "  3. Install APK on your phone"
    echo "  4. Open Android Auto - the app should appear!"
    echo ""
    echo -n "Build now? (y/n): "
    read -r response
    
    if [ "$response" = "y" ] || [ "$response" = "Y" ]; then
        build_app
    fi
}

main "$@"
