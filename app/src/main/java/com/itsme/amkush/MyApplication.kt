package com.itsme.amkush

import android.app.Application
import com.itsme.amkush.data.di.appModule
import com.itsme.amkush.utils.CrashLogger
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        //  INITIALIZE GLOBAL CRASH LOGGER (UI App)
        CrashLogger.init(this)

        // Initialize Koin
        startKoin {
            androidContext(this@MyApplication)
            modules(appModule)
        }
    }
}