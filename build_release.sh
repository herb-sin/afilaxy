#!/bin/bash

# Script para gerar AAB de release do Afilaxy
# Uso: ./build_release.sh

set -e

echo "🚀 Iniciando build de release do Afilaxy..."
echo ""

# Cores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Verificar se keystore.properties existe
if [ ! -f "keystore.properties" ]; then
    echo -e "${RED}❌ Erro: keystore.properties não encontrado!${NC}"
    echo "Crie o arquivo keystore.properties na raiz do projeto com:"
    echo ""
    echo "storeFile=afilaxy-release.jks"
    echo "storePassword=SUA_SENHA"
    echo "keyAlias=afilaxy"
    echo "keyPassword=SUA_SENHA"
    exit 1
fi

# Verificar se .env existe
if [ ! -f ".env" ]; then
    echo -e "${YELLOW}⚠️  Aviso: .env não encontrado${NC}"
    echo "Certifique-se de que MAPS_API_KEY está configurada"
fi

# Limpar build anterior
echo -e "${YELLOW}🧹 Limpando build anterior...${NC}"
./gradlew clean

# Gerar AAB
echo -e "${YELLOW}📦 Gerando AAB de release...${NC}"
./gradlew bundleRelease

# Verificar se AAB foi gerado
AAB_PATH="app/build/outputs/bundle/release/app-release.aab"
if [ -f "$AAB_PATH" ]; then
    echo ""
    echo -e "${GREEN}✅ AAB gerado com sucesso!${NC}"
    echo ""
    echo "📁 Localização: $AAB_PATH"
    echo ""
    
    # Mostrar informações do arquivo
    SIZE=$(du -h "$AAB_PATH" | cut -f1)
    echo "📊 Tamanho: $SIZE"
    echo ""
    
    # Extrair versão do build.gradle.kts
    VERSION_CODE=$(grep "versionCode = " app/build.gradle.kts | sed 's/.*= //' | tr -d ' ')
    VERSION_NAME=$(grep "versionName = " app/build.gradle.kts | sed 's/.*= "//' | sed 's/"//')
    
    echo "🏷️  Versão: $VERSION_NAME (build $VERSION_CODE)"
    echo ""
    echo -e "${GREEN}🎉 Pronto para upload na Google Play Store!${NC}"
    echo ""
    echo "Próximos passos:"
    echo "1. Acesse: https://play.google.com/console"
    echo "2. Selecione o app Afilaxy"
    echo "3. Vá em Produção → Criar nova versão"
    echo "4. Faça upload do AAB"
    echo ""
else
    echo -e "${RED}❌ Erro: AAB não foi gerado${NC}"
    echo "Verifique os logs acima para detalhes do erro"
    exit 1
fi
