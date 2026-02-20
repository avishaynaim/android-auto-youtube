#!/bin/bash
# Download Gradle Wrapper

set -e

GRADLE_VERSION="8.2"
DISTRIBUTION_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"

echo "Downloading Gradle wrapper..."

# Create wrapper directory
mkdir -p gradle/wrapper

# Download gradle-wrapper.jar if not exists
if [ ! -f "gradle/wrapper/gradle-wrapper.jar" ]; then
    echo "Downloading gradle-wrapper.jar..."
    # Get the jar from a known gradle installation or create a minimal one
    curl -sL "https://raw.githubusercontent.com/gradle/gradle/v${GRADLE_VERSION}.0/gradle/wrapper/gradle-wrapper.jar" \
        -o gradle/wrapper/gradle-wrapper.jar 2>/dev/null || \
    curl -sL "https://github.com/gradle/gradle/raw/v${GRADLE_VERSION}.0/gradle/wrapper/gradle-wrapper.jar" \
        -o gradle/wrapper/gradle-wrapper.jar
fi

# Create gradlew if not exists
if [ ! -f "gradlew" ]; then
    cat > gradlew << 'GRADLEW_SCRIPT'
#!/bin/sh
# Gradle wrapper stub - run gradle directly or use setup.sh
echo "Please run ./setup.sh to download the full Gradle wrapper"
echo "Or install Gradle: https://gradle.org/install/"
exit 1
GRADLEW_SCRIPT
    chmod +x gradlew
fi

echo "Gradle wrapper setup complete"
echo "Run ./setup.sh for full setup"
