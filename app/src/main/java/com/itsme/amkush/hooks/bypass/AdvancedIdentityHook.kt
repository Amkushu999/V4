package com.itsme.amkush.hooks.bypass

import com.itsme.amkush.utils.SafeHooker
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.util.Random

object AdvancedIdentityHook {

    private val audioRandom = Random()

    fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        spoofBuildSerial(lpparam)
        spoofAndroidId(lpparam)
        spoofCarrier(lpparam)
        spoofWifi(lpparam)
        spoofBattery(lpparam)
        spoofHardwareFeatures(lpparam)
        hideProcesses(lpparam)
        injectSensorJitter(lpparam)
        injectAmbientMicrophoneNoise(lpparam)
    }

    private fun spoofBuildSerial(lpparam: XC_LoadPackage.LoadPackageParam) {
        val fakeSerial = "R58M1234567W"
        SafeHooker.setStaticObjectField(lpparam, "android.os.Build", "SERIAL", fakeSerial)
        SafeHooker.hookMethod(lpparam, "android.os.Build", "getSerial", callback = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                param.result = fakeSerial
            }
        })
    }

    private fun spoofAndroidId(lpparam: XC_LoadPackage.LoadPackageParam) {
        SafeHooker.hookMethod(lpparam, "android.provider.Settings\$Secure", "getString", 
            "android.content.ContentResolver", String::class.java, 
            callback = object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val key = param.args[1] as? String ?: return
                    if (key == "android_id") {
                        param.result = "a1b2c3d4e5f6a7b8"
                    }
                }
            })
    }

    private fun spoofCarrier(lpparam: XC_LoadPackage.LoadPackageParam) {
        SafeHooker.hookMethod(lpparam, "android.telephony.TelephonyManager", "getSimOperatorName", callback = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                param.result = "Vodafone"
            }
        })
    }

    private fun spoofWifi(lpparam: XC_LoadPackage.LoadPackageParam) {
        SafeHooker.hookMethod(lpparam, "android.net.wifi.WifiInfo", "getSSID", callback = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val ssid = param.result as? String ?: return
                if (ssid.contains("AndroidWifi", ignoreCase = true) || ssid.contains("unknown", ignoreCase = true) || ssid == "\"\"" || ssid.isEmpty()) {
                    param.result = "\"Home_WiFi_5G\""
                }
            }
        })
    }

    private fun spoofBattery(lpparam: XC_LoadPackage.LoadPackageParam) {
        SafeHooker.hookMethod(lpparam, "android.os.BatteryManager", "getIntProperty", Int::class.javaPrimitiveType!!, callback = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val id = param.args[0] as Int
                if (id == 4) {
                    param.result = 78
                }
            }
        })
    }

    private fun spoofHardwareFeatures(lpparam: XC_LoadPackage.LoadPackageParam) {
        SafeHooker.hookMethod(lpparam, "android.app.ApplicationPackageManager", "hasSystemFeature", String::class.java, callback = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val feature = param.args[0] as? String ?: return
                if (feature == "android.hardware.camera.flash" ||
                    feature == "android.hardware.camera.autofocus" ||
                    feature == "android.hardware.telephony") {
                    param.result = true
                }
            }
        })
    }

    private fun hideProcesses(lpparam: XC_LoadPackage.LoadPackageParam) {
        SafeHooker.hookMethod(lpparam, "android.app.ActivityManager", "getRunningAppProcesses", callback = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val processes = param.result as? MutableList<*> ?: return
                val hiddenNames = listOf("magisk", "lsposed", "kashgate", "facegate", "supersu", "kernelsu")
                val iterator = processes.iterator()
                while (iterator.hasNext()) {
                    val process = iterator.next()
                    val name = XposedHelpers.getObjectField(process, "processName") as? String ?: continue
                    if (hiddenNames.any { name.contains(it, ignoreCase = true) }) {
                        iterator.remove()
                    }
                }
            }
        })
    }

    private fun injectSensorJitter(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            val sensorEventListenerClass = Class.forName("android.hardware.SensorEventListener")
            SafeHooker.hookMethod(lpparam, "android.hardware.SensorManager", "registerListener",
                sensorEventListenerClass, Class.forName("android.hardware.Sensor"), Int::class.javaPrimitiveType!!,
                callback = object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val originalListener = param.args[0]
                        val proxy = java.lang.reflect.Proxy.newProxyInstance(
                            sensorEventListenerClass.classLoader,
                            arrayOf(sensorEventListenerClass)
                        ) { _, method, args ->
                            if (method.name == "onSensorChanged" && args.isNotEmpty()) {
                                try {
                                    val event = args[0]
                                    val values = XposedHelpers.getObjectField(event, "values") as FloatArray
                                    for (i in values.indices) {
                                        values[i] += ((Math.random() * 0.02) - 0.01).toFloat()
                                    }
                                } catch (_: Exception) {}
                            }
                            method.invoke(originalListener, *args)
                        }
                        param.args[0] = proxy
                    }
                })
        } catch (_: Exception) {}
    }

    private fun injectAmbientMicrophoneNoise(lpparam: XC_LoadPackage.LoadPackageParam) {
        SafeHooker.hookMethod(lpparam, "android.media.AudioRecord", "read", ByteArray::class.java, Int::class.javaPrimitiveType!!, Int::class.javaPrimitiveType!!, callback = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val buffer = param.args[0] as? ByteArray ?: return
                val read = param.result as? Int ?: return
                if (read > 0) {
                    for (i in 0 until read) {
                        buffer[i] = (audioRandom.nextInt(11) - 5).toByte()
                    }
                }
            }
        })
        SafeHooker.hookMethod(lpparam, "android.media.AudioRecord", "read", ShortArray::class.java, Int::class.javaPrimitiveType!!, Int::class.javaPrimitiveType!!, callback = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val buffer = param.args[0] as? ShortArray ?: return
                val read = param.result as? Int ?: return
                if (read > 0) {
                    for (i in 0 until read) {
                        buffer[i] = (audioRandom.nextInt(201) - 100).toShort()
                    }
                }
            }
        })
    }
}