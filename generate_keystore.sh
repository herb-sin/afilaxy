#!/bin/bash

echo "🔐 GERANDO KEYSTORE PARA AFILAXY"
echo "================================"

# Criar diretório se não existir
mkdir -p ~/.android

# Gerar debug keystore se não existir
if [ ! -f ~/.android/debug.keystore ]; then
    echo "📱 Gerando debug keystore..."
    keytool -genkey -v -keystore ~/.android/debug.keystore \
        -storepass android -alias androiddebugkey \
        -keypass android -keyalg RSA -keysize 2048 \
        -validity 10000 \
        -dname "CN=Android Debug,O=Android,C=US"
fi

# Extrair SHA-1
echo "🔍 SHA-1 Fingerprint para Google Console:"
keytool -list -v -keystore ~/.android/debug.keystore \
    -alias androiddebugkey -storepass android -keypass android | grep SHA1

echo ""
echo "📋 PRÓXIMOS PASSOS:"
echo "1. Copie o SHA-1 acima"
echo "2. Cole no Google Cloud Console > API Key Restrictions"
echo "3. Adicione package name: com.afilaxy"