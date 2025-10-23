#!/bin/bash

echo "🔥 Testando configuração Firebase..."

# Verificar se google-services.json existe
if [ -f "app/google-services.json" ]; then
    echo "✅ google-services.json encontrado"
    
    # Verificar se contém dados válidos
    if grep -q "project_id" app/google-services.json; then
        echo "✅ Arquivo contém project_id"
    else
        echo "❌ Arquivo não contém project_id válido"
        exit 1
    fi
else
    echo "❌ google-services.json não encontrado"
    exit 1
fi

# Tentar build apenas do módulo app sem testes
echo "🔨 Tentando build simplificado..."
./gradlew :app:assembleDebug --no-daemon --stacktrace

if [ $? -eq 0 ]; then
    echo "✅ Build Firebase bem-sucedido!"
else
    echo "❌ Build falhou - verifique os erros acima"
fi