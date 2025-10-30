# 🔐 Segurança Firebase - Afilaxy

## ⚠️ ATENÇÃO: Dados Sigilosos

O arquivo `google-services.json` contém **credenciais sensíveis** que **NUNCA** devem ser commitadas:

### 🚨 Dados Sensíveis:
- **API Keys**: Acesso aos serviços Firebase
- **Project IDs**: Identificação do projeto
- **App IDs**: Identificadores únicos da aplicação

## ✅ Proteção Implementada

### 1. GitIgnore Configurado
```gitignore
# Google Services (e.g. APIs or Firebase)
# CRITICAL SECURITY: Never commit real Firebase credentials
google-services.json
!google-services.json.template
```

### 2. Template Seguro
- `google-services.json.template` - Template público
- `google-services.json` - Arquivo real (ignorado pelo git)

### 3. Verificação de Segurança
```bash
# Verificar se arquivo está sendo rastreado
git status | grep google-services

# Remover do rastreamento se necessário
git rm --cached app/google-services.json
```

## 🛠️ Configuração Segura

### Para Desenvolvedores:

1. **Copiar template**:
   ```bash
   cp app/google-services.json.template app/google-services.json
   ```

2. **Configurar credenciais reais**:
   - Baixar do Firebase Console
   - Substituir valores no arquivo local
   - **NUNCA** commitar o arquivo real

3. **Verificar .gitignore**:
   ```bash
   git check-ignore app/google-services.json
   # Deve retornar: app/google-services.json
   ```

### Para Produção:

1. **CI/CD**: Usar variáveis de ambiente
2. **Build**: Gerar arquivo durante build
3. **Deploy**: Injetar credenciais no momento do deploy

## 🔍 Auditoria de Segurança

### Verificar Histórico:
```bash
# Verificar se credenciais foram commitadas
git log --oneline --follow -- app/google-services.json

# Verificar conteúdo de commits anteriores
git show COMMIT_HASH:app/google-services.json
```

### Limpar Histórico (se necessário):
```bash
# CUIDADO: Reescreve histórico
git filter-branch --force --index-filter \
  'git rm --cached --ignore-unmatch app/google-services.json' \
  --prune-empty --tag-name-filter cat -- --all
```

## 📋 Checklist de Segurança

- [x] `google-services.json` no `.gitignore`
- [x] Template público disponível
- [x] Arquivo real não rastreado
- [ ] Credenciais reais configuradas localmente
- [ ] CI/CD configurado com variáveis seguras
- [ ] Auditoria de commits anteriores realizada

## 🚨 Em Caso de Exposição

1. **Revogar credenciais** no Firebase Console
2. **Gerar novas chaves** API
3. **Limpar histórico** do git (se necessário)
4. **Atualizar** todas as instâncias da aplicação