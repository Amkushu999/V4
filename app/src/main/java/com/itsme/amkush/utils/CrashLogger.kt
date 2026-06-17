package com.itsme.amkush.utils

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.io.StringWriter
import java.net.HttpURLConnection
import java.net.URL

object CrashLogger {

    private const val TAG = "CrashLogger"

    /**
     * Call this ONCE when the app starts. It sets up the global safety net.
     */
    fun init(context: Context) {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            // 1. Log the crash locally so we can see it in Logcat
            Log.e(TAG, "FATAL CRASH DETECTED!", throwable)

            // 2. Send the crash to the server in a background thread
            Thread {
                try {
                    sendCrashReport(context, throwable)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to send crash report", e)
                }
                
                // 3. Sleep briefly to allow the network request to finish before the app dies
                Thread.sleep(3000)

                // 4. Pass the crash to the default handler so the app actually crashes/closes
                defaultHandler?.uncaughtException(thread, throwable)
            }.apply {
                isDaemon = true
                start()
            }
        }
    }

    private fun sendCrashReport(context: Context, throwable: Throwable) {
        try {
            val url = URL("${Secrets.getBaseUrl()}/report_crash")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            conn.connectTimeout = 5000
            conn.readTimeout = 5000

            // Get Device ID
            val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "Unknown"

            // Get App Version
            val appVersion = try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            } catch (e: Exception) {
                "Unknown"
            }

            // Extract Stack Trace
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            throwable.printStackTrace(pw)
            val stackTrace = sw.toString()

            // Build JSON Payload
            val jsonBody = JSONObject().apply {
                put("device_id", deviceId)
                put("app_version", appVersion)
                put("android_version", Build.VERSION.RELEASE)
                put("device_model", "${Build.MANUFACTURER} ${Build.MODEL}")
                put("target_app", context.packageName)
                put("stack_trace", stackTrace)
            }

            conn.outputStream.use { it.write(jsonBody.toString().toByteArray()) }
            
            // Trigger the request and read response (optional)
            val responseCode = conn.responseCode
            Log.d(TAG, "Crash report sent. Server response: $responseCode")
            conn.disconnect()

        } catch (e: Exception) {
            Log.e(TAG, "Error sending crash report", e)
        }
    }
}