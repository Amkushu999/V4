package com.itsme.amkush.utils

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import org.json.JSONObject
import java.io.PrintWriter
import java.io.StringWriter
import java.net.HttpURLConnection
import java.net.URL

object CrashLogger {

    private const val TAG = "CrashLogger"

    /**
     * Call this ONCE when the app starts. Sets up the global uncaught-exception handler.
     */
    fun init(context: Context) {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "FATAL CRASH DETECTED!", throwable)

            // FIX #21: Use a non-daemon thread so the JVM cannot kill it before the
            // crash report is delivered. Previously isDaemon = true meant the JVM was
            // free to terminate the thread (and drop the report) the moment all user
            // threads exited — which happens almost immediately after a crash.
            Thread {
                try {
                    sendCrashReport(context, throwable)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to send crash report", e)
                }

                // Brief sleep to let the network request complete before the process dies
                try { Thread.sleep(3000) } catch (_: InterruptedException) {}

                defaultHandler?.uncaughtException(thread, throwable)
            }.apply {
                isDaemon = false
                start()
            }
        }
    }

    private fun sendCrashReport(context: Context, throwable: Throwable) {
        val url = URL("${Secrets.getBaseUrl()}/report_crash")
        val conn = url.openConnection() as HttpURLConnection
        // FIX #22: Wrap in try-finally to guarantee conn.disconnect() is always called.
        // Previously disconnect() was only on the happy path; a network error after
        // openConnection() would leak the connection handle.
        try {
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            conn.connectTimeout = 5000
            conn.readTimeout = 5000

            val deviceId = Settings.Secure.getString(
                context.contentResolver, Settings.Secure.ANDROID_ID
            ) ?: "Unknown"

            val appVersion = try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            } catch (e: Exception) {
                "Unknown"
            }

            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            val stackTrace = sw.toString()

            val jsonBody = JSONObject().apply {
                put("device_id", deviceId)
                put("app_version", appVersion)
                put("android_version", Build.VERSION.RELEASE)
                put("device_model", "${Build.MANUFACTURER} ${Build.MODEL}")
                put("target_app", context.packageName)
                put("stack_trace", stackTrace)
            }

            conn.outputStream.use { it.write(jsonBody.toString().toByteArray()) }

            val responseCode = conn.responseCode
            Log.d(TAG, "Crash report sent. Server response: $responseCode")
        } finally {
            conn.disconnect()
        }
    }
}
