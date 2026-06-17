package com.itsme.amkush.utils

import android.content.Context

object ActivationPrefs {
    private const val PREF = "activation_prefs"
    private const val TOKEN = "activation_token"

    fun saveToken(ctx: Context, token: String) = ctx.getSharedPreferences(PREF, 0).edit().putString(TOKEN, token).apply()
    fun getToken(ctx: Context): String? = ctx.getSharedPreferences(PREF, 0).getString(TOKEN, null)
    fun clear(ctx: Context) = ctx.getSharedPreferences(PREF, 0).edit().remove(TOKEN).apply()
}