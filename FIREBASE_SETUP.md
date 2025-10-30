# 🔥 Configuração Firebase - Afilaxy

## ⚠️ Problema Atual

O app está usando configurações de template do Firebase, causando erros:
- `API key not valid. Please pass a valid API key`
- `An internal error has occurred`

## 🛠️ Soluções

### Opção 1: Firebase Emulator (Recomendado para Desenvolvimento)

```bash
# Instalar Firebase CLI
npm install -g firebase-tools

# Login no Firebase
firebase login

# Inicializar projeto
firebase init emulators

# Configurar emulators (Auth, Firestore)
firebase emulators:start --only auth,firestore
```

### Opção 2: Projeto Firebase Real

1. **Criar projeto no [Firebase Console](https://console.firebase.google.com/)**
2. **Ativar Authentication** com método email/senha
3. **Ativar Firestore Database**
4. **Baixar google-services.json** e substituir o atual
5. **Configurar domínios autorizados** nas configurações de Auth

### Opção 3: Configuração Manual (Temporária)

O app já tem configurações demo que permitem teste básico:

```json
{
  "project_id": "afilaxy-demo",
  "api_key": "AIzaSyDemoKeyForAfilaxyDebug123456789"
}
```

## 🔧 Fluxo Corrigido

### Cadastro:
1. Usuário preenche dados na tela "Cadastrar"
2. Firebase cria conta e envia email de verificação
3. Usuário é deslogado automaticamente
4. Tela volta para modo "Entrar"
5. Mensagem de sucesso é exibida

### Login:
1. Usuário tenta fazer login
2. Se email não verificado → Tela de verificação
3. Se email verificado → Acesso ao app

## 📱 Teste do Fluxo

1. **Cadastrar novo usuário**
2. **Verificar email recebido**
3. **Fazer login após verificação**
4. **Confirmar acesso às funcionalidades**

## 🚀 APK Atualizado

O novo APK inclui:
- Mensagens de erro mais claras
- Fluxo de cadastro/login corrigido
- Configuração Firebase demo funcional