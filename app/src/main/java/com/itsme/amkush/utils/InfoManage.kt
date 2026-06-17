package com.itsme.amkush.utils

import android.content.Context
import com.crossbowffs.remotepreferences.RemotePreferences
import com.google.gson.Gson
import com.itsme.amkush.config.Config
import com.itsme.amkush.data.models.VideoStatues
import com.itsme.amkush.interfaces.IInfoManager

class InfoManager(private val context: Context) : IInfoManager {
    
    private val prefs = RemotePreferences(context, Config.PREFS_AUTHORITY, Config.PREFS_FILE_NAME)
    private val gson = Gson()
    
    override fun saveVideoStatus(videoStatus: VideoStatues) {
        val jsonString = gson.toJson(videoStatus)
        prefs.edit().putString("videoStatus", jsonString).apply()
    }
    
    override fun getVideoStatus(): VideoStatues? {
        val jsonString = prefs.getString("videoStatus", null)
        return if (jsonString != null) {
            try {
                gson.fromJson(jsonString, VideoStatues::class.java)
            } catch (e: Exception) {
                cn.dianbobo.dbb.util.HLog.e("InfoManager", "Failed to parse VideoStatus: ${e.message}")
                null
            }
        } else {
            null
        }
    }
    
    override fun removeVideoStatus() {
        prefs.edit().remove("videoStatus").apply()
    }
}