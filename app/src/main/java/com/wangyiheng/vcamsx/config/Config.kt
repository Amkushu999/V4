package com.wangyiheng.vcamsx.config

/**
 * Centralized configuration and constants
 */
object Config {
    // Content Provider URIs
    const val VIDEO_PROVIDER_URI = "content://com.wangyiheng.vcamsx.videoprovider"
    const val PREFS_AUTHORITY = "com.wangyiheng.vcamsx.preferences"
    const val PREFS_FILE_NAME = "main_prefs"
    
    // Default values
    const val DEFAULT_RTMP_URL = "rtmp://ns8.indexforce.com/home/mystream"
    const val VIDEO_FILE_NAME = "copied_video.mp4"
    const val DEFAULT_FALLBACK_VIDEO = "vcamsx"
    
    // Player settings
    const val MAX_RETRY_COUNT = 5
    const val DEFAULT_PROBE_SIZE = 1024L
    const val DEFAULT_ANALYZE_DURATION = 100L
    const val MAX_BUFFER_SIZE = 8192L
    const val MIN_FRAMES = 2
    
    // SurfaceTexture
    const val FAKE_SURFACE_TEXTURE_ID = 10
    
    // Logging
    const val LOG_TAG = "VCAMSX"
    const val DEBUG_MODE = true
    
    // Package names
    const val PACKAGE_NAME = "com.wangyiheng.vcamsx"
}