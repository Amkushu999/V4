package com.wangyiheng.vcamsx.utils

import android.content.Context
import android.util.Log
import android.view.SurfaceHolder
import cn.dianbobo.dbb.util.HLog
import com.wangyiheng.vcamsx.config.Config
import com.wangyiheng.vcamsx.interfaces.IVideoPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer

class MediaPlayerController(private val context: Context) : IVideoPlayer {
    private var mediaPlayer: IjkMediaPlayer? = null
    private var retryCount = 0
    
    override fun playVideo(holder: SurfaceHolder, videoPath: String) {
        release() 
        mediaPlayer = IjkMediaPlayer().apply {
            try {
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-all-videos", 0)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-fps", 30)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1)

                dataSource = videoPath
                setDisplay(holder)
                prepareAsync()
                
                setOnPreparedListener { start() }
                setOnErrorListener { _, what, extra ->
                    ErrorHandler.handleMediaPlayerError("VideoPlayer", what, extra)
                    true
                }
            } catch (e: Exception) {
                ErrorHandler.handleError("VideoPlayer", e, true, context)
            }
        }
    }
    
    override fun playRTMPStream(holder: SurfaceHolder, rtmpUrl: String) {
        release() 
        mediaPlayer = IjkMediaPlayer().apply {
            try {
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1)
                setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec_mpeg4", 1)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "analyzemaxduration", Config.DEFAULT_ANALYZE_DURATION)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "probesize", Config.DEFAULT_PROBE_SIZE)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "flush_packets", 1L)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 1L)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1L)
                
                setOnErrorListener { _, what, extra ->
                    ErrorHandler.handleStreamError(context, what)
                    true
                }
                setOnInfoListener { _, what, extra ->
                    Log.i("IjkMediaPlayer", "Info received. What: $what, Extra: $extra")
                    true
                }
                
                dataSource = rtmpUrl
                setDisplay(holder)
                prepareAsync()
                
                setOnPreparedListener {
                    HLog.d("Stream", "Live stream preview received successfully")
                    start()
                }
            } catch (e: Exception) {
                if (retryCount < Config.MAX_RETRY_COUNT) {
                    retryCount++
                    HLog.d("Stream", "Retry attempt $retryCount")
                } else {
                    ErrorHandler.handleError("Stream", e, true, context)
                }
            }
        }
    }
    
    override fun release() {
        mediaPlayer?.let { player ->
            try {
                // 🛡️ FIXED: Use isPlaying() instead of the fake isPlayable() stub
                if (player.isPlaying) player.stop()
                player.release()
            } catch (e: Exception) {
                ErrorHandler.handleError("MediaPlayer", e)
            }
        }
        mediaPlayer = null
        retryCount = 0
    }
    
    override fun isPlaying(): Boolean {
        // 🛡️ FIXED: Use the actual native isPlaying() method
        return try {
            mediaPlayer?.isPlaying ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    fun getPlayer(): IjkMediaPlayer? = mediaPlayer
}