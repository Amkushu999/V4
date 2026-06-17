package com.itsme.amkush.hooks.bypass

import com.itsme.amkush.utils.SafeHooker
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.io.File

object EmulatorBypassHook {
    fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        spoofBuildProperties(lpparam)
        hideEmulatorFiles(lpparam)
        spoofTelephony(lpparam)
        // 🛡️ REMOVED: spoofOpenGL(lpparam) - Cannot hook native methods with standard Xposed
        spoofHardwareProperties(lpparam)
    }

    private fun spoofBuildProperties(lpparam: XC_LoadPackage.LoadPackageParam) {
        val buildClass = "android.os.Build"
        SafeHooker.setStaticObjectField(lpparam, buildClass, "FINGERPRINT", "google/raven/raven:13/TQ3A.230901.001/10694318:user/release-keys")
        SafeHooker.setStaticObjectField(lpparam, buildClass, "MODEL", "Pixel 6 Pro")
        SafeHooker.setStaticObjectField(lpparam, buildClass, "MANUFACTURER", "Google")
        SafeHooker.setStaticObjectField(lpparam, buildClass, "BRAND", "google")
        SafeHooker.setStaticObjectField(lpparam, buildClass, "HARDWARE", "gs101")
        SafeHooker.setStaticObjectField(lpparam, buildClass, "BOARD", "raven")
        SafeHooker.setStaticObjectField(lpparam, buildClass, "DEVICE", "raven")
        SafeHooker.setStaticObjectField(lpparam, buildClass, "PRODUCT", "raven")
        SafeHooker.setStaticObjectField(lpparam, buildClass, "BOOTLOADER", "raven-1.0-10694318")
        SafeHooker.setStaticObjectField(lpparam, buildClass, "RADIO", "g5123b-230823-230905-B-10694318")
    }

    private fun hideEmulatorFiles(lpparam: XC_LoadPackage.LoadPackageParam) {
        SafeHooker.hookMethod(lpparam, "java.io.File", "exists", callback = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val file = param.thisObject as? File ?: return
                val path = file.absolutePath.lowercase()
                val emulatorIndicators = listOf(
                    "/dev/qemu_pipe", "/dev/socket/qemud", "/dev/qemu_trace",
                    "/system/lib/libc_malloc_debug_qemu.so", "/sys/qemu_trace",
                    "/system/bin/qemu-props", "/dev/socket/qemud", "/dev/__properties__"
                )
                if (emulatorIndicators.any { path.contains(it) }) {
                    param.result = false
                }
            }
        })
    }

    private fun spoofTelephony(lpparam: XC_LoadPackage.LoadPackageParam) {
        val tmClass = "android.telephony.TelephonyManager"
        SafeHooker.hookMethod(lpparam, tmClass, "getDeviceId", callback = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val result = param.result as? String
                if (result.isNullOrBlank() || result == "15555215554" || result == "000000000000000") {
                    param.result = "354821094837261"
                }
            }
        })
        SafeHooker.hookMethod(lpparam, tmClass, "getLine1Number", callback = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val result = param.result as? String
                if (result == "15555215554") {
                    param.result = null
                }
            }
        })
    }

    private fun spoofHardwareProperties(lpparam: XC_LoadPackage.LoadPackageParam) {
        SafeHooker.hookMethod(lpparam, "android.os.SystemProperties", "get", String::class.java, callback = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val key = param.args[0] as? String ?: return
                if (key == "ro.hardware") {
                    param.result = "gs101"
                } else if (key == "ro.product.board") {
                    param.result = "raven"
                }
            }
        })
    }
}