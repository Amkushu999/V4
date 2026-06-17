package com.wangyiheng.vcamsx.utils

import android.content.Context
import android.content.SharedPreferences
import com.crossbowffs.remotepreferences.RemotePreferences
import com.wangyiheng.vcamsx.config.Config

/**
 * Singleton that holds the real-time transformation state (Zoom and Pan).
 */
object MediaTransformState {
    private const val PREFS_NAME = "transform_prefs"
    private const val KEY_SCALE = "scale"
    private const val KEY_OFFSET_X = "offsetX"
    private const val KEY_OFFSET_Y = "offsetY"

    const val MAX_SCALE = 5.0f
    const val MIN_SCALE = 1.0f
    const val MAX_PAN = 2000f

    @Volatile var scale: Float = 1.0f
        set(value) { field = value; saveState() }
        
    @Volatile var offsetX: Float = 0.0f
        set(value) { field = value; saveState() }
        
    @Volatile var offsetY: Float = 0.0f
        set(value) { field = value; saveState() }

    private var prefs: SharedPreferences? = null

    /**
     * 🛡️ FIXED: Call this from the Xposed Hook (Target App Process).
     * Uses RemotePreferences to read FaceGate's prefs cross-process.
     */
    fun init(context: Context) {
        prefs = RemotePreferences(context, Config.PREFS_AUTHORITY, PREFS_NAME)
        loadState()
    }

    /**
     * Call this from the FaceGate UI (FaceGate Process).
     * Local SharedPrefs are fine here because RemotePreferenceProvider serves this file.
     */
    fun initUI(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun saveState() {
        prefs?.edit()?.apply {
            putFloat(KEY_SCALE, scale)
            putFloat(KEY_OFFSET_X, offsetX)
            putFloat(KEY_OFFSET_Y, offsetY)
            apply()
        }
    }

    private fun loadState() {
        prefs?.let {
            scale = it.getFloat(KEY_SCALE, 1.0f)
            offsetX = it.getFloat(KEY_OFFSET_X, 0.0f)
            offsetY = it.getFloat(KEY_OFFSET_Y, 0.0f)
        }
    }

    fun reset() {
        scale = 1.0f
        offsetX = 0.0f
        offsetY = 0.0f
    }
}