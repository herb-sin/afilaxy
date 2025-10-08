package com.afilaxy.utils

import kotlinx.coroutines.delay
import kotlin.math.pow

object RetryUtils {
    suspend fun <T> retryWithBackoff(
        maxRetries: Int = 3,
        initialDelay: Long = 1000,
        maxDelay: Long = 10000,
        factor: Double = 2.0,
        action: suspend () -> T
    ): T {
        repeat(maxRetries) { attempt ->
            try {
                return action()
            } catch (e: Exception) {
                if (attempt == maxRetries - 1) throw e
                
                val delay = (initialDelay * factor.pow(attempt)).toLong().coerceAtMost(maxDelay)
                delay(delay)
            }
        }
        throw IllegalStateException("Should not reach here")
    }
}