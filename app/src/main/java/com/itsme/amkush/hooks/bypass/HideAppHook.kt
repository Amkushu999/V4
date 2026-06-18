package com.itsme.amkush.hooks.bypass

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import com.crossbowffs.remotepreferences.RemotePreferences
import com.itsme.amkush.config.Config
import com.itsme.amkush.utils.SafeHooker
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

object HideAppHook {

    // FIX #20: Cache the hidden-apps set with a TTL to avoid a fresh IPC call to
    // RemotePreferences on every single getInstalledApplications/getInstalledPackages
    // invocation. These methods can be called dozens of times per second by some apps
    // (e.g. launchers, social apps). Without caching this created unbounded IPC traffic.
    private var cachedHiddenApps: Set<String> = emptySet()
    private var cacheTimestamp: Long = 0L
    private const val CACHE_TTL_MS = 5_000L

    fun init(context: Context, lpparam: XC_LoadPackage.LoadPackageParam) {
        hookGetInstalledApplications(context, lpparam)
        hookGetInstalledPackages(context, lpparam)
    }

    private fun getHiddenApps(context: Context): Set<String> {
        val now = System.currentTimeMillis()
        if (now - cacheTimestamp < CACHE_TTL_MS) return cachedHiddenApps

        val freshSet = try {
            val prefs = RemotePreferences(
                context,
                Config.PREFS_AUTHORITY,
                "hidden_apps"
            )
            val listString = prefs.getString("hidden_list", "") ?: ""
            if (listString.isBlank()) emptySet()
            else listString.split(",").filter { it.isNotBlank() }.toSet()
        } catch (e: Exception) {
            emptySet()
        }

        cachedHiddenApps = freshSet
        cacheTimestamp = now
        return freshSet
    }

    private fun hookGetInstalledApplications(context: Context, lpparam: XC_LoadPackage.LoadPackageParam) {
        SafeHooker.hookMethod(
            lpparam,
            "android.app.ApplicationPackageManager",
            "getInstalledApplications",
            Int::class.javaPrimitiveType!!,
            callback = object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val hiddenApps = getHiddenApps(context)
                    if (hiddenApps.isEmpty()) return
                    @Suppress("UNCHECKED_CAST")
                    val apps = param.result as? MutableList<ApplicationInfo> ?: return
                    apps.removeAll { hiddenApps.contains(it.packageName) }
                }
            }
        )
    }

    private fun hookGetInstalledPackages(context: Context, lpparam: XC_LoadPackage.LoadPackageParam) {
        SafeHooker.hookMethod(
            lpparam,
            "android.app.ApplicationPackageManager",
            "getInstalledPackages",
            Int::class.javaPrimitiveType!!,
            callback = object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val hiddenApps = getHiddenApps(context)
                    if (hiddenApps.isEmpty()) return
                    @Suppress("UNCHECKED_CAST")
                    val packages = param.result as? MutableList<PackageInfo> ?: return
                    packages.removeAll { hiddenApps.contains(it.packageName) }
                }
            }
        )
    }
}
