package com.afilaxy.security

object SecurityUtils {
    fun sanitizeLog(message: String): String {
        return message.replace(Regex("[\\r\\n\\t]"), "_")
                     .replace(Regex("[<>\"'&]"), "")
                     .take(200)
    }
    
    fun isValidCoordinate(lat: Double, lon: Double): Boolean {
        return lat in -90.0..90.0 && lon in -180.0..180.0 && 
               !lat.isNaN() && !lon.isNaN() && 
               !lat.isInfinite() && !lon.isInfinite()
    }
    
    fun formatSafeCoordinates(lat: Double, lon: Double): String {
        return if (isValidCoordinate(lat, lon)) {
            "%.6f,%.6f".format(lat, lon)
        } else {
            "0.0,0.0"
        }
    }
}