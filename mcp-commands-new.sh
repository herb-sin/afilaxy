#!/bin/bash

echo "🫁 Afilaxy MCP Commands"
echo "======================"
echo "1. analyze_emergency_flow <component>"
echo "2. security_audit <scope>"
echo "3. performance_metrics <type>"
echo "4. generate_test_data <count> <location>"
echo "5. project_health"
echo "6. firebase_status"
echo "7. code_suggestions <area>"
echo "8. build_status"
echo ""
echo "Exemplos:"
echo "  ./mcp-commands.sh analyze EmergencyViewModel"
echo "  ./mcp-commands.sh health"
echo "  ./mcp-commands.sh firebase"
echo "  ./mcp-commands.sh suggest ui"
echo "  ./mcp-commands.sh build"

case $1 in
  "analyze")
    echo "📊 Analisando componente: $2"
    ;;
  "health")
    echo "🏥 Verificando saúde do projeto..."
    ;;
  "firebase")
    echo "🔥 Verificando Firebase..."
    ;;
  "suggest")
    echo "💡 Gerando sugestões para: $2"
    ;;
  "build")
    echo "🔨 Verificando build..."
    ;;
  *)
    echo "Use um dos comandos listados acima"
    ;;
esac