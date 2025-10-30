# 🔥 Criação Completa do Projeto Firebase - afilaxy-prod

## 1. Criar Novo Projeto

### Passo 1: Acessar Firebase Console
1. Acesse: https://console.firebase.google.com/
2. Clique em **"Adicionar projeto"** ou **"Criar um projeto"**

### Passo 2: Configurar Projeto
1. **Nome do projeto**: `afilaxy-prod`
2. **ID do projeto**: `afilaxy-prod` (ou será gerado automaticamente)
3. Clique em **"Continuar"**

### Passo 3: Google Analytics (Opcional)
1. **Ativar Google Analytics**: Recomendado ✅
2. Selecione conta do Analytics ou crie nova
3. Clique em **"Criar projeto"**

## 2. Configurar Authentication

### Passo 1: Ativar Authentication
1. Menu lateral: **Authentication**
2. Clique em **"Vamos começar"** ou **"Get started"**

### Passo 2: Configurar Provedores
1. Aba **"Sign-in method"**
2. Clique em **"Email/Password"**
3. **Ativar** a primeira opção (Email/Password)
4. **Salvar**

### Passo 3: Configurar Domínios (Opcional agora)
1. Aba **"Settings"** (engrenagem)
2. **"Authorized domains"**
3. Adicionar domínios depois se necessário

## 3. Configurar Firestore Database

### Passo 1: Criar Database
1. Menu lateral: **Firestore Database**
2. Clique em **"Criar banco de dados"**

### Passo 2: Configurar Segurança
1. **Modo de produção** ✅ (Recomendado)
2. Clique em **"Avançar"**

### Passo 3: Escolher Localização
1. **Região**: `southamerica-east1` (São Paulo) ✅
2. Clique em **"Concluído"**

### Passo 4: Configurar Regras
1. Após criação, clique na aba **"Regras"**
2. Substitua o conteúdo por:

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
    
    // Public read for app configuration
    match /config/{document} {
      allow read: if true;
      allow write: if false;
    }
  }
}
```

3. Clique em **"Publicar"**

## 4. Adicionar App Android

### Passo 1: Registrar App
1. Na página inicial do projeto, clique no ícone **Android**
2. **Nome do pacote Android**: `com.afilaxy.debug`
3. **Apelido do app**: `Afilaxy Debug`
4. **Certificado de assinatura SHA-1**: (deixar vazio por enquanto)
5. Clique em **"Registrar app"**

### Passo 2: Baixar Configuração
1. **Baixar google-services.json**
2. **NÃO substitua ainda** o arquivo no projeto
3. Salve como `google-services-prod.json` temporariamente

### Passo 3: Pular Próximos Passos
1. **Adicionar SDK**: Já configurado ✅
2. **Executar app**: Pular por enquanto
3. Clique em **"Continuar no console"**

## 5. Configurações Finais

### Passo 1: Configurar Domínios Autorizados
1. **Authentication** > **Settings** > **Authorized domains**
2. Adicionar:
   - `localhost` ✅
   - `afilaxy-prod.firebaseapp.com` ✅
   - `afilaxy-prod.web.app` ✅
   - `afilaxy.com` (se tiver domínio)

### Passo 2: Configurar Cloud Messaging (Opcional)
1. Menu lateral: **Cloud Messaging**
2. Configurar depois se necessário

## 6. Testar Configuração

### Passo 1: Substituir Arquivo
```bash
# Backup do arquivo atual
cp app/google-services.json app/google-services-dev.json

# Usar arquivo de produção
cp google-services-prod.json app/google-services.json
```

### Passo 2: Build e Teste
```bash
./gradlew clean
./gradlew assembleDebug
```

### Passo 3: Testar Funcionalidades
1. Instalar APK no dispositivo
2. Testar cadastro de usuário
3. Verificar dados no Firestore Console
4. Testar login

## ✅ Checklist Final

- [ ] Projeto `afilaxy-prod` criado
- [ ] Authentication configurado (Email/Password)
- [ ] Firestore Database criado (modo produção)
- [ ] Regras de segurança configuradas
- [ ] App Android registrado (`com.afilaxy.debug`)
- [ ] google-services.json baixado
- [ ] Domínios autorizados configurados
- [ ] Build do app funcionando
- [ ] Testes básicos realizados

## 🚨 Importante

- **Não commitar** o google-services.json real
- **Testar** todas as funcionalidades antes de usar em produção
- **Backup** das configurações importantes
- **Documentar** credenciais de forma segura