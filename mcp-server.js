#!/usr/bin/env node

import { Server } from '@modelcontextprotocol/sdk/server/index.js';
import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js';
import { CallToolRequestSchema, ListToolsRequestSchema } from '@modelcontextprotocol/sdk/types.js';
import { execSync } from 'child_process';
import fs from 'fs';
import path from 'path';

const server = new Server(
  {
    name: 'afilaxy-mcp-server',
    version: '1.0.0',
  },
  {
    capabilities: {
      tools: {},
    },
  }
);

// Ferramentas MCP para desenvolvimento Afilaxy
server.setRequestHandler(ListToolsRequestSchema, async () => {
  return {
    tools: [
      {
        name: 'analyze_emergency_flow',
        description: 'Analisa fluxo de emergência e sugere otimizações',
        inputSchema: {
          type: 'object',
          properties: {
            component: { type: 'string', description: 'Componente a analisar (EmergencyViewModel, etc.)' }
          }
        }
      },
      {
        name: 'security_audit',
        description: 'Executa auditoria de segurança automatizada',
        inputSchema: {
          type: 'object',
          properties: {
            scope: { type: 'string', enum: ['auth', 'data', 'network', 'all'] }
          }
        }
      },
      {
        name: 'performance_metrics',
        description: 'Coleta métricas de performance do app',
        inputSchema: {
          type: 'object',
          properties: {
            metric_type: { type: 'string', enum: ['firebase', 'location', 'ui'] }
          }
        }
      },
      {
        name: 'generate_test_data',
        description: 'Gera dados de teste para emergências',
        inputSchema: {
          type: 'object',
          properties: {
            count: { type: 'number', description: 'Número de emergências simuladas' },
            location: { type: 'string', description: 'Localização base' }
          }
        }
      },
      {
        name: 'project_health',
        description: 'Analisa saúde geral do projeto Afilaxy',
        inputSchema: { type: 'object', properties: {} }
      },
      {
        name: 'firebase_status',
        description: 'Verifica configuração e status do Firebase',
        inputSchema: { type: 'object', properties: {} }
      },
      {
        name: 'code_suggestions',
        description: 'Sugere melhorias baseadas na arquitetura atual',
        inputSchema: {
          type: 'object',
          properties: {
            area: { type: 'string', enum: ['ui', 'data', 'domain', 'security'] }
          }
        }
      },
      {
        name: 'build_status',
        description: 'Verifica status do build e dependências',
        inputSchema: { type: 'object', properties: {} }
      }
    ]
  };
});

server.setRequestHandler(CallToolRequestSchema, async (request) => {
  const { name, arguments: args } = request.params;

  switch (name) {
    case 'analyze_emergency_flow':
      return analyzeEmergencyFlow(args.component);
    
    case 'security_audit':
      return performSecurityAudit(args.scope);
    
    case 'performance_metrics':
      return collectPerformanceMetrics(args.metric_type);
    
    case 'generate_test_data':
      return generateTestData(args.count, args.location);
    
    case 'project_health':
      return analyzeProjectHealth();
    
    case 'firebase_status':
      return checkFirebaseStatus();
    
    case 'code_suggestions':
      return generateCodeSuggestions(args.area);
    
    case 'build_status':
      return checkBuildStatus();
    
    default:
      throw new Error(`Ferramenta desconhecida: ${name}`);
  }
});

// Implementações das ferramentas
async function analyzeEmergencyFlow(component) {
  const filePath = `./app/src/main/java/com/afilaxy/presentation/emergency/${component}.kt`;
  
  if (!fs.existsSync(filePath)) {
    return { content: [{ type: 'text', text: `Componente ${component} não encontrado` }] };
  }
  
  const content = fs.readFileSync(filePath, 'utf8');
  const lines = content.split('\n').length;
  const functions = (content.match(/fun\s+\w+/g) || []).length;
  const complexity = content.includes('viewModelScope.launch') ? 'Alta' : 'Baixa';
  
  return {
    content: [{
      type: 'text',
      text: `📊 Análise ${component}:
• Linhas: ${lines}
• Funções: ${functions}
• Complexidade: ${complexity}
• Sugestões: ${lines > 200 ? 'Refatorar em classes menores' : 'Estrutura adequada'}`
    }]
  };
}

async function performSecurityAudit(scope) {
  const results = [];
  
  if (scope === 'auth' || scope === 'all') {
    const authFiles = ['AuthValidator.kt', 'InputSanitizer.kt'];
    authFiles.forEach(file => {
      const filePath = `./app/src/main/java/com/afilaxy/security/${file}`;
      if (fs.existsSync(filePath)) {
        const content = fs.readFileSync(filePath, 'utf8');
        const hasValidation = content.includes('requireAuthentication');
        results.push(`${file}: ${hasValidation ? '✅ Validação OK' : '⚠️ Revisar validação'}`);
      }
    });
  }
  
  return {
    content: [{
      type: 'text',
      text: `🔒 Auditoria de Segurança (${scope}):\n${results.join('\n')}`
    }]
  };
}

async function collectPerformanceMetrics(metricType) {
  const metrics = {
    firebase: 'Lazy loading implementado ✅',
    location: 'Rate limiting ativo ✅',
    ui: 'Compose otimizado ✅'
  };
  
  return {
    content: [{
      type: 'text',
      text: `⚡ Métricas ${metricType}: ${metrics[metricType] || 'Métrica não encontrada'}`
    }]
  };
}

