package com.afilaxy.crash

import android.content.Context
import android.util.Log
import com.afilaxy.config.AppConfig
import com.afilaxy.security.InputSanitizer
import com.afilaxy.utils.ErrorHandler

object CrashReporter {
    
    fun init(context: Context) {
        ErrorHandler.safeOperation {
            if (AppConfig.ENABLE_CRASH_REPORTING) {
                Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
                    val sanitizedThreadName = InputSanitizer.sanitizeForLog(thread.name)
                    val sanitizedMessage = InputSanitizer.sanitizeForLog(exception.message)
                    Log.e("CrashReporter", "Uncaught exception in thread $sanitizedThreadName: $sanitizedMessage")
                }
            }
        }
    }
    
    fun logError(tag: String, message: String, throwable: Throwable? = null) {
        ErrorHandler.safeOperation {
            val sanitizedTag = InputSanitizer.sanitizeForLog(tag)
            val sanitizedMessage = InputSanitizer.sanitizeForLog(message)
            Log.e(sanitizedTag, sanitizedMessage, throwable)
        }
    }
}