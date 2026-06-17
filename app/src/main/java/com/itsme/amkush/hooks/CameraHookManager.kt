package com.itsme.amkush.hooks

import android.app.Application
import android.content.Context
import com.crossbowffs.remotepreferences.RemotePreferences
import com.itsme.amkush.MainHook
import com.itsme.amkush.camerahook.CameraOne
import com.itsme.amkush.camerahook.CameraTwo
import com.itsme.amkush.config.Config
import com.itsme.amkush.hooks.bypass.AntiXposedDetectionHook
import com.itsme.amkush.hooks.bypass.AdvancedIdentityHook
import com.itsme.amkush.hooks.bypass.CustomDeviceSpooferHook
import com.itsme.amkush.hooks.bypass.EmulatorBypassHook
import com.itsme.amkush.hooks.bypass.HideAppHook
import com.itsme.amkush.hooks.bypass.RootBypassHook
import com.itsme.amkush.utils.CrashLogger
import com.itsme.amkush.utils.InfoManager
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

object CameraHookManager {
    
    private var context: Context? = null
    private var infoManager: InfoManager? = null
    
    fun initHooks(lpparam: XC_LoadPackage.LoadPackageParam) {
        RootBypassHook.init(lpparam)
        EmulatorBypassHook.init(lpparam)
        AntiXposedDetectionHook.init(lpparam)
    }
    
    private fun hookInstrumentation(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                "android.app.Instrumentation", lpparam.classLoader, "callApplicationOnCreate",
                Application::class.java, object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam?) {
                        if (param?.args?.get(0) is Application) {
                            val application = param.args[0] as Application
                            val applicationContext = application.applicationContext
                            if (context == applicationContext) return
                            
                            try {
                                context = applicationContext
                                infoManager = InfoManager(context!!)
                                CrashLogger.init(applicationContext)
                                HideAppHook.init(applicationContext, lpparam)
                                
                                val status = infoManager!!.getVideoStatus()
                                if (status?.targetPackageName != lpparam.packageName) {
                                    return
                                }
                                
                                cn.dianbobo.dbb.util.HLog.d("CameraHookManager", "Target matched! Hooking ${lpparam.packageName}")
                                
                                MainHook.context = context
                                MainHook.infoManager = infoManager
                                MainHook.initStatus()
                                
                                // 🛡️ FIXED: Use RemotePreferences to read image_uri from FaceGate's prefs
                                // because applicationContext here is the TARGET app's context, not FaceGate's
                                val remotePrefs = RemotePreferences(
                                    applicationContext,
                                    Config.PREFS_AUTHORITY,
                                    Config.PREFS_FILE_NAME
                                )
                                MainHook.imageUri = remotePrefs.getString("image_uri", null)
                                if (MainHook.imageUri != null) {
                                    cn.dianbobo.dbb.util.HLog.d("CameraHookManager", "Image injection enabled: ${MainHook.imageUri}")
                                }
                                
                                if (!lpparam.processName.contains(":")) {
                                    if (MainHook.ijkMediaPlayer == null) {
                                        val videoStatus = MainHook.videoStatus
                                        if (videoStatus?.isLiveStreamingEnabled == true) MainHook.initRTMPStream()
                                        else if (videoStatus?.isVideoEnable == true) MainHook.initIjkPlayer()
                                    }
                                }
                                
                                AdvancedIdentityHook.init(lpparam)
                                CustomDeviceSpooferHook.applySpoof(context!!, lpparam)
                                CameraOne.initHooks(lpparam)
                                CameraTwo.initHooks(lpparam)
                                
                            } catch (e: Exception) {
                                cn.dianbobo.dbb.util.HLog.e("CameraHookManager", "Instrumentation hook error: ${e.message}")
                            }
                        }
                    }
                })
        } catch (e: Throwable) {
            cn.dianbobo.dbb.util.HLog.e("CameraHookManager", "hookInstrumentation failed: ${e.message}")
        }
    }
    
    fun reset() {
        CameraOne.reset()
        CameraTwo.reset()
        context = null
        infoManager = null
    }
}