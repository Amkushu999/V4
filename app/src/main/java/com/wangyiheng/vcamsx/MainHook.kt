package com.wangyiheng.vcamsx

import android.app.Application
import android.content.Context
import android.net.Uri
import cn.dianbobo.dbb.util.HLog
import com.wangyiheng.vcamsx.config.Config
import com.wangyiheng.vcamsx.data.models.VideoStatues
import com.wangyiheng.vcamsx.hooks.CameraHookManager
import com.wangyiheng.vcamsx.utils.InfoManager
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage
import tv.danmaku.ijk.media.player.IjkMediaPlayer

class MainHook : IXposedHookLoadPackage {
    
    companion object {
        const val TAG = "VCAMSX_Hook"
        
        var context: Context? = null
        var infoManager: InfoManager? = null
        var videoStatus: VideoStatues? = null
        var ijkMediaPlayer: IjkMediaPlayer? = null
        var TheOnlyPlayer: IjkMediaPlayer? = null
        
        var imageUri: String? = null
        
        fun initStatus() {
            infoManager?.let { 
                videoStatus = it.getVideoStatus() 
            }
        }
        
        fun initIjkPlayer() {
            val ctx = context ?: return
            if (ijkMediaPlayer == null) {
                ijkMediaPlayer = IjkMediaPlayer().apply {
                    setVolume(0F, 0F)
                    
                    val codecType = videoStatus?.codecType ?: false
                    val mediaCodecOption = if (codecType) 1L else 0L
                    setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", mediaCodecOption)
                    
                    setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", Config.DEFAULT_PROBE_SIZE)
                    setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1L)
                    setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0L)
                    setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "min-frames", Config.MIN_FRAMES.toLong())
                    
                    setOnPreparedListener {
                        isLooping = true
                        start()
                    }
                    
                    setOnErrorListener { _, what, extra ->
                        HLog.e(TAG, "Local video error: What=$what, Extra=$extra")
                        stop()
                        true
                    }
                    
                    try {
                        setDataSource(ctx, Uri.parse(Config.VIDEO_PROVIDER_URI))
                        prepareAsync()
                    } catch (e: Exception) {
                        HLog.e(TAG, "Failed to set local video source: ${e.message}")
                    }
                }
            }
        }
        
        fun initRTMPStream() {
            val url = videoStatus?.liveURL ?: return
            
            if (ijkMediaPlayer == null) {
                ijkMediaPlayer = IjkMediaPlayer().apply {
                    setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0L)
                    setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-hevc", 0L)
                    setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 0L)
                    setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0L)
                    setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec_mpeg4", 1L)
                    setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "analyzemaxduration", Config.DEFAULT_ANALYZE_DURATION)
                    setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1L)
                    setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0L)
                    setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-buffer-size", 1024L)
                    setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "min-frames", 0L)
                    setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max_cached_duration", 100L)
                    setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "infbuf", 0L)
                    
                    setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1L)
                    setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", Config.DEFAULT_PROBE_SIZE)
                    
                    setVolume(0F, 0F)
                    
                    setOnErrorListener { _, what, extra ->
                        HLog.e(TAG, "Stream error: What=$what, Extra=$extra")
                        true
                    }
                    
                    setOnInfoListener { _, what, extra ->
                        HLog.i(TAG, "Stream info: What=$what, Extra=$extra")
                        true
                    }
                    
                    try {
                        dataSource = url
                        prepareAsync()
                        
                        setOnPreparedListener {
                            HLog.i(TAG, "Stream prepared successfully")
                            start()
                        }
                    } catch (e: Exception) {
                        HLog.e(TAG, "Failed to initialize RTMP stream: ${e.message}")
                    }
                }
            }
        }
        
        fun releasePlayer() {
            ijkMediaPlayer?.let { player ->
                try {
                    if (player.isPlaying) {
                        player.stop()
                    }
                    player.release()
                    ijkMediaPlayer = null
                    TheOnlyPlayer = null
                    HLog.d(TAG, "Player released successfully")
                } catch (e: Exception) {
                    HLog.e(TAG, "Error releasing player: ${e.message}")
                }
            }
        }
    }
    
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName == Config.PACKAGE_NAME) return
        
        if (lpparam.processName.contains(":")) {
            HLog.d(TAG, "Skipping background process: ${lpparam.processName}")
            return
        }
        
        CameraHookManager.initHooks(lpparam)
        
        HLog.d(TAG, "Hooks initialized for: ${lpparam.packageName}")
    }
}