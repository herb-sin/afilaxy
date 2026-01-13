#!/bin/bash

# Script para gerar os valores dos secrets para configurar no GitHub Actions
# Execute este script e copie os valores para GitHub Settings → Secrets

echo "=========================================="
echo "🔐 Gerador de Secrets para GitHub Actions"
echo "=========================================="
echo ""

# Cores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 1. GOOGLE_SERVICES_JSON
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "1️⃣  GOOGLE_SERVICES_JSON"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
if [ -f "app/google-services.json" ]; then
    echo -e "${GREEN}✅ Arquivo encontrado${NC}"
    echo ""
    echo "Valor para colar no GitHub:"
    echo "----------------------------"
    base64 -w 0 app/google-services.json
    echo ""
    echo ""
else
    echo -e "${RED}❌ Arquivo app/google-services.json não encontrado${NC}"
    echo "   Baixe do Firebase Console → Project Settings → General"
    echo ""
fi

# 2. KEYSTORE_BASE64
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "2️⃣  KEYSTORE_BASE64"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
KEYSTORE_FILE=""

# Procurar keystore
if [ -f "app/keystore.jks" ]; then
    KEYSTORE_FILE="app/keystore.jks"
elif [ -f "keystore.jks" ]; then
    KEYSTORE_FILE="keystore.jks"
elif [ -f "app/afilaxy.jks" ]; then
    KEYSTORE_FILE="app/afilaxy.jks"
fi

if [ -n "$KEYSTORE_FILE" ]; then
    echo -e "${GREEN}✅ Keystore encontrado: $KEYSTORE_FILE${NC}"
    echo ""
    echo "Valor para colar no GitHub:"
    echo "----------------------------"
    base64 -w 0 "$KEYSTORE_FILE"
    echo ""
    echo ""
else
    echo -e "${YELLOW}⚠️  Keystore não encontrado${NC}"
    echo ""
    echo "Para criar um novo keystore, execute:"
    echo "keytool -genkey -v -keystore app/keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias afilaxy"
    echo ""
fi

# 3. Credenciais do Keystore
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "3️⃣  Credenciais do Keystore"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
if [ -f "keystore.properties" ]; then
    echo -e "${GREEN}✅ keystore.properties encontrado${NC}"
    echo ""
    
    KEYSTORE_PASSWORD=$(grep "storePassword=" keystore.properties | cut -d'=' -f2)
    KEY_ALIAS=$(grep "keyAlias=" keystore.properties | cut -d'=' -f2)
    KEY_PASSWORD=$(grep "keyPassword=" keystore.properties | cut -d'=' -f2)
    
    echo "KEYSTORE_PASSWORD: $KEYSTORE_PASSWORD"
    echo "KEY_ALIAS: $KEY_ALIAS"
    echo "KEY_PASSWORD: $KEY_PASSWORD"
    echo ""
else
    echo -e "${YELLOW}⚠️  keystore.properties não encontrado${NC}"
    echo "   Você precisará fornecer esses valores manualmente:"
    echo "   - KEYSTORE_PASSWORD: senha do keystore"
    echo "   - KEY_ALIAS: alias da chave (ex: afilaxy)"
    echo "   - KEY_PASSWORD: senha da chave"
    echo ""
fi

# 4. Firebase Service Account
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "4️⃣  FIREBASE_SERVICE_ACCOUNT"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${YELLOW}⚠️  Deve ser obtido do Firebase Console${NC}"
echo ""
echo "Passos:"
echo "1. Firebase Console → Project Settings → Service Accounts"
echo "2. Clique em 'Generate new private key'"
echo "3. Copie TODO o conteúdo JSON e cole no secret"
echo ""

# 5. Resumo
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📋 Resumo - Secrets a configurar no GitHub"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Acesse: Settings → Secrets and variables → Actions"
echo ""
echo "Secrets necessários:"
echo "  ✓ GOOGLE_SERVICES_JSON (gerado acima)"
echo "  ✓ KEYSTORE_BASE64 (gerado acima)"
echo "  ✓ KEYSTORE_PASSWORD (veja acima ou keystore.properties)"
echo "  ✓ KEY_ALIAS (veja acima ou keystore.properties)"
echo "  ✓ KEY_PASSWORD (veja acima ou keystore.properties)"
echo "  ✓ FIREBASE_SERVICE_ACCOUNT (obtenha do Firebase Console)"
echo "  ○ PLAY_STORE_SERVICE_ACCOUNT (opcional, apenas para Play Store)"
echo ""
echo "=========================================="
echo "✅ Script concluído!"
echo "=========================================="
