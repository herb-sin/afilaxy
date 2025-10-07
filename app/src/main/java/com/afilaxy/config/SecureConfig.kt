package com.afilaxy.config

import android.content.Context
import java.util.Properties
import java.io.IOException

object SecureConfig {
    private var properties: Properties? = null
    
    fun init(context: Context) {
        if (properties == null) {
            properties = Properties()
            try {
                context.assets.open("config.properties").use { inputStream ->
                    properties?.load(inputStream)
                }
            } catch (e: IOException) {
                // Fallback para valores padrão em caso de erro
                properties?.setProperty("debug_mode", "false")
            }
        }
    }
    
    fun getProperty(key: String, defaultValue: String = ""): String {
        return properties?.getProperty(key, defaultValue) ?: defaultValue
    }
    
    fun isDebugMode(): Boolean {
        return getProperty("debug_mode", "false").toBoolean()
    }
}