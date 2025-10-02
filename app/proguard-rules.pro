# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep line numbers for debugging stack traces.
-keepattributes LineNumberTable,SourceFile
-renamesourcefileattribute SourceFile

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Security classes - não ofuscar para auditoria
-keep class com.afilaxy.security.** { *; }

# Remover logs em produção
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Proteger contra reflection attacks
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Ofuscar strings sensíveis
-adaptclassstrings
-obfuscationdictionary dictionary.txt
-classobfuscationdictionary dictionary.txt
-packageobfuscationdictionary dictionary.txt