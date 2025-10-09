#!/bin/bash

# 🚀 Script de Setup CI/CD para Afilaxy
# Este script ajuda na configuração inicial da pipeline

echo "🫁 Afilaxy - Setup CI/CD"
echo "========================"

# Verificar se estamos no diretório correto
if [ ! -f "app/build.gradle.kts" ]; then
    echo "❌ Execute este script na raiz do projeto Afilaxy"
    exit 1
fi

echo "✅ Diretório correto detectado"

# Verificar dependências
echo "🔍 Verificando dependências..."

if ! command -v base64 &> /dev/null; then
    echo "❌ base64 não encontrado"
    exit 1
fi

if ! command -v keytool &> /dev/null; then
    echo "❌ keytool não encontrado (instale o JDK)"
    exit 1
fi

echo "✅ Dependências OK"

# Verificar arquivos necessários
echo "📁 Verificando arquivos..."

if [ ! -f "app/google-services.json" ]; then
    echo "⚠️  google-services.json não encontrado"
    echo "   Copie o arquivo do Firebase Console para app/"
fi

if [ ! -f "keystore.properties" ]; then
    echo "⚠️  keystore.properties não encontrado"
    echo "   Será necessário para builds de produção"
fi

# Gerar comandos para secrets
echo ""
echo "🔑 Comandos para gerar secrets do GitHub:"
echo "========================================"

if [ -f "app/google-services.json" ]; then
    echo "GOOGLE_SERVICES_JSON:"
    echo "base64 -w 0 app/google-services.json"
    echo ""
fi

if [ -f "app/keystore.jks" ]; then
    echo "KEYSTORE_BASE64:"
    echo "base64 -w 0 app/keystore.jks"
    echo ""
fi

# Verificar se o keystore existe
if [ ! -f "app/keystore.jks" ]; then
    echo "🔐 Keystore não encontrado. Deseja criar um? (y/n)"
    read -r create_keystore
    
    if [ "$create_keystore" = "y" ]; then
        echo "Criando keystore..."
        keytool -genkey -v -keystore app/keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias afilaxy
        
        if [ $? -eq 0 ]; then
            echo "✅ Keystore criado com sucesso!"
            echo "📝 Não esqueça de criar o arquivo keystore.properties"
        fi
    fi
fi

# Verificar workflows
echo "📋 Status dos workflows:"
echo "======================="

workflows=("ci.yml" "cd.yml" "release.yml" "security-check.yml")
for workflow in "${workflows[@]}"; do
    if [ -f ".github/workflows/$workflow" ]; then
        echo "✅ $workflow"
    else
        echo "❌ $workflow"
    fi
done

echo ""
echo "🎯 Próximos passos:"
echo "=================="
echo "1. Configure os secrets no GitHub (veja .github/SECRETS_SETUP.md)"
echo "2. Teste a pipeline fazendo um commit"
echo "3. Configure o Firebase App Distribution"
echo "4. Configure o Play Store Console (opcional)"

echo ""
echo "📚 Documentação completa: .github/SECRETS_SETUP.md"
echo "🚀 Pipeline pronta para uso!"