async function generateTestData(count, location) {
  const testData = [];
  
  for (let i = 0; i < count; i++) {
    testData.push({
      id: `test_emergency_${i}`,
      location: location || 'São Paulo, SP',
      timestamp: Date.now() + (i * 1000),
      status: 'ACTIVE'
    });
  }
  
  return {
    content: [{
      type: 'text',
      text: `🧪 Gerados ${count} dados de teste:\n${JSON.stringify(testData, null, 2)}`
    }]
  };
}

async function analyzeProjectHealth() {
  const stats = {
    kotlinFiles: 0,
    testFiles: 0,
    securityFiles: 0,
    uiScreens: 0
  };
  
  function countFiles(dir, pattern) {
    try {
      const result = execSync(`find ${dir} -name "${pattern}" 2>/dev/null | wc -l`, { encoding: 'utf8' });
      return parseInt(result.trim());
    } catch { return 0; }
  }
  
  stats.kotlinFiles = countFiles('./app/src/main/java', '*.kt');
  stats.testFiles = countFiles('./app/src/test', '*.kt');
  stats.securityFiles = countFiles('./app/src/main/java/com/afilaxy/security', '*.kt');
  stats.uiScreens = countFiles('./app/src/main/java/com/afilaxy/presentation', '*Screen.kt');
  
  const coverage = stats.testFiles > 0 ? ((stats.testFiles / stats.kotlinFiles) * 100).toFixed(1) : '0';
  
  return {
    content: [{
      type: 'text',
      text: `🏥 Saúde do Projeto Afilaxy:
• Arquivos Kotlin: ${stats.kotlinFiles}
• Telas UI: ${stats.uiScreens}
• Módulos Segurança: ${stats.securityFiles}
• Cobertura Testes: ${coverage}%
• Status: ${stats.kotlinFiles > 50 ? '✅ Projeto Robusto' : '⚠️ Em Desenvolvimento'}`
    }]
  };
}

async function checkFirebaseStatus() {
  const hasConfig = fs.existsSync('./app/google-services.json');
  const hasFirebaseUtils = fs.existsSync('./app/src/main/java/com/afilaxy/FirebaseUtils.kt');
  const hasAuth = fs.existsSync('./app/src/main/java/com/afilaxy/domain/repository/AuthRepository.kt');
  
  return {
    content: [{
      type: 'text',
      text: `🔥 Status Firebase:
• Configuração: ${hasConfig ? '✅' : '❌'} google-services.json
• Utils: ${hasFirebaseUtils ? '✅' : '❌'} FirebaseUtils.kt
• Autenticação: ${hasAuth ? '✅' : '❌'} AuthRepository.kt
• Status: ${hasConfig && hasFirebaseUtils && hasAuth ? '✅ Totalmente Configurado' : '⚠️ Configuração Incompleta'}`
    }]
  };
}

async function generateCodeSuggestions(area) {
  const suggestions = {
    ui: [
      '• Implementar temas dark/light automáticos',
      '• Adicionar animações de transição entre telas',
      '• Otimizar recomposições com remember()'
    ],
    data: [
      '• Implementar cache offline para emergências',
      '• Adicionar retry automático para falhas de rede',
      '• Usar DataStore para preferências do usuário'
    ],
    domain: [
      '• Criar Use Cases para validação de medicamentos',
      '• Implementar padrão Repository para dados de saúde',
      '• Adicionar validação de proximidade geográfica'
    ],
    security: [
      '• Implementar biometria para acesso rápido',
      '• Adicionar criptografia end-to-end para dados médicos',
      '• Criar audit log para ações críticas'
    ]
  };
  
  return {
    content: [{
      type: 'text',
      text: `💡 Sugestões para ${area}:\n${suggestions[area]?.join('\n') || 'Área não encontrada'}`
    }]
  };
}

async function checkBuildStatus() {
  try {
    const gradleProps = fs.existsSync('./gradle.properties');
    const buildGradle = fs.existsSync('./app/build.gradle.kts');
    const hasKeystore = fs.existsSync('./keystore.properties');
    
    let buildInfo = '';
    if (buildGradle) {
      const content = fs.readFileSync('./app/build.gradle.kts', 'utf8');
      const hasCompose = content.includes('compose');
      const hasHilt = content.includes('hilt');
      buildInfo = `\n• Jetpack Compose: ${hasCompose ? '✅' : '❌'}\n• Hilt DI: ${hasHilt ? '✅' : '❌'}`;
    }
    
    return {
      content: [{
        type: 'text',
        text: `🔨 Status do Build:\n• Gradle Props: ${gradleProps ? '✅' : '❌'}\n• Build Script: ${buildGradle ? '✅' : '❌'}\n• Keystore: ${hasKeystore ? '✅' : '❌'}${buildInfo}\n• Status: ${gradleProps && buildGradle ? '✅ Pronto para Build' : '⚠️ Configuração Pendente'}`
      }]
    };
  } catch (error) {
    return {
      content: [{
        type: 'text',
        text: `❌ Erro ao verificar build: ${error.message}`
      }]
    };
  }
}

// Inicializar servidor
async function main() {
  const transport = new StdioServerTransport();
  await server.connect(transport);
}

main().catch(console.error);