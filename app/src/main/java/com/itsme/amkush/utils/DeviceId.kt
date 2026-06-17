package com.itsme.amkush.utils

import android.content.Context
import android.provider.Settings
import java.util.UUID

object DeviceId {
    fun get(ctx: Context): String {
        val id = Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ANDROID_ID)
        return if (!id.isNullOrEmpty()) id else UUID.randomUUID().toString()
    }
}