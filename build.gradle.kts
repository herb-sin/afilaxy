buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        try {
            classpath("com.android.tools.build:gradle:8.13.0")
            classpath("com.google.gms:google-services:4.4.3")
            classpath("com.google.dagger:hilt-android-gradle-plugin:2.48")
        } catch (e: Exception) {
            println("Error loading dependencies: ${e.message}")
            throw e
        }
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin) apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}