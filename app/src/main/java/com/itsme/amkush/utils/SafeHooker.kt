package com.itsme.amkush.utils

import cn.dianbobo.dbb.util.HLog
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Safe wrapper around XposedHelpers that catches exceptions
 * and logs them instead of crashing the target app.
 */
object SafeHooker {
    
    private const val TAG = "SafeHooker"
    
    /**
     * Safely hooks a method. If the method doesn't exist or hooking fails,
     * it logs the error instead of crashing.
     */
    fun hookMethod(
        lpparam: XC_LoadPackage.LoadPackageParam,
        className: String,
        methodName: String,
        vararg parameterTypes: Any,
        callback: XC_MethodHook
    ) {
        try {
            XposedHelpers.findAndHookMethod(
                className,
                lpparam.classLoader,
                methodName,
                *parameterTypes,
                callback
            )
        } catch (e: Throwable) {
            HLog.e(TAG, "Failed to hook $className.$methodName: ${e.message}")
        }
    }
    
    /**
     * Safely sets a static object field. If the field doesn't exist,
     * it logs the error instead of crashing.
     */
    fun setStaticObjectField(
        lpparam: XC_LoadPackage.LoadPackageParam,
        className: String,
        fieldName: String,
        value: Any?
    ) {
        try {
            val clazz = XposedHelpers.findClass(className, lpparam.classLoader)
            XposedHelpers.setStaticObjectField(clazz, fieldName, value)
        } catch (e: Throwable) {
            HLog.e(TAG, "Failed to set static field $className.$fieldName: ${e.message}")
        }
    }
    
    /**
     * Safely hooks a constructor.
     */
    fun hookConstructor(
        lpparam: XC_LoadPackage.LoadPackageParam,
        className: String,
        vararg parameterTypes: Any,
        callback: XC_MethodHook
    ) {
        try {
            val clazz = XposedHelpers.findClass(className, lpparam.classLoader)
            XposedHelpers.findAndHookConstructor(
                clazz,
                *parameterTypes,
                callback
            )
        } catch (e: Throwable) {
            HLog.e(TAG, "Failed to hook constructor $className: ${e.message}")
        }
    }
    
    /**
     * Safely gets an object field from an instance.
     */
    fun getObjectField(instance: Any?, fieldName: String): Any? {
        return try {
            if (instance != null) {
                XposedHelpers.getObjectField(instance, fieldName)
            } else null
        } catch (e: Throwable) {
            HLog.e(TAG, "Failed to get field $fieldName: ${e.message}")
            null
        }
    }
    
    /**
     * Safely sets an object field on an instance.
     */
    fun setObjectField(instance: Any?, fieldName: String, value: Any?) {
        try {
            if (instance != null) {
                XposedHelpers.setObjectField(instance, fieldName, value)
            }
        } catch (e: Throwable) {
            HLog.e(TAG, "Failed to set field $fieldName: ${e.message}")
        }
    }
}