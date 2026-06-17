package com.itsme.amkush.utils

import android.util.Log
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.util.*

object MediaPlayerManager {
    private const val MAX_PLAYER_COUNT = 5 
    private val playerQueue = LinkedList<IjkMediaPlayer>()

    // 🛡️ FIXED: Removed the init {} block. 
    // Creating IjkMediaPlayer instances before native libs are loaded causes UnsatisfiedLinkError.
    // Players are now created safely on-demand inside acquirePlayer().

    private var currentPlayingPlayer: IjkMediaPlayer? = null

    fun acquirePlayer(): IjkMediaPlayer {
        currentPlayingPlayer?.let {
            releasePlayer(it)
        }

        return if (playerQueue.isNotEmpty()) {
            currentPlayingPlayer = playerQueue.poll() 
            currentPlayingPlayer!!
        } else {
            currentPlayingPlayer = IjkMediaPlayer() 
            currentPlayingPlayer!!
        }
    }

    private fun releasePlayer(player: IjkMediaPlayer?) {
        player?.apply {
            reset()
            // 🛡️ FIXED: Only return to queue if below max capacity to prevent memory leaks
            if (playerQueue.size < MAX_PLAYER_COUNT) {
                playerQueue.offer(this) 
            } else {
                // If queue is full, actually release the native player to free memory
                release()
            }
        }
    }
}