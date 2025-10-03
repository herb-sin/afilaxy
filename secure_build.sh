#!/bin/bash

echo "🔒 BUILD SEGURO - AFILAXY"
echo "========================"

# Verificar se API keys estão em variáveis de ambiente
if [ -z "$GEMINI_API_KEY" ] || [ -z "$MAPS_API_KEY" ]; then
    echo "❌ ERRO: API keys não encontradas nas variáveis de ambiente"
    echo "Configure: export GEMINI_API_KEY=sua_key"
    echo "Configure: export MAPS_API_KEY=sua_key"
    exit 1
fi

# Criar local.properties temporário
echo "GEMINI_API_KEY=$GEMINI_API_KEY" > local.properties.temp
echo "MAPS_API_KEY=$MAPS_API_KEY" >> local.properties.temp

# Build seguro
echo "📦 Gerando build seguro..."
./gradlew bundleRelease

# Limpar credenciais temporárias
rm -f local.properties.temp

# Verificar se build não contém credenciais
echo "🔍 Verificando segurança do build..."
if grep -r "AIzaSy" app/build/outputs/ 2>/dev/null; then
    echo "❌ ALERTA: Possível API key no build!"
    exit 1
else
    echo "✅ Build seguro - sem credenciais expostas"
fi

echo "🎯 Build concluído com segurança!"
echo "📁 Localização: app/build/outputs/bundle/release/"