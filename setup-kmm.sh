#!/bin/bash

# 🚀 Script de Setup Automático do Afilaxy KMM
# Execute este script para criar a estrutura completa do projeto

set -e  # Parar se houver erro

echo "🚀 Criando projeto Afilaxy KMM..."
echo ""

# Cores para output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 1. Criar diretórios
echo -e "${BLUE}📁 Passo 1: Criando estrutura de diretórios...${NC}"
cd /home/afilaxy/Projetos
mkdir -p afilaxy-kmm/shared/src/commonMain/kotlin/com/afilaxy/shared
mkdir -p afilaxy-kmm/shared/src/androidMain/kotlin/com/afilaxy/shared
mkdir -p afilaxy-kmm/shared/src/iosMain/kotlin/com/afilaxy/shared
mkdir -p afilaxy-kmm/androidApp/src/main/kotlin
mkdir -p afilaxy-kmm/androidApp/src/main/res
mkdir -p afilaxy-kmm/iosApp
mkdir -p afilaxy-kmm/gradle
echo -e "${GREEN}✅ Diretórios criados${NC}\n"

# 2. Copiar configurações
echo -e "${BLUE}📋 Passo 2: Copiando arquivos de configuração...${NC}"
cd "/home/afilaxy/Projetos/afilaxy Kotlin/afilaxy"
cp kmm-config/settings.gradle.kts ../../afilaxy-kmm/
cp kmm-config/build.gradle.kts ../../afilaxy-kmm/
cp kmm-config/shared-build.gradle.kts ../../afilaxy-kmm/shared/build.gradle.kts
cp kmm-config/androidApp-build.gradle.kts ../../afilaxy-kmm/androidApp/build.gradle.kts
cp kmm-config/libs.versions.toml ../../afilaxy-kmm/gradle/
echo -e "${GREEN}✅ Configurações copiadas${NC}\n"

# 3. Copiar Gradle wrapper
echo -e "${BLUE}⚙️  Passo 3: Copiando Gradle wrapper...${NC}"
cp -r gradle ../../afilaxy-kmm/
cp gradlew ../../afilaxy-kmm/
cp gradlew.bat ../../afilaxy-kmm/
cp gradle.properties ../../afilaxy-kmm/
echo -e "${GREEN}✅ Gradle wrapper copiado${NC}\n"

# 4. Copiar código inicial
echo -e "${BLUE}🔧 Passo 4: Copiando código inicial...${NC}"
cp kmm-config/Greeting.kt ../../afilaxy-kmm/shared/src/commonMain/kotlin/com/afilaxy/shared/
cp kmm-config/Platform.kt ../../afilaxy-kmm/shared/src/commonMain/kotlin/com/afilaxy/shared/
cp kmm-config/Platform.android.kt ../../afilaxy-kmm/shared/src/androidMain/kotlin/com/afilaxy/shared/Platform.kt
cp kmm-config/Platform.ios.kt ../../afilaxy-kmm/shared/src/iosMain/kotlin/com/afilaxy/shared/Platform.kt
echo -e "${GREEN}✅ Código inicial copiado${NC}\n"

# 5. Copiar documentação
echo -e "${BLUE}📚 Passo 5: Copiando documentação...${NC}"
cp kmm-config/README.md ../../afilaxy-kmm/
echo -e "${GREEN}✅ README copiado${NC}\n"

# 6. Copiar .gitignore
echo -e "${BLUE}🔒 Passo 6: Copiando .gitignore...${NC}"
cp .gitignore ../../afilaxy-kmm/
echo -e "${GREEN}✅ .gitignore copiado${NC}\n"

# 7. Copiar Firebase (se existir)
echo -e "${BLUE}🔥 Passo 7: Copiando configuração Firebase...${NC}"
if [ -f "google-services.json" ]; then
    cp google-services.json ../../afilaxy-kmm/androidApp/
    echo -e "${GREEN}✅ google-services.json copiado${NC}"
else
    echo -e "${BLUE}⚠️  google-services.json não encontrado - você precisará copiá-lo manualmente${NC}"
fi

if [ -f ".env" ]; then
    cp .env ../../afilaxy-kmm/
    echo -e "${GREEN}✅ .env copiado${NC}"
fi

if [ -f ".env.example" ]; then
    cp .env.example ../../afilaxy-kmm/
fi
echo ""

# 8. Verificar estrutura
echo -e "${BLUE}🔍 Passo 8: Verificando estrutura criada...${NC}"
cd ../../afilaxy-kmm
echo ""
tree -L 3 -I 'build|.gradle' || ls -la
echo ""

echo -e "${GREEN}✅✅✅ Setup concluído com sucesso! ✅✅✅${NC}"
echo ""
echo -e "${BLUE}📍 Próximos passos:${NC}"
echo "1. Abra o Android Studio"
echo "2. File → Open → /home/afilaxy/Projetos/afilaxy-kmm"
echo "3. Aguarde o Gradle sync"
echo "4. Rode o Android app para testar"
echo ""
echo -e "${GREEN}🚀 Bem-vindo ao Afilaxy KMM!${NC}"
