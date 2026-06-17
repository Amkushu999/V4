package com.wangyiheng.vcamsx.data.di

import com.wangyiheng.vcamsx.interfaces.IInfoManager
import com.wangyiheng.vcamsx.interfaces.IVideoFileManager
import com.wangyiheng.vcamsx.interfaces.IVideoPlayer
import com.wangyiheng.vcamsx.modules.home.controllers.HomeViewModel
import com.wangyiheng.vcamsx.utils.InfoManager
import com.wangyiheng.vcamsx.utils.MediaPlayerController
import com.wangyiheng.vcamsx.utils.VideoFileManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single<IInfoManager> { InfoManager(androidContext()) }
    single<IVideoFileManager> { VideoFileManager(androidContext()) }
    single<IVideoPlayer> { MediaPlayerController(androidContext()) }
    factory { HomeViewModel() }
}