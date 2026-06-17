package com.wangyiheng.vcamsx.camerahook

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CaptureRequest
import android.net.Uri
import android.os.Handler
import android.view.Surface
import com.wangyiheng.vcamsx.MainHook
import com.wangyiheng.vcamsx.utils.MediaTransformState
import com.wangyiheng.vcamsx.utils.PlaybackState
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

object CameraTwo {
    
    private var c2_builder: CaptureRequest.Builder? = null
    private var original_preview_Surface: Surface? = null
    private var c2_virtual_surface: Surface? = null
    private var c2_state_callback: CameraDevice.StateCallback? = null
    
    fun initHooks(lpparam: XC_LoadPackage.LoadPackageParam) {
        hookCameraManager(lpparam)
    }
    
    private fun hookCameraManager(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                "android.hardware.camera2.CameraManager", lpparam.classLoader, "openCamera",
                String::class.java, CameraDevice.StateCallback::class.java, Handler::class.java, 
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        try {
                            if (param.args[1] == null) return
                            if (param.args[1] == c2_state_callback) return
                            
                            c2_state_callback = param.args[1] as CameraDevice.StateCallback
                            val c2_state_callback_class = param.args[1]?.javaClass
                            process_camera2_init(c2_state_callback_class as Class<Any>?, lpparam)
                        } catch (e: Exception) {
                            cn.dianbobo.dbb.util.HLog.e("CameraTwo", "openCamera error: $e")
                        }
                    }
                })
        } catch (e: Throwable) {
            cn.dianbobo.dbb.util.HLog.e("CameraTwo", "hookCameraManager failed: ${e.message}")
        }
    }
    
    private fun process_camera2_init(c2StateCallbackClass: Class<Any>?, lpparam: XC_LoadPackage.LoadPackageParam) {
        if (c2StateCallbackClass == null) return
        try {
            XposedHelpers.findAndHookMethod(c2StateCallbackClass, "onOpened", CameraDevice::class.java, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) { original_preview_Surface = null }
            })
            
            XposedHelpers.findAndHookMethod("android.hardware.camera2.CaptureRequest.Builder", lpparam.classLoader, "addTarget", Surface::class.java, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (param.args[0] != null) {
                        if (param.args[0] == c2_virtual_surface) return
                        val surfaceInfo = param.args[0].toString()
                        if (!surfaceInfo.contains("Surface(name=null)")) {
                            if (original_preview_Surface != param.args[0] as Surface) {
                                original_preview_Surface = param.args[0] as Surface
                            }
                        }
                    }
                }
            })
            
            XposedHelpers.findAndHookMethod("android.hardware.camera2.CaptureRequest.Builder", lpparam.classLoader, "build", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (param.thisObject != null && param.thisObject != c2_builder) {
                        c2_builder = param.thisObject as CaptureRequest.Builder
                        process_camera_play()
                    }
                }
            })
            
            XposedHelpers.findAndHookMethod(c2StateCallbackClass, "onDisconnected", CameraDevice::class.java, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) { original_preview_Surface = null }
            })
        } catch (e: Throwable) {
            cn.dianbobo.dbb.util.HLog.e("CameraTwo", "process_camera2_init failed: ${e.message}")
        }
    }
    
    private fun process_camera_play() {
        val surface = original_preview_Surface ?: return
        
        if (MainHook.imageUri != null) {
            injectStaticImageWithTransform(surface)
            return
        }
        
        val videoStatus = MainHook.videoStatus
        val ijkPlayer = MainHook.ijkMediaPlayer
        
        if (ijkPlayer == null || !ijkPlayer.isPlayable) {
            if (videoStatus?.isLiveStreamingEnabled == true) MainHook.initRTMPStream()
            else if (videoStatus?.isVideoEnable == true) MainHook.initIjkPlayer()
        }
        
        MainHook.TheOnlyPlayer = MainHook.ijkMediaPlayer
        
        MainHook.ijkMediaPlayer?.let { player ->
            val volume = if (videoStatus?.isVideoEnable == true && videoStatus.volume) 1F else 0F
            player.setVolume(volume, volume)
            if (videoStatus?.isVideoEnable == true || videoStatus?.isLiveStreamingEnabled == true) {
                player.setSurface(surface)
                applyPlaybackState()
            }
        }
    }

    private fun applyPlaybackState() {
        MainHook.ijkMediaPlayer?.let { player ->
            if (PlaybackState.isPlaying) {
                if (!player.isPlaying) player.start()
            } else {
                if (player.isPlaying) player.pause()
            }
        }
    }

    private fun injectStaticImageWithTransform(surface: Surface) {
        val imageUriStr = MainHook.imageUri ?: return
        val ctx = MainHook.context ?: return

        val bitmap = try {
            val inputStream = ctx.contentResolver.openInputStream(Uri.parse(imageUriStr))
            val bmp = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            bmp
        } catch (e: Exception) {
            cn.dianbobo.dbb.util.HLog.e("CameraTwo", "Image decode error: ${e.message}")
            null
        }

        if (bitmap == null) return

        var canvas: Canvas? = null
        try {
            canvas = surface.lockCanvas(null)
            if (canvas != null) {
                val matrix = Matrix()
                val scale = MediaTransformState.scale
                val offsetX = MediaTransformState.offsetX
                val offsetY = MediaTransformState.offsetY

                matrix.postScale(scale, scale)
                val centerX = canvas.width / 2f
                val centerY = canvas.height / 2f
                matrix.postTranslate(-centerX, -centerY)
                matrix.postTranslate(offsetX, offsetY)
                matrix.postTranslate(centerX, centerY)

                canvas.drawBitmap(bitmap, matrix, null)
            }
        } catch (e: Exception) {
            cn.dianbobo.dbb.util.HLog.e("CameraTwo", "Image injection error: ${e.message}")
        } finally {
            if (canvas != null) {
                surface.unlockCanvasAndPost(canvas)
            }
            bitmap.recycle()
        }
    }
    
    fun reset() {
        c2_builder = null
        original_preview_Surface = null
        c2_virtual_surface = null
        c2_state_callback = null
    }
}