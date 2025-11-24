#!/bin/bash

# Script para gerar APK a partir do AAB (Android App Bundle)
# Uso: ./build_aab_apk.sh

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Versão do Bundletool
BUNDLETOOL_VERSION="1.15.6"
BUNDLETOOL_JAR="bundletool-all-${BUNDLETOOL_VERSION}.jar"
BUNDLETOOL_URL="https://github.com/google/bundletool/releases/download/${BUNDLETOOL_VERSION}/${BUNDLETOOL_JAR}"

log() {
    echo -e "${BLUE}[AFILAXY AAB]${NC} $1"
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

# Verificar diretório
if [ ! -f "app/build.gradle.kts" ] && [ ! -f "app/build.gradle" ]; then
    error "Execute este script na raiz do projeto Android"
    exit 1
fi

# 1. Verificar/Baixar Bundletool
if [ ! -f "$BUNDLETOOL_JAR" ]; then
    log "Bundletool não encontrado. Baixando versão ${BUNDLETOOL_VERSION}..."
    if command -v wget &> /dev/null; then
        wget -q "$BUNDLETOOL_URL" -O "$BUNDLETOOL_JAR"
    elif command -v curl &> /dev/null; then
        curl -L "$BUNDLETOOL_URL" -o "$BUNDLETOOL_JAR"
    else
        error "Nem wget nem curl encontrados. Por favor instale um deles ou baixe o bundletool manualmente."
        exit 1
    fi
    
    if [ ! -f "$BUNDLETOOL_JAR" ]; then
        error "Falha ao baixar bundletool."
        exit 1
    fi
    success "Bundletool baixado."
else
    log "Usando bundletool existente: $BUNDLETOOL_JAR"
fi

# 2. Gerar AAB (Release)
log "Gerando Android App Bundle (AAB)..."
./gradlew bundleRelease

AAB_PATH="app/build/outputs/bundle/release/app-release.aab"
if [ ! -f "$AAB_PATH" ]; then
    error "Falha ao gerar AAB em $AAB_PATH"
    exit 1
fi
success "AAB gerado com sucesso."

# 3. Gerar APK Universal a partir do AAB
log "Gerando APK Universal a partir do AAB..."

# Verificar Keystore para assinatura (necessário para bundletool gerar APKs instaláveis corretamente)
KEYSTORE_PATH="app/keystore.jks"
KEY_ALIAS="key0"
# Senhas padrão ou solicitar? 
# Para simplificar, vamos tentar usar as propriedades se existirem, ou assumir valores padrão/perguntar.
# Mas o bundletool precisa das senhas na linha de comando ou arquivo.
# Se não tiver keystore, geramos unsigned ou debug?
# O ideal para testar o AAB de release é usar a mesma chave de release.

if [ -f "keystore.properties" ]; then
    # Tentar ler do keystore.properties (formato simples)
    # Cuidado: parsing shell de properties pode ser frágil
    source <(grep = keystore.properties | sed 's/ *= */=/g')
fi

# Fallback ou verificação
if [ -z "$storePassword" ] || [ -z "$keyPassword" ] || [ -z "$keyAlias" ] || [ -z "$storeFile" ]; then
    warning "Configurações de keystore não encontradas ou incompletas em keystore.properties."
    warning "Tentando usar valores padrão ou debug keystore..."
    
    # Se não tivermos as credenciais de release, podemos tentar gerar um APK debug a partir do bundle?
    # Bundletool requer assinatura para build-apks se quisermos instalar.
    # Podemos usar --mode=universal sem assinar? Não, ele gera unsigned.
    
    log "Gerando APK Universal NÃO ASSINADO (para assinar depois ou testar em emuladores permissivos)..."
    java -jar "$BUNDLETOOL_JAR" build-apks \
        --bundle="$AAB_PATH" \
        --output="app/build/outputs/apk/release/app-release-universal.apks" \
        --mode=universal
        
    # Extrair o APK do arquivo .apks (que é um zip)
    unzip -o "app/build/outputs/apk/release/app-release-universal.apks" -d "app/build/outputs/apk/release/universal_extract"
    mv "app/build/outputs/apk/release/universal_extract/universal.apk" "app/build/outputs/apk/release/afilaxy-universal-unsigned.apk"
    OUTPUT_NAME="afilaxy-universal-unsigned.apk"
    
else
    # Caminho absoluto para keystore se for relativo
    if [[ "$storeFile" != /* ]]; then
        storeFile="$(pwd)/$storeFile"
    fi

    log "Usando keystore: $storeFile"
    
    java -jar "$BUNDLETOOL_JAR" build-apks \
        --bundle="$AAB_PATH" \
        --output="app/build/outputs/apk/release/app-release-universal.apks" \
        --ks="$storeFile" \
        --ks-pass="pass:$storePassword" \
        --ks-key-alias="$keyAlias" \
        --key-pass="pass:$keyPassword" \
        --mode=universal
        
    unzip -o "app/build/outputs/apk/release/app-release-universal.apks" -d "app/build/outputs/apk/release/universal_extract"
    mv "app/build/outputs/apk/release/universal_extract/universal.apk" "app/build/outputs/apk/release/afilaxy-universal.apk"
    OUTPUT_NAME="afilaxy-universal.apk"
fi

# 4. Mover para output
mkdir -p apk-output
cp "app/build/outputs/apk/release/$OUTPUT_NAME" "apk-output/$OUTPUT_NAME"

APK_FULL_PATH=$(realpath "apk-output/$OUTPUT_NAME")
APK_SIZE=$(du -h "$APK_FULL_PATH" | cut -f1)

echo ""
success "Processo concluído!"
echo "📦 AAB: $AAB_PATH"
echo "📱 APK Universal: $OUTPUT_NAME"
echo "   Tamanho: $APK_SIZE"
echo "   Local: $APK_FULL_PATH"
echo ""
echo "Nota: Este APK 'universal' contém recursos para todas as configurações de dispositivo,"
echo "      sendo maior que o download real da Play Store (que entrega apenas o necessário)."
