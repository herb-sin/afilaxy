#!/bin/bash

echo "🔒 BUILD SEGURO - AFILAXY"
echo "========================"

# Carregar .env se existir
if [ -f ".env" ]; then
    echo "📁 Carregando configurações do .env..."
    export $(cat .env | xargs)
fi

# Verificar se API keys estão disponíveis
if [ -z "$GEMINI_API_KEY" ] || [ -z "$MAPS_API_KEY" ]; then
    echo "❌ ERRO: API keys não encontradas"
    echo "Opção 1: Crie arquivo .env com suas keys"
    echo "Opção 2: Configure: export GEMINI_API_KEY=sua_key"
    echo "Opção 3: Configure: export MAPS_API_KEY=sua_key"
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

# Verificar se build não contém credenciais reais
echo "🔍 Verificando segurança do build..."
if grep -r "AIzaSy[A-Za-z0-9_-]\{35\}" app/build/outputs/ 2>/dev/null | grep -v "SUA_NOVA"; then
    echo "❌ ALERTA: API key real detectada no build!"
    exit 1
else
    echo "✅ Build seguro - sem credenciais reais expostas"
fi

echo "🎯 Build concluído com segurança!"
echo "📁 Localização: app/build/outputs/bundle/release/"