# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep YouTube API classes
-keep class com.google.api.** { *; }
-keep class com.google.apis.** { *; }

# Keep Google Play Services
-keep class com.google.android.gms.** { *; }

# Keep model classes
-keep class com.autoyoutube.model.** { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
