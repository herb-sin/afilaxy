package com.afilaxy.performance

import android.util.Log
import com.afilaxy.BuildConfig

/**
 * Otimizador de logs para reduzir warnings de métodos ocultos
 * e melhorar performance em produção
 */
object LogOptimizer {
    
    private const val TAG = "Afilaxy"
    
    // Controle de logs baseado no build type
    private val isDebugBuild = BuildConfig.DEBUG
    
    /**
     * Log de debug - apenas em builds de debug
     */
    fun d(tag: String, message: String) {
        if (isDebugBuild) {
            Log.d(tag, message)
        }
    }
    
    /**
     * Log de informação - controlado
     */
    fun i(tag: String, message: String) {
        if (isDebugBuild || isImportantLog(message)) {
            Log.i(tag, message)
        }
    }
    
    /**
     * Log de warning - sempre ativo mas filtrado
     */
    fun w(tag: String, message: String) {
        if (!isHiddenMethodWarning(message)) {
            Log.w(tag, message)
        }
    }
    
    /**
     * Log de erro - sempre ativo
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }
    
    /**
     * Verifica se é um warning de método oculto que pode ser suprimido
     */
    private fun isHiddenMethodWarning(message: String): Boolean {
        return message.contains("Accessing hidden method") ||
               message.contains("Accessing hidden field") ||
               message.contains("greylist") ||
               message.contains("sun/misc/Unsafe")
    }
    
    /**
     * Verifica se é um log importante que deve aparecer mesmo em release
     */
    private fun isImportantLog(message: String): Boolean {
        return message.contains("Emergency") ||
               message.contains("Location") ||
               message.contains("Firebase") ||
               message.contains("Error") ||
               message.contains("Exception")
    }
}