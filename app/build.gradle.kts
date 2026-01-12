import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}

repositories {
    google {
        content {
            includeGroupByRegex("com\\.android.*")
            includeGroupByRegex("com\\.google.*")
            includeGroupByRegex("androidx.*")
        }
    }
    mavenCentral()
    gradlePluginPortal()
}

android {
    namespace = "com.afilaxy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.afilaxy.app"
        minSdk = 23
        targetSdk = 35
        versionCode = 14
        versionName = "2.0.3"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Firebase configuration from environment variables
        buildConfigField("String", "FIREBASE_PROJECT_ID", "\"${System.getenv("FIREBASE_PROJECT_ID") ?: ""}\"")
        buildConfigField("String", "FIREBASE_APP_ID", "\"${System.getenv("FIREBASE_APP_ID") ?: ""}\"")
        buildConfigField("String", "FIREBASE_API_KEY", "\"${System.getenv("FIREBASE_API_KEY") ?: ""}\"")
        buildConfigField("String", "FIREBASE_STORAGE_BUCKET", "\"${System.getenv("FIREBASE_STORAGE_BUCKET") ?: ""}\"")
        
        // Maps API Key from .env file or environment variable
        val envFile = rootProject.file(".env")
        val mapsApiKey = if (envFile.exists()) {
            val props = Properties()
            props.load(FileInputStream(envFile))
            props.getProperty("MAPS_API_KEY") ?: System.getenv("MAPS_API_KEY") ?: "YOUR_MAPS_API_KEY_HERE"
        } else {
            System.getenv("MAPS_API_KEY") ?: "YOUR_MAPS_API_KEY_HERE"
        }
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    }

    signingConfigs {
        create("release") {
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = Properties()
                keystoreProperties.load(keystorePropertiesFile.inputStream())
                
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            versionNameSuffix = "-debug"
            isMinifyEnabled = false
            // Otimizações para debug
            renderscriptOptimLevel = 3
            // Reduz warnings de métodos ocultos
            buildConfigField("boolean", "SUPPRESS_HIDDEN_API_WARNINGS", "true")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
            // Suprime warnings em produção
            buildConfigField("boolean", "SUPPRESS_HIDDEN_API_WARNINGS", "true")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { 
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xjvm-default=all"
        )
    }
    
    kapt {
        correctErrorTypes = true
        useBuildCache = false
        generateStubs = true
        arguments {
            arg("dagger.hilt.shareTestComponents", "true")
        }
    }
    
    // Otimizações de performance
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Dependências do Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-functions-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    // Adicione outras dependências Firebase SEM versão

    // Compose Bill of Materials (BOM) - Gerencia todas as versões do Compose
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Bibliotecas Padrão do AndroidX e Ciclo de Vida (Lifecycle)
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.fragment:fragment-ktx:1.8.2")

    // Bibliotecas da Interface Gráfica do Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Suporte ao ViewModel no Compose (Corrige o erro "Unresolved reference: viewModel")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.1")
    
    // Accompanist Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    // Biblioteca do Google Generative AI (Gemini)
    implementation("com.google.ai.client.generativeai:generativeai:0.6.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Google Maps SDK
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.maps.android:maps-compose:4.3.3")

    // Networking seguro
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // Security libraries
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // Hilt Dependency Injection - Temporarily disabled
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    implementation("androidx.hilt:hilt-work:1.1.0")
    kapt("androidx.hilt:hilt-compiler:1.1.0")
    
    // Room Database - Temporarily disabled
    // implementation("androidx.room:room-runtime:2.6.1")
    // implementation("androidx.room:room-ktx:2.6.1")
    // kapt("androidx.room:room-compiler:2.6.1")
    
    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // Paging 3
    implementation("androidx.paging:paging-runtime:3.2.1")
    implementation("androidx.paging:paging-compose:3.2.1")
    
    // Room Paging - Temporarily disabled
    // implementation("androidx.room:room-paging:2.6.1")
    
    // Biometric Authentication
    implementation("androidx.biometric:biometric:1.1.0")
    
    // Image Compression
    implementation("id.zelory:compressor:3.0.1")
    
    // Analytics
    implementation("com.google.firebase:firebase-analytics-ktx")
    
    // Testing libraries
    testImplementation("org.mockito:mockito-core:5.1.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("com.google.dagger:hilt-android-testing:2.48")
    kaptTest("com.google.dagger:hilt-compiler:2.48")

    // Remover Crashlytics temporariamente para Alpha
    // implementation("com.google.firebase:firebase-crashlytics-ktx")
    // implementation("com.google.firebase:firebase-analytics-ktx")

    // Bibliotecas de Teste (Padrão)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
