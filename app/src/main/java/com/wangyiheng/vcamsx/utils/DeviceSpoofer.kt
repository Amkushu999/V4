package com.wangyiheng.vcamsx.utils

import android.content.Context
import com.crossbowffs.remotepreferences.RemotePreferences

object DeviceSpoofer {
    private const val PREFS_NAME = "device_spoof_prefs"
    private const val AUTHORITY = "com.wangyiheng.vcamsx.preferences"

    private fun getPrefs(context: Context): RemotePreferences {
        return RemotePreferences(context, AUTHORITY, PREFS_NAME)
    }

    fun applySpoof(
        context: Context,
        brand: String,
        manufacturer: String,
        model: String,
        androidVersion: String,
        buildId: String,
        securityPatch: String
    ) {
        // 🆕 Automatically generate a realistic FINGERPRINT based on the user's inputs
        val fingerprint = "$brand/${model}/${model}:${androidVersion}/${buildId}:user/release-keys"
        
        getPrefs(context).edit().apply {
            putString("spoof_brand", brand)
            putString("spoof_manufacturer", manufacturer)
            putString("spoof_model", model)
            putString("spoof_android_version", androidVersion)
            putString("spoof_build_id", buildId)
            putString("spoof_security_patch", securityPatch)
            putString("spoof_fingerprint", fingerprint)
            putBoolean("spoof_enabled", true)
            apply()
        }
    }

    fun clearSpoof(context: Context) {
        getPrefs(context).edit().clear().apply()
    }

    fun isSpoofEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean("spoof_enabled", false)
    }
}