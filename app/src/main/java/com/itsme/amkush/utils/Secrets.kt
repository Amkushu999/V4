package com.itsme.amkush.utils

/**
 * Centralized storage for sensitive strings (API URLs, Secrets).
 * Uses an ASCII shift cipher to prevent trivial string extraction from the APK.
 */
object Secrets {

    // FIX #19: Removed the comment that printed the plaintext URL directly next to the
    // cipher data, completely defeating the purpose of obfuscation. The encrypted bytes
    // are the only authoritative source; the plaintext must not appear in source or APK.
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
