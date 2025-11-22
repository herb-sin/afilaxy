package com.afilaxy

import android.app.Application
import com.afilaxy.performance.AnrOptimizer
import com.afilaxy.performance.LogOptimizer
import com.afilaxy.performance.MapsPerformanceOptimizer
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltAndroidApp
class AfilaxyApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Suprimir warnings de métodos ocultos se configurado
        if (BuildConfig.SUPPRESS_HIDDEN_API_WARNINGS) {
            suppressHiddenApiWarnings()
        }
        
        // Initialize heavy operations in background to prevent ANR
        AnrOptimizer.executeAsync {
            initializeServices()
        }
    }
    
    private suspend fun initializeServices() {
        withContext(Dispatchers.IO) {
            try {
                // Inicializar Firebase
                val app = FirebaseApp.initializeApp(this@AfilaxyApplication)
                if (app != null) {
                    LogOptimizer.d("AfilaxyApp", "Firebase inicializado com sucesso")
                } else {
                    LogOptimizer.d("AfilaxyApp", "Firebase já estava inicializado")
                }
                
                // Inicializar Maps SDK de forma otimizada
                MapsPerformanceOptimizer.initializeMaps(this@AfilaxyApplication)
                
            } catch (e: Exception) {
                LogOptimizer.e("AfilaxyApp", "Erro ao inicializar serviços: ${e.message}", e)
            }
        }
    }
    
    /**
     * Suprime warnings de métodos ocultos usando reflexão segura
     */
    private fun suppressHiddenApiWarnings() {
        try {
            val vmRuntimeClass = Class.forName("dalvik.system.VMRuntime")
            val getRuntime = vmRuntimeClass.getMethod("getRuntime")
            val runtime = getRuntime.invoke(null)
            
            val setHiddenApiExemptions = vmRuntimeClass.getMethod(
                "setHiddenApiExemptions", 
                Array<String>::class.java
            )
            
            // Exemptions para reduzir warnings específicos
            val exemptions = arrayOf(
                "Lsun/misc/Unsafe;",
                "Ljava/lang/invoke/",
                "Llibcore/io/Memory;",
                "Ldalvik/system/CloseGuard;"
            )
            
            setHiddenApiExemptions.invoke(runtime, exemptions)
            LogOptimizer.d("AfilaxyApp", "Hidden API warnings suprimidos")
            
        } catch (e: Exception) {
            // Falha silenciosa - não é crítico
            LogOptimizer.d("AfilaxyApp", "Não foi possível suprimir warnings: ${e.message}")
        }
    }
}