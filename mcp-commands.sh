#!/bin/bash

echo "🔧 Comandos MCP Afilaxy"
echo "======================"
echo "1. Análise de código: npm run analyze"
echo "2. Auditoria segurança: npm run security"  
echo "3. Métricas: npm run metrics"
echo "4. Dados teste: npm run testdata"

# Adicionar scripts ao package.json
cat > package.json << 'EOF'
{
  "name": "afilaxy-mcp-tools",
  "version": "1.0.0",
  "type": "module",
  "dependencies": {
    "@modelcontextprotocol/sdk": "^0.4.0"
  },
  "scripts": {
    "mcp-server": "node mcp-server.js",
    "analyze": "echo 'Use: analyze_emergency_flow EmergencyViewModel'",
    "security": "echo 'Use: security_audit all'",
    "metrics": "echo 'Use: performance_metrics firebase'",
    "testdata": "echo 'Use: generate_test_data 5 \"São Paulo\"'"
  }
}
EOF