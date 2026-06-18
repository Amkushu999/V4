package com.itsme.amkush.config

/**
 * Centralized configuration and constants
 */
object Config {
    // Content Provider URIs
    const val VIDEO_PROVIDER_URI = "content://com.itsme.amkush.videoprovider"
    const val PREFS_AUTHORITY = "com.itsme.amkush.preferences"
    const val PREFS_FILE_NAME = "main_prefs"

    // Default values
    const val DEFAULT_RTMP_URL = "rtmp://ns8.indexforce.com/home/mystream"
    const val VIDEO_FILE_NAME = "copied_video.mp4"
    const val DEFAULT_FALLBACK_VIDEO = "facegate"

    // Player settings
    const val MAX_RETRY_COUNT = 5
    const val DEFAULT_PROBE_SIZE = 1024L
    const val DEFAULT_ANALYZE_DURATION = 100L
    const val MAX_BUFFER_SIZE = 8192L
    const val MIN_FRAMES = 2

    // SurfaceTexture
    const val FAKE_SURFACE_TEXTURE_ID = 10

    // Logging
    // FIX #17: Updated LOG_TAG from stale "VCAMSX" to current app name "FACEGATE"
    const val LOG_TAG = "FACEGATE"

    // FIX #18: DEBUG_MODE was hardcoded true, which caused verbose stack-trace logging
    // and debug toasts in production builds. Set to false so debug output is suppressed
    // in release APKs. Re-enable locally when debugging.
    const val DEBUG_MODE = false

    // Package names
    const val PACKAGE_NAME = "com.itsme.amkush"
}
