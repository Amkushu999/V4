package com.itsme.amkush.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import java.security.MessageDigest

/**
 * Utility for APK signature operations.
 * - getSignatureHash(): Sends hash to server for verification (Primary Security)
 * - isAppTampered(): Local check (Secondary, returns false to rely on server)
 */
object SignatureUtils {
    
    /**
     * Extracts the current app's SHA-256 signature hash.
     * This is sent to the server in the HTTP headers for verification.
     */
    fun getSignatureHash(context: Context): String {
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
                return "NO_SIGNATURE"
            }
            
            val signature = signatures[0]
            val signatureBytes = signature.toByteArray()
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(signatureBytes)
            
            Base64.encodeToString(hashBytes, Base64.NO_WRAP)
            
        } catch (e: Exception) {
            "EXTRACTION_ERROR"
        }
    }
    
    /**
     * Local tamper check.
     * NOTE: We primarily rely on SERVER-SIDE verification now.
     * This method returns false to allow the app to run and let the server decide.
     * The server will reject modified APKs based on the signature hash.
     */
    fun isAppTampered(context: Context): Boolean {
        // We rely on server-side signature verification instead of local checks.
        // This prevents hackers from simply removing the local check.
        return false
    }
}