package com.itsme.amkush.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import cn.dianbobo.dbb.util.HLog
import com.itsme.amkush.config.Config

/**
 * Centralized error handling utility — safe for non-Xposed environments.
 */
object ErrorHandler {

    private const val TAG = "ErrorHandler"
    private const val XPOSED_BRIDGE_CLASS = "de.robv.android.xposed.XposedBridge"

    fun handleError(tag: String, error: Throwable, showToast: Boolean = false, context: Context? = null) {
        HLog.d(tag, "Error: ${error.message}")

        if (Config.DEBUG_MODE) {
            error.printStackTrace()
        }

        if (showToast && context != null) {
            try {
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                // Ignore toast errors
            }
        }
    }

    /**
     * Safely logs to Xposed if available, otherwise falls back to standard Logcat.
     */
    fun handleXposedError(tag: String, error: Throwable) {
        val message = "$tag Error: ${error.message}"
        try {
            val xposedBridgeClass = Class.forName(XPOSED_BRIDGE_CLASS)
            val logMethod = xposedBridgeClass.getMethod("log", String::class.java)
            logMethod.invoke(null, message)

            if (Config.DEBUG_MODE) {
                val logThrowableMethod = xposedBridgeClass.getMethod("log", Throwable::class.java)
                logThrowableMethod.invoke(null, error)
            }
        } catch (e: Throwable) {
            Log.e(tag, "Xposed not active, logging to logcat: $message", error)
        }
    }

    fun handleMediaPlayerError(tag: String, what: Int, extra: Int): Boolean {
        HLog.d(tag, "MediaPlayer Error - What: $what, Extra: $extra")
        return true
    }

    fun handleStreamError(context: Context?, what: Int) {
        // FIX #24: Replaced Chinese toast text "直播接收失败" with English.
        val message = "Stream receive failed: $what"
        HLog.d("Stream", message)

        context?.let {
            try {
                Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                // Ignore toast errors
            }
        }
    }
}
