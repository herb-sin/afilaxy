# 🔒 Guia de Segurança - Afilaxy

## Configuração Segura

### 1. Variáveis de Ambiente
Copie `.env.example` para `.env` e configure:

```bash
cp .env.example .env
```

Configure as seguintes variáveis:
- `FIREBASE_PROJECT_ID`: ID do projeto Firebase
- `FIREBASE_APP_ID`: ID da aplicação Firebase
- `FIREBASE_API_KEY`: Chave da API Firebase
- `FIREBASE_STORAGE_BUCKET`: Bucket de armazenamento

### 2. Keystore de Produção
```bash
# Gerar keystore seguro
keytool -genkey -v -keystore afilaxy-release.keystore -alias afilaxy -keyalg RSA -keysize 2048 -validity 10000
```

### 3. Configuração de Rede
- ✅ Certificate pinning habilitado
- ✅ Cleartext traffic bloqueado em produção
- ✅ TLS 1.2+ obrigatório

## Recursos de Segurança Implementados

### 🛡️ Autenticação e Autorização
- **AuthGuard**: Verificação obrigatória de autenticação
- **Email verification**: Obrigatório para operações críticas
- **Rate limiting**: Prevenção de ataques de força bruta

### 🔍 Validação de Entrada
- **InputSanitizer**: Prevenção de injeção NoSQL
- **ValidationResult**: Validação robusta com feedback
- **Coordinate validation**: Validação de coordenadas GPS

### 📊 Monitoramento
- **SecurityMonitor**: Detecção de atividades suspeitas
- **Logging seguro**: Prevenção de log injection
- **Alertas automáticos**: Para tentativas de ataque

### 💾 Backup Seguro
- **Criptografia AES-256**: Para dados críticos
- **Rotação automática**: Manter apenas 5 backups
- **Validação de integridade**: Verificação de dados

## Práticas de Segurança

### ✅ Implementado
- [x] Credenciais não hardcoded
- [x] Validação de entrada robusta
- [x] Rate limiting inteligente
- [x] Logging seguro
- [x] Criptografia de dados sensíveis
- [x] Certificate pinning
- [x] Testes de segurança automatizados

### 🔄 Monitoramento Contínuo
- Verificação automática de dependências
- Scan de código para vulnerabilidades
- Testes de penetração regulares
- Auditoria de logs de segurança

## Resposta a Incidentes

### 1. Detecção Automática
- Tentativas de injeção SQL/NoSQL
- Múltiplas falhas de login
- Requests anômalos

### 2. Ações Automáticas
- Bloqueio temporário de usuário
- Rate limiting agressivo
- Alertas para equipe de segurança

### 3. Investigação
```bash
# Verificar logs de segurança
adb logcat | grep "SecurityMonitor\|SecurityUtils"

# Analisar tentativas de ataque
grep "injection_attempt\|failed_login" security.log
```

## Configuração de Desenvolvimento

### Ambiente Local
```bash
# Configurar emulador Firebase
firebase emulators:start --only auth,firestore

# Executar testes de segurança
./gradlew test --tests "*security*"
```

### Verificações Pré-Deploy
```bash
# Lint de segurança
./gradlew lint

# Verificar secrets
grep -r "AIza\|AAAA\|sk_\|pk_" app/src/ --exclude-dir=test

# Testes de integração
./gradlew connectedAndroidTest
```

## Contato de Segurança

Para reportar vulnerabilidades de segurança:
- 📧 Email: security@afilaxy.com
- 🔒 PGP Key: [Disponível no site]
- ⏱️ Tempo de resposta: 24 horas

## Atualizações de Segurança

- **Dependências**: Atualizadas mensalmente
- **Patches críticos**: Aplicados em 48h
- **Revisões de código**: Obrigatórias para mudanças de segurança

---

**⚠️ IMPORTANTE**: Nunca commite credenciais, chaves ou tokens no repositório. Use sempre variáveis de ambiente ou armazenamento seguro.