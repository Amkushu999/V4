package com.itsme.amkush.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

object ActivationApi {
    
    // 🔒 The IP is now fetched from the encrypted Secrets object at runtime.
    val BASE_URL: String = Secrets.getBaseUrl()

    /**
     * Get the current APK's SHA-256 signature hash
     */
    private fun getAppSignatureHash(context: Context): String {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
            }
            
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }
            
            if (signatures == null || signatures.isEmpty()) {
                return ""
            }
            
            val signature = signatures[0]
            val signatureBytes = signature.toByteArray()
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(signatureBytes)
            Base64.encodeToString(hashBytes, Base64.NO_WRAP)
            
        } catch (e: Exception) {
            ""
        }
    }

    private fun postJson(endpoint: String, body: String, context: Context): String {
        val url = URL("$BASE_URL$endpoint")
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 15000
        conn.readTimeout = 15000
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        
        //  Add APK signature hash to request header
        val signatureHash = getAppSignatureHash(context)
        conn.setRequestProperty("X-APK-Signature", signatureHash)
        
        conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
        val stream = if (conn.responseCode >= 400) conn.errorStream else conn.inputStream
        return BufferedReader(InputStreamReader(stream, "UTF-8")).use { it.readText() }
    }

    fun validateKey(context: Context, key: String, deviceId: String): JSONObject {
        return try {
            val body = JSONObject().put("key", key).put("device_id", deviceId).toString()
            JSONObject(postJson("/validate_key", body, context))
        } catch (e: Exception) {
            JSONObject().put("valid", false).put("error", e.message ?: "Network error")
        }
    }

    fun checkToken(context: Context, token: String?): Boolean {
        if (token == null) return false
        return try {
            val body = JSONObject().put("token", token).toString()
            JSONObject(postJson("/check_token", body, context)).optBoolean("valid", false)
        } catch (e: Exception) { 
            false 
        }
    }
}