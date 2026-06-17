package com.itsme.amkush.hooks.bypass

import com.itsme.amkush.utils.SafeHooker
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

object AntiXposedDetectionHook {

    fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        hideXposedClasses(lpparam)
        hideXposedPackages(lpparam)
        hideXposedInClassLoader(lpparam)
    }

    private fun hideXposedClasses(lpparam: XC_LoadPackage.LoadPackageParam) {
        val xposedClasses = listOf(
            "de.robv.android.xposed.XposedBridge",
            "de.robv.android.xposed.XposedHelpers",
            "de.robv.android.xposed.XC_MethodHook",
            "de.robv.android.xposed.XC_MethodReplacement",
            "de.robv.android.xposed.callbacks.XC_LoadPackage",
            "de.robv.android.xposed.IXposedHookLoadPackage",
            "de.robv.android.xposed.IXposedHookInitPackageResources",
            "de.robv.android.xposed.IXposedHookZygoteInit"
        )

        SafeHooker.hookMethod(lpparam, "java.lang.Class", "forName", String::class.java, callback = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val className = param.args[0] as? String ?: return
                if (xposedClasses.any { className.contains(it) }) {
                    param.throwable = ClassNotFoundException(className)
                }
            }
        })

        SafeHooker.hookMethod(lpparam, "java.lang.ClassLoader", "loadClass", String::class.java, callback = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val className = param.args[0] as? String ?: return
                if (xposedClasses.any { className.contains(it) }) {
                    param.throwable = ClassNotFoundException(className)
                }
            }
        })
    }

    private fun hideXposedPackages(lpparam: XC_LoadPackage.LoadPackageParam) {
        val xposedPackages = listOf(
            "de.robv.android.xposed.installer",
            "org.meowcat.edxposed.manager",
            "com.solohsu.android.edxp.manager",
            "org.lsposed.manager",
            "io.github.lsposed.manager",
            "com.android.xposed",
            "com.github.xposed"
        )

        SafeHooker.hookMethod(lpparam, "android.app.ApplicationPackageManager", "getPackageInfo", String::class.java, Int::class.javaPrimitiveType!!, callback = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val packageName = param.args[0] as? String ?: return
                if (xposedPackages.any { packageName.contains(it) }) {
                    param.throwable = android.content.pm.PackageManager.NameNotFoundException("Package not found")
                }
            }
        })
    }

    private fun hideXposedInClassLoader(lpparam: XC_LoadPackage.LoadPackageParam) {
        SafeHooker.hookMethod(lpparam, "java.lang.ClassLoader", "getParent", callback = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val parent = param.result ?: return
                if (parent.javaClass.name.contains("xposed", ignoreCase = true)) {
                    param.result = null
                }
            }
        })
    }
}