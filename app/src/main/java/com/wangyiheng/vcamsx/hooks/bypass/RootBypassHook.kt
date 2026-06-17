package com.wangyiheng.vcamsx.hooks.bypass

import android.os.Build
import com.wangyiheng.vcamsx.utils.SafeHooker
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.io.File

object RootBypassHook {

    fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        hookFileExists(lpparam)
        hookRuntimeExec(lpparam)
        hookProcessBuilder(lpparam)
        hookPackageManager(lpparam)
    }

    private fun hookFileExists(lpparam: XC_LoadPackage.LoadPackageParam) {
        SafeHooker.hookMethod(lpparam, "java.io.File", "exists", callback = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val file = param.thisObject as? File ?: return
                val path = file.absolutePath.lowercase()
                
                val rootIndicators = listOf(
                    "/su", "/magisk", "/superuser", "/xbin/su", "/system/app/Superuser",
                    "/sbin/su", "/data/local/xbin/su", "/data/local/bin/su",
                    "/system/xbin/daemonsu", "/system/etc/init.d/99SuperSUDaemon",
                    "/system/bin/.ext/.su", "/system/usr/we-need-root/su-backup",
                    "/system/xbin/mu", "/data/adb/magisk", "/data/adb/kernelsu"
                )
                
                if (rootIndicators.any { path.contains(it) }) {
                    param.result = false
                }
            }
        })
    }

    private fun hookRuntimeExec(lpparam: XC_LoadPackage.LoadPackageParam) {
        SafeHooker.hookMethod(lpparam, "java.lang.Runtime", "exec", String::class.java, callback = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val command = param.args[0] as? String ?: return
                if (command.contains("su") || command.contains("magisk") || command.contains("mounts")) {
                    param.throwable = java.io.IOException("Permission denied")
                }
            }
        })
        
        SafeHooker.hookMethod(lpparam, "java.lang.Runtime", "exec", Array<String>::class.java, callback = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val commands = param.args[0] as? Array<*> ?: return
                val joined = commands.joinToString(" ").lowercase()
                if (joined.contains("su") || joined.contains("magisk") || joined.contains("mounts")) {
                    param.throwable = java.io.IOException("Permission denied")
                }
            }
        })
    }

    private fun hookProcessBuilder(lpparam: XC_LoadPackage.LoadPackageParam) {
        SafeHooker.hookMethod(lpparam, "java.lang.ProcessBuilder", "start", callback = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val builder = param.thisObject as? ProcessBuilder ?: return
                val commands = builder.command()
                val joined = commands.joinToString(" ").lowercase()
                if (joined.contains("su") || joined.contains("magisk")) {
                    param.throwable = java.io.IOException("Permission denied")
                }
            }
        })
    }

    private fun hookPackageManager(lpparam: XC_LoadPackage.LoadPackageParam) {
        val rootApps = listOf(
            "com.topjohnwu.magisk", "io.github.huskydg.magisk", "com.koushikdutta.superuser",
            "eu.chainfire.supersu", "me.weishu.kernelsu", "com.noshufou.android.su",
            "com.thirdparty.superuser", "com.yellowes.su"
        )

        if (Build.VERSION.SDK_INT >= 33) {
            SafeHooker.hookMethod(lpparam, "android.app.ApplicationPackageManager", "getPackageInfo", String::class.java, android.content.pm.PackageManager.PackageInfoFlags::class.java, callback = object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val packageName = param.args[0] as? String ?: return
                    if (packageName in rootApps) {
                        param.throwable = android.content.pm.PackageManager.NameNotFoundException("Package not found")
                    }
                }
            })
        } else {
            SafeHooker.hookMethod(lpparam, "android.app.ApplicationPackageManager", "getPackageInfo", String::class.java, Int::class.javaPrimitiveType!!, callback = object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val packageName = param.args[0] as? String ?: return
                    if (packageName in rootApps) {
                        param.throwable = android.content.pm.PackageManager.NameNotFoundException("Package not found")
                    }
                }
            })
        }
    }
}