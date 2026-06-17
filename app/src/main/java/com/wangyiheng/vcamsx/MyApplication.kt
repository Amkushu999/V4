package com.wangyiheng.vcamsx

import android.app.Application
import com.wangyiheng.vcamsx.data.di.appModule
import com.wangyiheng.vcamsx.utils.CrashLogger
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