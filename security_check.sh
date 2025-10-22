#!/bin/bash

# Afilaxy Security Check Script
# Validates security configurations and best practices

echo "🔒 Afilaxy Security Check"
echo "========================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

ISSUES_FOUND=0

# Check 1: Verify google-services.json is not in git
echo -n "Checking google-services.json exclusion... "
if [ -f "app/google-services.json" ] && git check-ignore app/google-services.json > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Protected${NC}"
elif [ ! -f "app/google-services.json" ]; then
    echo -e "${YELLOW}⚠ File not found (expected in production)${NC}"
else
    echo -e "${RED}✗ EXPOSED - Add to .gitignore${NC}"
    ISSUES_FOUND=$((ISSUES_FOUND + 1))
fi

# Check 2: Verify network security config exists
echo -n "Checking network security config... "
if [ -f "app/src/main/res/xml/network_security_config.xml" ]; then
    echo -e "${GREEN}✓ Present${NC}"
else
    echo -e "${RED}✗ Missing network security config${NC}"
    ISSUES_FOUND=$((ISSUES_FOUND + 1))
fi

# Check 3: Check for hardcoded secrets in code
echo -n "Scanning for hardcoded secrets... "
SECRET_PATTERNS=(
    "AIza[0-9A-Za-z\\-_]{35}"
    "sk_live_[0-9a-zA-Z]{24}"
    "sk_test_[0-9a-zA-Z]{24}"
    "password.*=.*[\"'][^\"']{8,}[\"']"
    "api_key.*=.*[\"'][^\"']{10,}[\"']"
)

SECRETS_FOUND=0
for pattern in "${SECRET_PATTERNS[@]}"; do
    if grep -r -E "$pattern" app/src/ --include="*.kt" --include="*.java" > /dev/null 2>&1; then
        SECRETS_FOUND=$((SECRETS_FOUND + 1))
    fi
done

if [ $SECRETS_FOUND -eq 0 ]; then
    echo -e "${GREEN}✓ No secrets found${NC}"
else
    echo -e "${RED}✗ Found $SECRETS_FOUND potential secrets${NC}"
    ISSUES_FOUND=$((ISSUES_FOUND + 1))
fi

# Check 4: Verify security classes exist
echo -n "Checking security classes... "
SECURITY_CLASSES=(
    "app/src/main/java/com/afilaxy/security/SecurityValidator.kt"
    "app/src/main/java/com/afilaxy/security/InputSanitizer.kt"
    "app/src/main/java/com/afilaxy/security/AuthGuard.kt"
    "app/src/main/java/com/afilaxy/security/SecureXmlParser.kt"
)

MISSING_CLASSES=0
for class_file in "${SECURITY_CLASSES[@]}"; do
    if [ ! -f "$class_file" ]; then
        MISSING_CLASSES=$((MISSING_CLASSES + 1))
    fi
done

if [ $MISSING_CLASSES -eq 0 ]; then
    echo -e "${GREEN}✓ All security classes present${NC}"
else
    echo -e "${RED}✗ Missing $MISSING_CLASSES security classes${NC}"
    ISSUES_FOUND=$((ISSUES_FOUND + 1))
fi

# Check 5: Verify AndroidManifest security settings
echo -n "Checking AndroidManifest security... "
if grep -q 'android:usesCleartextTraffic="false"' app/src/main/AndroidManifest.xml && \
   grep -q 'android:allowBackup="false"' app/src/main/AndroidManifest.xml; then
    echo -e "${GREEN}✓ Secure configuration${NC}"
else
    echo -e "${RED}✗ Insecure AndroidManifest settings${NC}"
    ISSUES_FOUND=$((ISSUES_FOUND + 1))
fi

# Check 6: Verify proguard/R8 configuration
echo -n "Checking code obfuscation... "
if [ -f "app/proguard-rules.pro" ] && grep -q "minifyEnabled true" app/build.gradle.kts; then
    echo -e "${GREEN}✓ Code obfuscation enabled${NC}"
else
    echo -e "${YELLOW}⚠ Code obfuscation not fully configured${NC}"
fi

# Summary
echo ""
echo "========================="
if [ $ISSUES_FOUND -eq 0 ]; then
    echo -e "${GREEN}🎉 Security check passed! No critical issues found.${NC}"
    exit 0
else
    echo -e "${RED}⚠️  Security check failed! Found $ISSUES_FOUND critical issues.${NC}"
    echo "Please fix the issues above before deploying to production."
    exit 1
fi