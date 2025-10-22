#!/bin/bash

# Script para configuração segura do Afilaxy
# Este script configura as credenciais do Firebase de forma segura

set -e

echo "🔒 Configuração Segura do Afilaxy"
echo "=================================="

# Verificar se o template existe
if [ ! -f "app/google-services.json.template" ]; then
    echo "❌ Erro: Template google-services.json.template não encontrado"
    exit 1
fi

# Verificar se já existe configuração
if [ -f "app/google-services.json" ]; then
    echo "⚠️  Arquivo google-services.json já existe"
    read -p "Deseja sobrescrever? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Operação cancelada"
        exit 0
    fi
fi

echo "📋 Configure suas credenciais do Firebase:"
echo

# Solicitar credenciais do usuário
read -p "Project Number: " FIREBASE_PROJECT_NUMBER
read -p "Project ID: " FIREBASE_PROJECT_ID
read -p "Storage Bucket: " FIREBASE_STORAGE_BUCKET
read -p "Mobile SDK App ID: " FIREBASE_MOBILE_SDK_APP_ID
read -p "Mobile SDK App ID (Debug): " FIREBASE_MOBILE_SDK_APP_ID_DEBUG
read -p "API Key: " FIREBASE_API_KEY

# Validar entradas básicas
if [ -z "$FIREBASE_PROJECT_NUMBER" ] || [ -z "$FIREBASE_PROJECT_ID" ] || [ -z "$FIREBASE_API_KEY" ]; then
    echo "❌ Erro: Campos obrigatórios não preenchidos"
    exit 1
fi

# Criar arquivo de configuração
echo "🔧 Criando arquivo de configuração..."

# Usar sed para substituir as variáveis no template
sed -e "s/\${FIREBASE_PROJECT_NUMBER}/$FIREBASE_PROJECT_NUMBER/g" \
    -e "s/\${FIREBASE_PROJECT_ID}/$FIREBASE_PROJECT_ID/g" \
    -e "s/\${FIREBASE_STORAGE_BUCKET}/$FIREBASE_STORAGE_BUCKET/g" \
    -e "s/\${FIREBASE_MOBILE_SDK_APP_ID}/$FIREBASE_MOBILE_SDK_APP_ID/g" \
    -e "s/\${FIREBASE_MOBILE_SDK_APP_ID_DEBUG}/$FIREBASE_MOBILE_SDK_APP_ID_DEBUG/g" \
    -e "s/\${FIREBASE_API_KEY}/$FIREBASE_API_KEY/g" \
    app/google-services.json.template > app/google-services.json

# Verificar se o arquivo foi criado
if [ -f "app/google-services.json" ]; then
    echo "✅ Arquivo google-services.json criado com sucesso"
    echo "🔒 Lembre-se: Este arquivo contém credenciais sensíveis e não deve ser commitado"
    echo "📝 O arquivo já está no .gitignore para sua segurança"
else
    echo "❌ Erro ao criar arquivo de configuração"
    exit 1
fi

echo
echo "🚀 Configuração concluída!"
echo "Agora você pode executar o projeto com suas credenciais do Firebase."