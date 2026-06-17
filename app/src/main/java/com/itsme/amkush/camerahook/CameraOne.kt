package com.itsme.amkush.camerahook

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.net.Uri
import android.view.Surface
import com.itsme.amkush.MainHook
import com.itsme.amkush.config.Config
import com.itsme.amkush.utils.ImageToNV21
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

object CameraOne {
    
    private var origin_preview_camera: Camera? = null
    private var fake_SurfaceTexture: SurfaceTexture? = null
    private var original_c1_preview_SurfaceTexture: SurfaceTexture? = null
    private var original_preview_Surface: Surface? = null
    
    // 🛡️ FIXED: Cache fields now properly wired up
    private var cachedImageNV21: ByteArray? = null
    private var cachedImageWidth: Int = 0
    private var cachedImageHeight: Int = 0
    private var lastImageUri: String? = null
    private var lastPreviewWidth: Int = 0
    private var lastPreviewHeight: Int = 0
    
    fun initHooks(lpparam: XC_LoadPackage.LoadPackageParam) {
        hookSetPreviewTexture(lpparam)
        hookStartPreview(lpparam)
        hookSetPreviewCallback(lpparam)
    }
    
    private fun hookSetPreviewTexture(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                "android.hardware.Camera", lpparam.classLoader, "setPreviewTexture",
                SurfaceTexture::class.java, object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (param.args[0] == null) return
                        if (param.args[0] == fake_SurfaceTexture) return
                        if (origin_preview_camera != null && origin_preview_camera == param.thisObject) {
                            param.args[0] = fake_SurfaceTexture
                            return
                        }
                        origin_preview_camera = param.thisObject as Camera
                        original_c1_preview_SurfaceTexture = param.args[0] as SurfaceTexture
                        fake_SurfaceTexture = if (fake_SurfaceTexture == null) {
                            SurfaceTexture(Config.FAKE_SURFACE_TEXTURE_ID)
                        } else {
                            fake_SurfaceTexture!!.release()
                            SurfaceTexture(Config.FAKE_SURFACE_TEXTURE_ID)
                        }
                        param.args[0] = fake_SurfaceTexture
                    }
                })
        } catch (e: Throwable) {
            cn.dianbobo.dbb.util.HLog.e("CameraOne", "hookSetPreviewTexture failed: ${e.message}")
        }
    }
    
    private fun hookSetPreviewCallback(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                "android.hardware.Camera", lpparam.classLoader, "setPreviewCallback",
                "android.hardware.Camera\$PreviewCallback", object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val camera = param.thisObject as? Camera ?: return
                        val callback = param.args[0] ?: return
                        
                        val proxyCallback = java.lang.reflect.Proxy.newProxyInstance(
                            callback.javaClass.classLoader,
                            arrayOf(Class.forName("android.hardware.Camera\$PreviewCallback"))
                        ) { _, method, args ->
                            if (method.name == "onPreviewFrame") {
                                val imageUri = MainHook.imageUri
                                if (imageUri != null) {
                                    val ctx = MainHook.context
                                    if (ctx != null) {
                                        try {
                                            val params = camera.parameters
                                            val actualWidth = params.previewSize.width
                                            val actualHeight = params.previewSize.height
                                            
                                            // ️ FIXED: Only re-decode if URI or size changed
                                            val needsDecode = imageUri != lastImageUri ||
                                                actualWidth != lastPreviewWidth ||
                                                actualHeight != lastPreviewHeight
                                            
                                            if (needsDecode) {
                                                val inputStream = ctx.contentResolver.openInputStream(Uri.parse(imageUri))
                                                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                                                inputStream?.close()
                                                
                                                if (originalBitmap != null) {
                                                    val scaledBitmap = Bitmap.createScaledBitmap(
                                                        originalBitmap, actualWidth, actualHeight, true
                                                    )
                                                    cachedImageNV21 = ImageToNV21.convertToNV21(scaledBitmap)
                                                    cachedImageWidth = actualWidth
                                                    cachedImageHeight = actualHeight
                                                    lastImageUri = imageUri
                                                    lastPreviewWidth = actualWidth
                                                    lastPreviewHeight = actualHeight
                                                    
                                                    scaledBitmap.recycle()
                                                    originalBitmap.recycle()
                                                }
                                            }
                                            
                                            // Use cached NV21 bytes
                                            cachedImageNV21?.let { nv21Bytes ->
                                                args[0] = nv21Bytes
                                            }
                                        } catch (e: Exception) {
                                            cn.dianbobo.dbb.util.HLog.e("CameraOne", "Image injection error: ${e.message}")
                                        }
                                    }
                                }
                            }
                            method.invoke(callback, *args)
                        }
                        param.args[0] = proxyCallback
                    }
                })
        } catch (e: Throwable) {
            cn.dianbobo.dbb.util.HLog.e("CameraOne", "hookSetPreviewCallback failed: ${e.message}")
        }
    }
    
    private fun hookStartPreview(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                "android.hardware.Camera", lpparam.classLoader, "startPreview",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam?) {
                        val videoStatus = MainHook.videoStatus
                        val ijkPlayer = MainHook.ijkMediaPlayer
                        
                        if (MainHook.imageUri == null) {
                            if (ijkPlayer == null || !ijkPlayer.isPlayable) {
                                if (videoStatus?.isLiveStreamingEnabled == true) MainHook.initRTMPStream()
                                else if (videoStatus?.isVideoEnable == true) MainHook.initIjkPlayer()
                            }
                            MainHook.TheOnlyPlayer = MainHook.ijkMediaPlayer
                        }
                        c1_camera_play()
                    }
                })
        } catch (e: Throwable) {
            cn.dianbobo.dbb.util.HLog.e("CameraOne", "hookStartPreview failed: ${e.message}")
        }
    }
    
    private fun c1_camera_play() {
        if (MainHook.imageUri == null) {
            if (original_c1_preview_SurfaceTexture != null) {
                val videoStatus = MainHook.videoStatus
                if (videoStatus?.isVideoEnable == true || videoStatus?.isLiveStreamingEnabled == true) {
                    original_preview_Surface = Surface(original_c1_preview_SurfaceTexture)
                    if (original_preview_Surface?.isValid == true) {
                        handleMediaPlayer(original_preview_Surface!!)
                    }
                }
            }
        }
    }
    
    private fun handleMediaPlayer(surface: Surface) {
        try {
            MainHook.initStatus()
            val videoStatus = MainHook.videoStatus
            videoStatus?.let { status ->
                val volume = if (status.isVideoEnable && status.volume) 1F else 0F
                MainHook.ijkMediaPlayer?.setVolume(volume, volume)
                if (status.isVideoEnable || status.isLiveStreamingEnabled) {
                    MainHook.ijkMediaPlayer?.setSurface(surface)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun reset() {
        origin_preview_camera = null
        original_c1_preview_SurfaceTexture = null
        original_preview_Surface = null
        fake_SurfaceTexture?.release()
        fake_SurfaceTexture = null
        // 🛡️ FIXED: Clear cache on reset
        cachedImageNV21 = null
        cachedImageWidth = 0
        cachedImageHeight = 0
        lastImageUri = null
        lastPreviewWidth = 0
        lastPreviewHeight = 0
    }
}