#!/bin/bash

# Script de validação de segurança do Afilaxy
set -e

echo "🔒 Validação de Segurança - Afilaxy"
echo "=================================="

# Verificar se credenciais estão protegidas
echo "📋 Verificando proteção de credenciais..."
if [ -f "app/google-services.json" ]; then
    echo "❌ ERRO: google-services.json encontrado! Use o template."
    echo "   Execute: rm app/google-services.json"
    echo "   Execute: ./setup_secure_config.sh"
    exit 1
else
    echo "✅ Credenciais protegidas"
fi

# Verificar se template existe
if [ ! -f "app/google-services.json.template" ]; then
    echo "❌ ERRO: Template não encontrado"
    exit 1
else
    echo "✅ Template de configuração presente"
fi

# Verificar .gitignore
echo "📋 Verificando .gitignore..."
if grep -q "google-services.json" .gitignore; then
    echo "✅ .gitignore protege credenciais"
else
    echo "❌ ERRO: .gitignore não protege credenciais"
    exit 1
fi

# Verificar componentes de segurança
echo "📋 Verificando componentes de segurança..."

SECURITY_FILES=(
    "app/src/main/java/com/afilaxy/security/SecureXmlUtils.kt"
    "app/src/main/java/com/afilaxy/security/InputSanitizer.kt"
    "app/src/main/java/com/afilaxy/security/AuthGuard.kt"
    "app/src/main/java/com/afilaxy/security/SecurityValidator.kt"
)

for file in "${SECURITY_FILES[@]}"; do
    if [ -f "$file" ]; then
        echo "✅ $file"
    else
        echo "❌ ERRO: $file não encontrado"
        exit 1
    fi
done

# Verificar se há logs inseguros
echo "📋 Verificando logs inseguros..."
UNSAFE_LOGS=$(grep -r "Log\.[dwie].*\${.*}" app/src/ || true)
if [ -n "$UNSAFE_LOGS" ]; then
    echo "⚠️  Possíveis logs inseguros encontrados:"
    echo "$UNSAFE_LOGS"
else
    echo "✅ Logs seguros"
fi

echo
echo "🎉 Validação de segurança concluída!"
echo "✅ Todas as verificações passaram"
echo
echo "📝 Próximos passos:"
echo "1. Execute: ./setup_secure_config.sh (se ainda não fez)"
echo "2. Configure suas credenciais Firebase"
echo "3. Execute o app normalmente"