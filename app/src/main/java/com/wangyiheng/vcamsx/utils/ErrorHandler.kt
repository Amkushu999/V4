package com.wangyiheng.vcamsx.utils

import android.content.Context
import android.widget.Toast
import cn.dianbobo.dbb.util.HLog
import com.wangyiheng.vcamsx.config.Config
import de.robv.android.xposed.XposedBridge

/**
 * Centralized error handling utility
 */
object ErrorHandler {
    
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
    
    fun handleXposedError(tag: String, error: Throwable) {
        val message = "$tag Error: ${error.message}"
        XposedBridge.log(message)
        
        if (Config.DEBUG_MODE) {
            XposedBridge.log(error)
        }
    }
    
    fun handleMediaPlayerError(tag: String, what: Int, extra: Int): Boolean {
        HLog.d(tag, "MediaPlayer Error - What: $what, Extra: $extra")
        return true // Error handled
    }
    
    fun handleStreamError(context: Context?, what: Int) {
        val message = "直播接收失败$what"
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