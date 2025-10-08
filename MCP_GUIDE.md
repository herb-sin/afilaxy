# 🫁 Guia MCP Afilaxy

## ✅ Status Atual

Seu projeto **já possui um MCP funcional** com acesso dinâmico ao estado atual! 

## 🛠️ Ferramentas Disponíveis

### 1. **Análise de Emergência**
```bash
analyze_emergency_flow EmergencyViewModel
```
- Analisa complexidade do código
- Sugere otimizações
- Identifica gargalos

### 2. **Auditoria de Segurança**
```bash
security_audit all
```
- Verifica validações de autenticação
- Analisa sanitização de inputs
- Checa configurações de segurança

### 3. **Métricas de Performance**
```bash
performance_metrics firebase
```
- Monitora Firebase
- Analisa geolocalização
- Verifica otimizações de UI

### 4. **Dados de Teste**
```bash
generate_test_data 5 "São Paulo"
```
- Gera emergências simuladas
- Cria cenários de teste
- Popula dados mock

### 5. **Saúde do Projeto** ⭐
```bash
project_health
```
- Conta arquivos Kotlin
- Verifica cobertura de testes
- Analisa estrutura geral

### 6. **Status Firebase** ⭐
```bash
firebase_status
```
- Verifica configuração
- Checa arquivos necessários
- Valida integração

### 7. **Sugestões de Código** ⭐
```bash
code_suggestions ui
```
- Áreas: `ui`, `data`, `domain`, `security`
- Sugestões contextuais
- Melhorias específicas

### 8. **Status do Build** ⭐
```bash
build_status
```
- Verifica Gradle
- Checa dependências
- Valida configuração

## 🚀 Como Usar

### Método 1: Cliente JavaScript
```bash
node mcp-client.js
```

### Método 2: Integração IDE
Configure seu IDE para usar o MCP server:
```json
{
  "mcpServers": {
    "afilaxy-dev": {
      "command": "node",
      "args": ["mcp-server.js"],
      "cwd": "/home/afilaxy/Projetos/afilaxy Kotlin/afilaxy"
    }
  }
}
```

### Método 3: Scripts NPM
```bash
npm run mcp-server
```

## 💡 Benefícios Obtidos

✅ **Acesso Dinâmico**: O MCP lê o estado atual do projeto em tempo real
✅ **Análise Contextual**: Entende a arquitetura Clean + MVVM do Afilaxy
✅ **Sugestões Específicas**: Recomendações baseadas no código atual
✅ **Monitoramento**: Acompanha saúde e performance do projeto
✅ **Automação**: Gera dados de teste e executa auditorias

## 🎯 Próximos Passos

1. **Integre com seu IDE** usando a configuração MCP
2. **Execute análises regulares** com `project_health`
3. **Use sugestões** para melhorar o código
4. **Monitore Firebase** antes de deploys
5. **Gere dados de teste** para desenvolvimento

## 🔧 Personalização

Edite `mcp-server.js` para adicionar:
- Novas ferramentas específicas do Afilaxy
- Integrações com APIs externas
- Análises customizadas de saúde
- Métricas específicas de asma/emergência

Seu MCP está **pronto e funcional**! 🎉