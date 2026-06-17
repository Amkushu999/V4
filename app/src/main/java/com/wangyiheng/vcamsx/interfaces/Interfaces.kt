package com.wangyiheng.vcamsx.interfaces

import android.net.Uri
import android.view.SurfaceHolder
import com.wangyiheng.vcamsx.data.models.VideoStatues
import java.io.File

/**
 * Interface for video player operations
 */
interface IVideoPlayer {
    fun playVideo(holder: SurfaceHolder, videoPath: String)
    fun playRTMPStream(holder: SurfaceHolder, rtmpUrl: String)
    fun release()
    fun isPlaying(): Boolean
}

/**
 * Interface for info/preferences management
 */
interface IInfoManager {
    fun saveVideoStatus(videoStatus: VideoStatues)
    fun getVideoStatus(): VideoStatues?
    fun removeVideoStatus()
}

/**
 * Interface for video file operations
 */
interface IVideoFileManager {
    fun copyVideoToAppDir(videoUri: Uri): File
    fun getVideoPath(): String
    fun videoExists(): Boolean
}