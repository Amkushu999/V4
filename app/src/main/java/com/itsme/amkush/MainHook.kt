package com.itsme.amkush

import android.app.Application
import android.content.Context
import android.net.Uri
import cn.dianbobo.dbb.util.HLog
import com.itsme.amkush.config.Config
import com.itsme.amkush.data.models.VideoStatues
import com.itsme.amkush.hooks.CameraHookManager
import com.itsme.amkush.utils.InfoManager
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage
import tv.danmaku.ijk.media.player.IjkMediaPlayer

class MainHook : IXposedHookLoadPackage {

    companion object {
        // FIX #4: Updated tag to match the current app name
        const val TAG = "FACEGATE_Hook"

        // FIX #5: @Volatile on all fields shared across Xposed hook threads,
        // media player callbacks, and initialization. Without @Volatile, JVM
        // may cache stale values in thread-local registers causing silent failures.
        @Volatile var context: Context? = null
        @Volatile var infoManager: InfoManager? = null
        @Volatile var videoStatus: VideoStatues? = null
        @Volatile var ijkMediaPlayer: IjkMediaPlayer? = null
        @Volatile var TheOnlyPlayer: IjkMediaPlayer? = null
        @Volatile var imageUri: String? = null

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

                    // FIX #6: Listeners must be set BEFORE prepareAsync() to avoid a race
                    // condition where the player prepares before the listener is registered,
                    // causing start() to never be called and video to silently not play.
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

                    // FIX #7 (CRITICAL): Moved ALL listeners BEFORE prepareAsync().
                    // Previously setOnPreparedListener was set AFTER prepareAsync() which is a
                    // race condition — if the stream prepared fast, start() was never called.
                    setOnPreparedListener {
                        HLog.i(TAG, "Stream prepared successfully")
                        start()
                    }

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
