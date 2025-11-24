#!/bin/bash

# Load properties
if [ -f "keystore.properties" ]; then
    export $(grep -v '^#' keystore.properties | xargs)
else
    echo "❌ keystore.properties not found!"
    exit 1
fi

KEYSTORE_FILE=$storeFile
ALIAS=$keyAlias
STORE_PASS=$storePassword
KEY_PASS=$keyPassword

echo "🔐 Generating Release Keystore: $KEYSTORE_FILE"
echo "=========================================="

if [ -f "$KEYSTORE_FILE" ]; then
    echo "⚠️  Keystore already exists. Skipping generation."
else
    keytool -genkey -v -keystore "$KEYSTORE_FILE" \
        -alias "$ALIAS" \
        -keyalg RSA \
        -keysize 2048 \
        -validity 10000 \
        -storepass "$STORE_PASS" \
        -keypass "$KEY_PASS" \
        -dname "CN=Afilaxy, OU=Mobile, O=Afilaxy, L=Sao Paulo, ST=SP, C=BR"
    
    echo "✅ Keystore generated successfully!"
fi

echo ""
echo "🔍 SHA-1 Fingerprint (Release):"
keytool -list -v -keystore "$KEYSTORE_FILE" -alias "$ALIAS" -storepass "$STORE_PASS" | grep "SHA1"
