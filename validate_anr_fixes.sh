#!/bin/bash

# 🔍 Script de Validação das Correções de ANR - Afilaxy

echo "🚀 Validando correções de ANR no Afilaxy..."
echo "=============================================="

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Função para verificar se arquivo existe e contém otimizações
check_file_optimization() {
    local file=$1
    local pattern=$2
    local description=$3
    
    if [ -f "$file" ]; then
        if grep -q "$pattern" "$file"; then
            echo -e "${GREEN}✅ $description${NC}"
            return 0
        else
            echo -e "${RED}❌ $description - Otimização não encontrada${NC}"
            return 1
        fi
    else
        echo -e "${RED}❌ $description - Arquivo não encontrado: $file${NC}"
        return 1
    fi
}

# Contador de verificações
total_checks=0
passed_checks=0

echo "📱 Verificando MainActivity..."
((total_checks++))
if check_file_optimization "app/src/main/java/com/afilaxy/MainActivity.kt" "AnrOptimizer" "MainActivity com AnrOptimizer"; then
    ((passed_checks++))
fi

echo ""
echo "🗺️ Verificando Google Maps..."
((total_checks++))
if check_file_optimization "app/src/main/java/com/afilaxy/presentation/emergency/EmergencyScreenMaps.kt" "MapsOptimizer" "EmergencyScreenMaps com MapsOptimizer"; then
    ((passed_checks++))
fi

echo ""
echo "🧠 Verificando ViewModel..."
((total_checks++))
if check_file_optimization "app/src/main/java/com/afilaxy/presentation/emergency/EmergencyViewModel.kt" "withTimeout" "EmergencyViewModel com timeout"; then
    ((passed_checks++))
fi

echo ""
echo "🏗️ Verificando Application..."
((total_checks++))
if check_file_optimization "app/src/main/java/com/afilaxy/AfilaxyApplication.kt" "AnrOptimizer" "AfilaxyApplication otimizada"; then
    ((passed_checks++))
fi

echo ""
echo "⚡ Verificando MapsOptimizer..."
((total_checks++))
if check_file_optimization "app/src/main/java/com/afilaxy/performance/MapsOptimizer.kt" "safeCameraOperation" "MapsOptimizer criado"; then
    ((passed_checks++))
fi

echo ""
echo "🔧 Verificando AnrOptimizer..."
((total_checks++))
if check_file_optimization "app/src/main/java/com/afilaxy/performance/AnrOptimizer.kt" "executeAsync" "AnrOptimizer funcional"; then
    ((passed_checks++))
fi

echo ""
echo "=============================================="
echo "📊 Resultado da Validação:"
echo "=============================================="

if [ $passed_checks -eq $total_checks ]; then
    echo -e "${GREEN}🎉 SUCESSO! Todas as $total_checks otimizações foram aplicadas!${NC}"
    echo ""
    echo "✅ Próximos passos:"
    echo "   1. Compile o app: ./gradlew assembleDebug"
    echo "   2. Instale no dispositivo: adb install app/build/outputs/apk/debug/app-debug.apk"
    echo "   3. Monitore logs: adb logcat | grep -E 'ANR|Choreographer|MapsOptimizer'"
    echo "   4. Teste a tela de emergência com mapas"
    echo ""
    echo "🔍 Comandos úteis para monitoramento:"
    echo "   • ANR Detection: adb logcat | grep 'ANR in com.afilaxy'"
    echo "   • Frame Drops: adb logcat | grep 'Skipped.*frames'"
    echo "   • Performance: adb logcat | grep 'MapsOptimizer\\|AnrOptimizer'"
    
    exit 0
else
    echo -e "${RED}⚠️  ATENÇÃO! $((total_checks - passed_checks)) de $total_checks otimizações falharam!${NC}"
    echo ""
    echo "🔧 Ações necessárias:"
    echo "   1. Verifique os arquivos marcados com ❌"
    echo "   2. Reaplique as otimizações necessárias"
    echo "   3. Execute novamente este script"
    
    exit 1
fi