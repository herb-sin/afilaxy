#!/bin/bash

# Script para configuração segura do keystore
set -e

echo "🔐 Configuração Segura do Keystore - Afilaxy"
echo "============================================"

if [ -f "keystore.properties" ]; then
    echo "⚠️  Arquivo keystore.properties já existe"
    read -p "Deseja sobrescrever? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Operação cancelada"
        exit 0
    fi
fi

echo "📋 Configure as propriedades do keystore:"
echo

read -p "Caminho do keystore (ex: app/keystore.jks): " KEYSTORE_FILE
read -s -p "Senha do keystore: " KEYSTORE_PASSWORD
echo
read -p "Alias da chave (ex: afilaxy): " KEY_ALIAS
read -s -p "Senha da chave: " KEY_PASSWORD
echo

if [ -z "$KEYSTORE_FILE" ] || [ -z "$KEYSTORE_PASSWORD" ] || [ -z "$KEY_ALIAS" ] || [ -z "$KEY_PASSWORD" ]; then
    echo "❌ Erro: Todos os campos são obrigatórios"
    exit 1
fi

echo "🔧 Criando arquivo keystore.properties..."

sed -e "s/\${KEYSTORE_FILE}/$KEYSTORE_FILE/g" \
    -e "s/\${KEYSTORE_PASSWORD}/$KEYSTORE_PASSWORD/g" \
    -e "s/\${KEY_ALIAS}/$KEY_ALIAS/g" \
    -e "s/\${KEY_PASSWORD}/$KEY_PASSWORD/g" \
    keystore.properties.template > keystore.properties

if [ -f "keystore.properties" ]; then
    echo "✅ Arquivo keystore.properties criado com sucesso"
    echo "🔒 Arquivo protegido pelo .gitignore"
else
    echo "❌ Erro ao criar arquivo"
    exit 1
fi

echo
echo "🚀 Configuração concluída!"
echo "Agora você pode fazer build do app com assinatura."