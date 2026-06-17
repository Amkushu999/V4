package com.itsme.amkush.data.di

import com.itsme.amkush.interfaces.IInfoManager
import com.itsme.amkush.interfaces.IVideoFileManager
import com.itsme.amkush.interfaces.IVideoPlayer
import com.itsme.amkush.modules.home.controllers.HomeViewModel
import com.itsme.amkush.utils.InfoManager
import com.itsme.amkush.utils.MediaPlayerController
import com.itsme.amkush.utils.VideoFileManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single<IInfoManager> { InfoManager(androidContext()) }
    single<IVideoFileManager> { VideoFileManager(androidContext()) }
    single<IVideoPlayer> { MediaPlayerController(androidContext()) }
    factory { HomeViewModel() }
}