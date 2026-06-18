package com.itsme.amkush.utils

import android.util.Log
import android.view.SurfaceHolder
import cn.dianbobo.dbb.util.HLog
import com.itsme.amkush.config.Config
import com.itsme.amkush.interfaces.IVideoPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer

class MediaPlayerController(private val context: android.content.Context) : IVideoPlayer {
    private var mediaPlayer: IjkMediaPlayer? = null
    private var retryCount = 0

    override fun playVideo(holder: SurfaceHolder, videoPath: String) {
        release()
        mediaPlayer = IjkMediaPlayer().apply {
            try {
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0L)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-all-videos", 0L)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-fps", 30L)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1L)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1L)

                // FIX #8 (CRITICAL): Set ALL listeners BEFORE prepareAsync(). Previously
                // prepareAsync() was called before setOnPreparedListener, creating a race
                // condition where a fast prepare would fire before the listener was registered,
                // causing the video to silently never start playing.
                setOnPreparedListener { start() }
                setOnErrorListener { _, what, extra ->
                    ErrorHandler.handleMediaPlayerError("VideoPlayer", what, extra)
                    true
                }

                dataSource = videoPath
                setDisplay(holder)
                prepareAsync()
            } catch (e: Exception) {
                ErrorHandler.handleError("VideoPlayer", e, true, context)
            }
        }
    }

    override fun playRTMPStream(holder: SurfaceHolder, rtmpUrl: String) {
        release()
        mediaPlayer = IjkMediaPlayer().apply {
            try {
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0L)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1L)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1L)
                setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1L)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0L)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec_mpeg4", 1L)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "analyzemaxduration", Config.DEFAULT_ANALYZE_DURATION)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "probesize", Config.DEFAULT_PROBE_SIZE)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "flush_packets", 1L)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 1L)
                setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1L)

                // FIX #9 (CRITICAL): Listeners before prepareAsync() — same race condition fix.
                setOnPreparedListener {
                    HLog.d("Stream", "Live stream preview received successfully")
                    start()
                }
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
        return try {
            mediaPlayer?.isPlaying ?: false
        } catch (e: Exception) {
            false
        }
    }

    fun getPlayer(): IjkMediaPlayer? = mediaPlayer
}
