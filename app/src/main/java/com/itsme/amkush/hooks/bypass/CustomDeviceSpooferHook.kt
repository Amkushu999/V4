package com.itsme.amkush.hooks.bypass

import android.content.Context
import com.crossbowffs.remotepreferences.RemotePreferences
import com.itsme.amkush.utils.SafeHooker
import de.robv.android.xposed.callbacks.XC_LoadPackage

object CustomDeviceSpooferHook {

    private const val PREFS_NAME = "device_spoof_prefs"
    private const val AUTHORITY = "com.itsme.amkush.preferences"

    /**
     * Reads the spoofed values from RemotePreferences and applies them.
     * This is called ONLY for the selected target app inside CameraHookManager.
     */
    fun applySpoof(context: Context, lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            val prefs = RemotePreferences(context, AUTHORITY, PREFS_NAME)
            val isEnabled = prefs.getBoolean("spoof_enabled", false)
            if (!isEnabled) return

            val buildClass = "android.os.Build"
            val versionClass = "android.os.Build\$VERSION"

            prefs.getString("spoof_brand", null)?.let { SafeHooker.setStaticObjectField(lpparam, buildClass, "BRAND", it) }
            prefs.getString("spoof_manufacturer", null)?.let { SafeHooker.setStaticObjectField(lpparam, buildClass, "MANUFACTURER", it) }
            prefs.getString("spoof_model", null)?.let { SafeHooker.setStaticObjectField(lpparam, buildClass, "MODEL", it) }
            prefs.getString("spoof_android_version", null)?.let { SafeHooker.setStaticObjectField(lpparam, versionClass, "RELEASE", it) }
            prefs.getString("spoof_security_patch", null)?.let { SafeHooker.setStaticObjectField(lpparam, versionClass, "SECURITY_PATCH", it) }
            prefs.getString("spoof_fingerprint", null)?.let { SafeHooker.setStaticObjectField(lpparam, buildClass, "FINGERPRINT", it) }

        } catch (e: Exception) {
            // Silently fail if RemotePreferences can't connect
        }
    }
}