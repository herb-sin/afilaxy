# 👥 Onboarding da Equipe - Afilaxy

## 🚀 Setup Inicial para Desenvolvedores

### 1. Clonar Repositório
```bash
git clone https://github.com/seu-usuario/afilaxy.git
cd afilaxy
```

### 2. Configurar Firebase (Desenvolvimento)

#### Opção A: Firebase Emulator (Recomendado)
```bash
# Instalar Firebase CLI
npm install -g firebase-tools

# Login no Firebase
firebase login

# Inicializar emulators
firebase init emulators

# Executar emulators
firebase emulators:start --only auth,firestore
```

#### Opção B: Credenciais de Desenvolvimento
```bash
# Copiar template
cp app/google-services.json.template app/google-services.json

# Solicitar credenciais de dev ao líder técnico
# Substituir valores no arquivo copiado
```

### 3. Build e Execução
```bash
# Dar permissão ao gradlew
chmod +x gradlew

# Build debug
./gradlew assembleDebug

# Executar no emulador/dispositivo
./gradlew installDebug
```

## 🔐 Regras de Segurança

### ❌ NUNCA Faça:
- Commitar `google-services.json` real
- Hardcodar API keys no código
- Compartilhar credenciais por chat/email
- Fazer push de keystores

### ✅ SEMPRE Faça:
- Usar emulators para desenvolvimento
- Verificar .gitignore antes de commits
- Solicitar credenciais via canal seguro
- Reportar exposição acidental imediatamente

## 📋 Workflow de Desenvolvimento

### 1. Nova Feature
```bash
# Criar branch
git checkout -b feature/nova-funcionalidade

# Desenvolver com emulators
firebase emulators:start --only auth,firestore

# Testar localmente
./gradlew test
./gradlew connectedAndroidTest

# Commit e push
git add .
git commit -m "feat: adiciona nova funcionalidade"
git push origin feature/nova-funcionalidade
```

### 2. Code Review
- PR obrigatório para main
- Review de segurança automático
- Testes passando obrigatório
- Aprovação de 1+ reviewer

### 3. Deploy
- Merge para main → Deploy automático
- APK disponível em GitHub Actions
- Notificação no Slack/Teams

## 🛠️ Ferramentas e Comandos

### Scripts Úteis:
```bash
# Gerar APK debug
./build_apk.sh debug

# Verificar segurança
./security_validation.sh

# Limpar build
./gradlew clean

# Executar testes
./gradlew test
```

### Debugging:
```bash
# Logs do Firebase
adb logcat | grep Firebase

# Logs do app
adb logcat | grep com.afilaxy

# Limpar dados do app
adb shell pm clear com.afilaxy.debug
```

## 📞 Contatos e Suporte

### Líder Técnico:
- **Nome**: [Seu Nome]
- **Email**: [seu-email@empresa.com]
- **Slack**: @seu-usuario

### Canais:
- **#afilaxy-dev**: Discussões técnicas
- **#afilaxy-alerts**: Alertas de CI/CD
- **#afilaxy-releases**: Releases e deploys

### Documentação:
- **Firebase**: `FIREBASE_CREDENTIALS_SETUP.md`
- **CI/CD**: `CICD_SETUP.md`
- **Segurança**: `SECURITY_FIREBASE.md`
- **APK Build**: `INSTALACAO_APK.md`

## 🎯 Checklist de Onboarding

- [ ] Repositório clonado
- [ ] Firebase emulators funcionando
- [ ] Build debug executado com sucesso
- [ ] App instalado no dispositivo/emulador
- [ ] Testes básicos executados
- [ ] Acesso aos canais de comunicação
- [ ] Documentação lida e compreendida
- [ ] Primeiro commit realizado (pequena alteração)
- [ ] PR criado e aprovado

## 🚨 Emergências

### Credenciais Expostas:
1. **Reportar imediatamente** ao líder técnico
2. **Não** tentar "consertar" sozinho
3. **Documentar** como aconteceu
4. **Aguardar** instruções para revogação

### Build Quebrado:
1. Verificar logs do GitHub Actions
2. Testar localmente com emulators
3. Reportar no canal #afilaxy-dev
4. Criar issue se necessário

### App Crashando:
1. Capturar logs completos
2. Reproduzir passos para crash
3. Verificar se é ambiente específico
4. Reportar com detalhes técnicos