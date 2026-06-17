package com.wangyiheng.vcamsx.hooks.bypass

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import com.crossbowffs.remotepreferences.RemotePreferences
import com.wangyiheng.vcamsx.config.Config
import com.wangyiheng.vcamsx.utils.SafeHooker
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

object HideAppHook {

    fun init(context: Context, lpparam: XC_LoadPackage.LoadPackageParam) {
        hookGetInstalledApplications(context, lpparam)
        hookGetInstalledPackages(context, lpparam)
    }

    private fun getHiddenApps(context: Context): Set<String> {
        return try {
            val prefs = RemotePreferences(
                context,
                Config.PREFS_AUTHORITY,
                "hidden_apps"
            )
            val listString = prefs.getString("hidden_list", "") ?: ""
            if (listString.isBlank()) {
                emptySet()
            } else {
                listString.split(",").filter { it.isNotBlank() }.toSet()
            }
        } catch (e: Exception) {
            emptySet()
        }
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