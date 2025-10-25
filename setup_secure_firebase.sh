#!/bin/bash

# Afilaxy - Configuração Segura do Firebase
# Este script configura as credenciais Firebase de forma segura

set -e

echo "🔒 AFILAXY - CONFIGURAÇÃO SEGURA DO FIREBASE"
echo "============================================="

# Verificar se o template existe
if [ ! -f "app/google-services.json.template" ]; then
    echo "❌ Erro: Template google-services.json.template não encontrado!"
    exit 1
fi

# Verificar se já existe configuração real
if [ -f "app/google-services.json" ] && ! grep -q "YOUR_PROJECT_ID" "app/google-services.json"; then
    echo "⚠️  Configuração Firebase já existe."
    read -p "Deseja sobrescrever? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Operação cancelada."
        exit 0
    fi
fi

echo
echo "📝 Insira suas credenciais Firebase:"
echo "   (Encontre essas informações no Firebase Console)"
echo

# Coletar credenciais de forma segura
read -p "Project Number: " PROJECT_NUMBER
read -p "Project ID: " PROJECT_ID
read -p "Storage Bucket: " STORAGE_BUCKET
read -p "Mobile SDK App ID (produção): " MOBILE_SDK_APP_ID
read -p "Mobile SDK App ID (debug): " MOBILE_SDK_APP_ID_DEBUG
read -s -p "API Key: " API_KEY
echo

# Validar entradas básicas
if [ -z "$PROJECT_NUMBER" ] || [ -z "$PROJECT_ID" ] || [ -z "$API_KEY" ]; then
    echo "❌ Erro: Campos obrigatórios não preenchidos!"
    exit 1
fi

# Criar arquivo de configuração
echo
echo "🔧 Gerando configuração..."

cp "app/google-services.json.template" "app/google-services.json"

# Substituir placeholders
sed -i "s/{{PROJECT_NUMBER}}/$PROJECT_NUMBER/g" "app/google-services.json"
sed -i "s/{{PROJECT_ID}}/$PROJECT_ID/g" "app/google-services.json"
sed -i "s/{{STORAGE_BUCKET}}/$STORAGE_BUCKET/g" "app/google-services.json"
sed -i "s/{{MOBILE_SDK_APP_ID}}/$MOBILE_SDK_APP_ID/g" "app/google-services.json"
sed -i "s/{{MOBILE_SDK_APP_ID_DEBUG}}/$MOBILE_SDK_APP_ID_DEBUG/g" "app/google-services.json"
sed -i "s/{{API_KEY}}/$API_KEY/g" "app/google-services.json"

# Definir permissões seguras
chmod 600 "app/google-services.json"

echo "✅ Configuração Firebase criada com sucesso!"
echo
echo "🔒 IMPORTANTE:"
echo "   • O arquivo google-services.json está protegido pelo .gitignore"
echo "   • NUNCA commite este arquivo no Git"
echo "   • Mantenha suas credenciais em local seguro"
echo "   • Para CI/CD, use variáveis de ambiente"
echo
echo "🚀 Agora você pode compilar o projeto com:"
echo "   ./gradlew build"