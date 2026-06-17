package com.wangyiheng.vcamsx.utils

import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object AntiTamperManager {

    // 🔒 The IP is now fetched from the encrypted Secrets object at runtime.
    private val VPS_BASE_URL: String = Secrets.getBaseUrl()

    /**
     * Runs the tamper check. If modified, bans the device on the VPS, 
     * shows a popup, and forcefully kills the app.
     */
    fun checkAndEnforce(context: Context) {
        if (SignatureUtils.isAppTampered(context)) {
            
            // 1. Silently report the hacker's Device ID to your VPS in the background
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val deviceId = DeviceId.get(context)
                    val url = URL("$VPS_BASE_URL/ban_device")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.doOutput = true
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.connectTimeout = 5000
                    conn.readTimeout = 5000
                    
                    val jsonBody = JSONObject().apply {
                        put("device_id", deviceId)
                        put("reason", "app_tampered_repacked")
                        put("timestamp", System.currentTimeMillis())
                    }
                    
                    conn.outputStream.use { it.write(jsonBody.toString().toByteArray()) }
                    conn.responseCode // Trigger the request
                    conn.disconnect()
                } catch (e: Exception) {
                    // Ignore network errors, we still block them locally
                }
            }

            // 2. Show the Popup and Kill the App on the Main Thread
            Handler(Looper.getMainLooper()).post {
                try {
                    AlertDialog.Builder(context)
                        .setTitle("Security Alert")
                        .setMessage("Nice Try Hacker!")
                        .setCancelable(false) // Prevents pressing back to bypass
                        .setPositiveButton("Ok Ban Me") { dialog, _ ->
                            dialog.dismiss()
                            killApp()
                        }
                        .show()
                    
                    // Failsafe: If they somehow dismiss the dialog or it fails to render, 
                    // kill the app after 5 seconds anyway.
                    Handler(Looper.getMainLooper()).postDelayed({
                        killApp()
                    }, 5000)

                } catch (e: Exception) {
                    killApp()
                }
            }
        }
    }

    private fun killApp() {
        android.os.Process.killProcess(android.os.Process.myPid())
        System.exit(0)
    }
}