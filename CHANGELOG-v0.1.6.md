# 🚀 Afilaxy v0.1.6-alpha - Sistema de Notificações

## 📅 Data: 04/11/2025 - 21:07

## 🆕 Novas Funcionalidades:

### 📱 Sistema de Notificações Push (FCM)
- **NotificationRepository**: Gerenciamento de tokens FCM
- **AfilaxyFirebaseMessagingService**: Service para receber notificações
- **Integração completa**: Login, Home e Emergency ViewModels

### 🔔 Funcionalidades de Notificação:
- **Token automático**: Salvo no login do usuário
- **Notificações de emergência**: Para helpers próximos
- **Canal dedicado**: "Emergências" com alta prioridade
- **Limpeza segura**: Token removido no logout

## 🔧 Melhorias Técnicas:

### 📋 Permissões Adicionadas:
- `POST_NOTIFICATIONS` (Android 13+)
- `INTERNET` (explícita)

### 🏗️ Arquitetura:
- Repository pattern para notificações
- Service dedicado para FCM
- Integração com Firestore para tokens

## 📱 APKs Disponíveis:

### 🚀 Release (Recomendado):
- **Arquivo**: `afilaxy-v0.1.6-alpha-notifications.apk`
- **Tamanho**: 6.4MB
- **Otimizado**: ProGuard + R8

### 🔧 Debug:
- **Arquivo**: `afilaxy-v0.1.6-alpha-debug-notifications.apk`
- **Tamanho**: 20MB
- **Logs**: Habilitados

## ✅ Funcionalidades Testáveis:

- ✅ Login/Cadastro Firebase
- ✅ Sistema de Helpers com GPS
- ✅ Notificações Push (FCM)
- ✅ Emergências com alertas
- ✅ Mapas Google integrados
- ✅ Comunidade (produtos/eventos)

## 🎯 Próximos Passos:
- Backend para envio real de notificações
- Testes em múltiplos dispositivos
- Otimizações de performance
- Publicação na Play Store

---
**Versão anterior sem notificações: v0.1.5-alpha**
**Nova versão com notificações: v0.1.6-alpha** ⭐