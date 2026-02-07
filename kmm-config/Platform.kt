package com.afilaxy.shared

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
