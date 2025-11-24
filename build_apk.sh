#!/bin/bash

# Script para gerar APK do Afilaxy
# Uso: ./build_apk.sh [debug|release]

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Função para log colorido
log() {
    echo -e "${BLUE}[AFILAXY BUILD]${NC} $1"
}

success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Verificar se estamos no diretório correto
if [ ! -f "app/build.gradle.kts" ] && [ ! -f "app/build.gradle" ]; then
    error "Execute este script na raiz do projeto Android (onde está o app/build.gradle)"
    exit 1
fi

# Determinar tipo de build
BUILD_TYPE=${1:-debug}
if [ "$BUILD_TYPE" != "debug" ] && [ "$BUILD_TYPE" != "release" ]; then
    error "Tipo de build inválido. Use: debug ou release"
    exit 1
fi

log "Iniciando build do APK - Tipo: $BUILD_TYPE"

# Limpar builds anteriores
log "Limpando builds anteriores..."
./gradlew clean

# Verificar configuração do Firebase
if [ ! -f "app/google-services.json" ]; then
    warning "Arquivo google-services.json não encontrado!"
    warning "Certifique-se de configurar o Firebase antes do build de release"
    
    if [ "$BUILD_TYPE" = "release" ]; then
        error "Build de release requer configuração do Firebase"
        exit 1
    fi
fi

# Build do APK
log "Gerando APK $BUILD_TYPE..."

if [ "$BUILD_TYPE" = "debug" ]; then
    ./gradlew assembleDebug
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
    OUTPUT_NAME="afilaxy-debug.apk"
else
    # Para release, verificar se existe keystore
    if [ ! -f "app/keystore.jks" ] && [ ! -f "app/release-key.jks" ] && [ ! -f "app/afilaxy-release.keystore" ] && [ ! -f "keystore.properties" ]; then
        warning "Keystore não encontrado. Gerando APK release não assinado..."
        ./gradlew assembleRelease
        APK_PATH="app/build/outputs/apk/release/app-release-unsigned.apk"
        OUTPUT_NAME="afilaxy-release-unsigned.apk"
    else
        ./gradlew assembleRelease
        APK_PATH="app/build/outputs/apk/release/app-release.apk"
        OUTPUT_NAME="afilaxy-release.apk"
    fi
fi

# Verificar se o APK foi gerado
if [ ! -f "$APK_PATH" ]; then
    error "Falha ao gerar APK. Verifique os logs acima."
    exit 1
fi

# Criar diretório de output se não existir
mkdir -p apk-output

# Copiar APK para diretório de output com nome amigável
cp "$APK_PATH" "apk-output/$OUTPUT_NAME"

# Informações do APK
APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
APK_FULL_PATH=$(realpath "apk-output/$OUTPUT_NAME")

success "APK gerado com sucesso!"
echo ""
echo "📱 Informações do APK:"
echo "   Arquivo: $OUTPUT_NAME"
echo "   Tamanho: $APK_SIZE"
echo "   Localização: $APK_FULL_PATH"
echo ""

if [ "$BUILD_TYPE" = "debug" ]; then
    echo "🔧 APK Debug:"
    echo "   - Permite instalação em qualquer dispositivo"
    echo "   - Inclui logs de debug"
    echo "   - Não otimizado para produção"
else
    echo "🚀 APK Release:"
    echo "   - Otimizado para produção"
    echo "   - Logs de debug removidos"
    if [[ "$OUTPUT_NAME" == *"unsigned"* ]]; then
        warning "APK não assinado - não pode ser publicado na Play Store"
    else
        echo "   - Assinado e pronto para distribuição"
    fi
fi

echo ""
echo "📋 Para instalar:"
echo "   1. Transfira o APK para o dispositivo Android"
echo "   2. Ative 'Fontes desconhecidas' nas configurações"
echo "   3. Toque no arquivo APK para instalar"
echo ""

# Gerar QR Code se qrencode estiver disponível
if command -v qrencode &> /dev/null; then
    log "Gerando QR Code para download..."
    echo "APK: $OUTPUT_NAME" | qrencode -t ANSI
fi

success "Build concluído! APK disponível em: apk-output/$OUTPUT_NAME"