package com.itsme.amkush.utils

import android.content.Context
import android.widget.Toast
import cn.dianbobo.dbb.util.HLog
import com.itsme.amkush.config.Config
// REMOVE THIS: import de.robv.android.xposed.XposedBridge

object ErrorHandler {
    
    // Use a helper to check if Xposed is actually available at runtime
    private fun isXposedAvailable(): Boolean {
        return try {
            Class.forName("de.robv.android.xposed.XposedBridge")
            true
        } catch (e: Throwable) {
            false
        }
    }

    fun handleXposedError(tag: String, error: Throwable) {
        val message = "$tag Error: ${error.message}"
        
        if (isXposedAvailable()) {
            // Use reflection to call XposedBridge to avoid linking errors
            try {
                val clazz = Class.forName("de.robv.android.xposed.XposedBridge")
                val logMethod = clazz.getMethod("log", String::class.java)
                logMethod.invoke(null, message)
            } catch (e: Exception) {
                HLog.d(tag, "Failed to log to Xposed: ${e.message}")
            }
        } else {
            HLog.d(tag, "Standard Log: $message")
        }
    }

    // ... keep your other functions (handleError, etc) as they were
}
