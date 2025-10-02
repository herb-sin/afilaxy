package com.afilaxy.crash

import android.content.Context
import android.util.Log
import com.afilaxy.config.AppConfig

object CrashReporter {
    
    fun init(context: Context) {
        if (AppConfig.ENABLE_CRASH_REPORTING) {
            Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
                Log.e("CrashReporter", "Uncaught exception in thread ${thread.name}", exception)
            }
        }
    }
    
    fun logError(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
    }
}