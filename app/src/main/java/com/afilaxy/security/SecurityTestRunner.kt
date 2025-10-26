package com.afilaxy.security

import kotlinx.coroutines.runBlocking

object SecurityTestRunner {
    
    fun runAllSecurityTests(): String {
        return runBlocking {
            val results = PenetrationTestSuite.runSecurityTests()
            val report = PenetrationTestSuite.generateSecurityReport()
            
            // Log security test results
            SecureLogger.security("SECURITY_TESTS", "Completed ${results.size} tests")
            
            // Report any failures
            val failures = results.filter { !it.passed }
            if (failures.isNotEmpty()) {
                SecurityMonitor.reportSecurityEvent("SECURITY_TEST_FAILURES", "Found ${failures.size} vulnerabilities")
            }
            
            report
        }
    }
    
    fun validateSecurityImplementation(): Boolean {
        return try {
            // Test centralized validator
            val testInput = "test\$where"
            val result = CentralizedValidator.validateInput(testInput, CentralizedValidator.InputType.GENERAL)
            
            // Test security interceptor
            val interceptorResult = SecurityInterceptor.secureOperation("test_operation") { true }
            
            // Test authentication policy
            val authResult = AuthenticationPolicy.requireAuthentication("emergency_create")
            
            !result.isValid && interceptorResult != null && authResult
        } catch (e: Exception) {
            SecureLogger.e("SecurityTestRunner", "Security validation failed", e)
            false
        }
    }
}