package com.itsme.amkush.utils

/**
 * Centralized storage for sensitive strings (API URLs, Secrets).
 * Uses an ASCII Shift Cipher to prevent hackers from finding plain text strings in the APK.
 */
object Secrets {
    
    // 🔒 ENCRYPTED SERVER URL: "http://142.93.247.175:5000" (Shifted by +7)
    private val ENCRYPTED_IP = intArrayOf(
        111, 123, 123, 119, 65, 54, 54, 56, 59, 57, 53, 64, 58, 53, 57, 59, 62, 53, 56, 62, 60, 65, 60, 55, 55, 55
    )

    /**
     * Decrypts and returns the Base URL at runtime.
     */
    fun getBaseUrl(): String {
        val chars = CharArray(ENCRYPTED_IP.size)
        for (i in ENCRYPTED_IP.indices) {
            chars[i] = (ENCRYPTED_IP[i] - 7).toChar()
        }
        return String(chars)
    }
}