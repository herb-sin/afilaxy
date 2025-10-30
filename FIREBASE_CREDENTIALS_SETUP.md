# 🔥 Configuração de Credenciais Firebase - Afilaxy

## 1. Criar Projeto Firebase

### Passo 1: Acessar Firebase Console
1. Acesse: https://console.firebase.google.com/
2. Clique em "Criar um projeto"
3. Nome do projeto: `afilaxy-prod`
4. Ative Google Analytics (opcional)

### Passo 2: Configurar Authentication
1. No menu lateral: **Authentication**
2. Clique em "Começar"
3. Aba **Sign-in method**
4. Ative **Email/senha**
5. Ative **Link de email** (opcional)

### Passo 3: Configurar Firestore
1. No menu lateral: **Firestore Database**
2. Clique em "Criar banco de dados"
3. Modo: **Produção** (com regras de segurança)
4. Localização: **southamerica-east1** (São Paulo)

### Passo 4: Adicionar App Android
1. No menu lateral: **Visão geral do projeto**
2. Clique no ícone Android
3. Package name: `com.afilaxy.debug`
4. App nickname: `Afilaxy Debug`
5. SHA-1: (opcional para desenvolvimento)

### Passo 5: Baixar Credenciais
1. Baixe o arquivo `google-services.json`
2. **NÃO** substitua o arquivo atual ainda
3. Salve como `google-services-prod.json` temporariamente

## 2. Configurar Domínios Autorizados

### Authentication > Settings > Authorized domains
Adicione seus domínios:
- `localhost` (desenvolvimento)
- `afilaxy.com` (se tiver domínio)
- `your-app-domain.com`

## 3. Configurar Regras de Segurança

### Firestore Rules:
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can only access their own data
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Emergency requests - authenticated users only
    match /emergencies/{emergencyId} {
      allow read, write: if request.auth != null;
    }
  }
}
```

## 4. Testar Configuração

### Opção A: Ambiente de Desenvolvimento
```bash
# Manter arquivo atual para desenvolvimento
cp app/google-services.json app/google-services-dev.json

# Usar credenciais de produção
cp google-services-prod.json app/google-services.json
```

### Opção B: Firebase Emulator (Recomendado)
```bash
# Instalar Firebase CLI
npm install -g firebase-tools

# Login
firebase login

# Inicializar emulators
firebase init emulators

# Executar emulators
firebase emulators:start --only auth,firestore
```

## 5. Validar Funcionamento

1. **Build do app**: `./gradlew assembleDebug`
2. **Testar cadastro**: Criar nova conta
3. **Verificar Firestore**: Dados salvos corretamente
4. **Testar login**: Autenticação funcionando

## ⚠️ Importante

- **Nunca** commite `google-services.json` real
- Use **emulators** para desenvolvimento
- **Produção** apenas com credenciais reais
- **Revogue** credenciais antigas se expostas