package com.wangyiheng.vcamsx.utils

/**
 * Singleton that holds the video playback state.
 * The UI updates this, and the Xposed Hook reads it to control ijkplayer.
 */
object PlaybackState {
    
    // Whether the video should be playing
    @Volatile
    var isPlaying: Boolean = true
    
    // Whether the video should loop
    @Volatile
    var isLooping: Boolean = true
    
    /**
     * Toggle play/pause
     */
    fun togglePlayback() {
        isPlaying = !isPlaying
    }
    
    /**
     * Reset to default state
     */
    fun reset() {
        isPlaying = true
        isLooping = true
    }
}