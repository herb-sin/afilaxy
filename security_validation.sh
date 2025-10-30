#!/bin/bash

# Security Validation Script for Afilaxy
# Validates that all security measures are properly implemented

echo "🔒 Afilaxy Security Validation"
echo "=============================="

# Check if security classes exist
echo "📋 Checking security classes..."

SECURITY_CLASSES=(
    "app/src/main/java/com/afilaxy/security/SecureXmlParser.kt"
    "app/src/main/java/com/afilaxy/security/SecureFileValidator.kt"
    "app/src/main/java/com/afilaxy/security/SecureCrypto.kt"
    "app/src/main/java/com/afilaxy/security/InputSanitizer.kt"
    "app/src/main/java/com/afilaxy/security/AuthGuard.kt"
    "app/src/main/java/com/afilaxy/security/SecureLogger.kt"
    "app/src/main/java/com/afilaxy/security/SecurityMonitor.kt"
    "app/src/main/java/com/afilaxy/security/CentralizedValidator.kt"
    "app/src/main/java/com/afilaxy/security/SecurityUtils.kt"
)

MISSING_CLASSES=0

for class in "${SECURITY_CLASSES[@]}"; do
    if [[ -f "$class" ]]; then
        echo "✅ $class"
    else
        echo "❌ $class - MISSING"
        ((MISSING_CLASSES++))
    fi
done

# Check for dangerous patterns in code
echo ""
echo "🔍 Scanning for security vulnerabilities..."

DANGEROUS_PATTERNS=(
    "eval("
    "javascript:"
    "document.write"
    "innerHTML ="
    "outerHTML ="
    "System.exec"
    "Runtime.exec"
    "ProcessBuilder"
    "\$where"
    "\$ne"
    "ObjectId("
)

VULNERABILITIES_FOUND=0

for pattern in "${DANGEROUS_PATTERNS[@]}"; do
    if grep -r "$pattern" app/src/main/java/ >/dev/null 2>&1; then
        echo "⚠️  Found potentially dangerous pattern: $pattern"
        ((VULNERABILITIES_FOUND++))
    fi
done

# Check for proper authentication checks
echo ""
echo "🔐 Validating authentication implementation..."

AUTH_FILES=(
    "app/src/main/java/com/afilaxy/presentation/emergency/EmergencyViewModel.kt"
    "app/src/main/java/com/afilaxy/presentation/login/LoginViewModel.kt"
    "app/src/main/java/com/afilaxy/MainActivity.kt"
)

AUTH_ISSUES=0

for file in "${AUTH_FILES[@]}"; do
    if [[ -f "$file" ]]; then
        if grep -q "AuthGuard.isUserAuthenticated()" "$file"; then
            echo "✅ Authentication check found in $file"
        else
            echo "⚠️  Missing authentication check in $file"
            ((AUTH_ISSUES++))
        fi
    fi
done

# Summary
echo ""
echo "📊 Security Validation Summary"
echo "=============================="
echo "Missing security classes: $MISSING_CLASSES"
echo "Potential vulnerabilities: $VULNERABILITIES_FOUND"
echo "Authentication issues: $AUTH_ISSUES"

TOTAL_ISSUES=$((MISSING_CLASSES + VULNERABILITIES_FOUND + AUTH_ISSUES))

if [[ $TOTAL_ISSUES -eq 0 ]]; then
    echo ""
    echo "🎉 Security validation PASSED! All checks completed successfully."
    exit 0
else
    echo ""
    echo "❌ Security validation FAILED! Found $TOTAL_ISSUES issues that need attention."
    exit 1
fi