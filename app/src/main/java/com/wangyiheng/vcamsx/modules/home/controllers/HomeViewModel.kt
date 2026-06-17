package com.wangyiheng.vcamsx.modules.home.controllers

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.wangyiheng.vcamsx.config.Config
import com.wangyiheng.vcamsx.data.models.VideoStatues
import com.wangyiheng.vcamsx.interfaces.IInfoManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class HomeViewModel : ViewModel(), KoinComponent {
    
    private val infoManager: IInfoManager by inject()
    
    val isVideoEnabled = mutableStateOf(false)
    val isVolumeEnabled = mutableStateOf(false)
    val videoPlayer = mutableStateOf(1)
    val codecType = mutableStateOf(false)
    val isLiveStreamingEnabled = mutableStateOf(false)
    var liveURL = mutableStateOf(Config.DEFAULT_RTMP_URL)
    
    val isVideoDisplay = mutableStateOf(false)
    val isLiveStreamingDisplay = mutableStateOf(false)
    
    fun init() { loadState() }
    
    fun loadState() {
        infoManager.getVideoStatus()?.let { status ->
            isVideoEnabled.value = status.isVideoEnable
            isVolumeEnabled.value = status.volume
            videoPlayer.value = status.videoPlayer
            codecType.value = status.codecType
            isLiveStreamingEnabled.value = status.isLiveStreamingEnabled
            liveURL.value = status.liveURL
        }
    }
    
    fun saveState() {
        infoManager.removeVideoStatus()
        infoManager.saveVideoStatus(
            VideoStatues(
                isVideoEnable = isVideoEnabled.value,
                volume = isVolumeEnabled.value,
                videoPlayer = videoPlayer.value,
                codecType = codecType.value,
                isLiveStreamingEnabled = isLiveStreamingEnabled.value,
                liveURL = liveURL.value
            )
        )
    }
    
    fun toggleVideoDisplay() { isVideoDisplay.value = !isVideoDisplay.value }
    fun toggleLiveStreamDisplay() { isLiveStreamingDisplay.value = !isLiveStreamingDisplay.value }
}