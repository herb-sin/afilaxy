# 🔧 Correção Firebase SHA-1 - Dispositivos Físicos

## 🚨 Problema Identificado

```
Erro no login: An internal error has occurred. 
[Requests from this Android client application com.afilaxy.debug are blocked.]
```

**Causa**: SHA-1 fingerprint do APK não registrada no Firebase Console.

## 🔑 SHA-1 Fingerprint Necessária

**Debug Keystore SHA-1:**
```
6B:9E:29:D2:5E:08:53:96:42:AA:C4:9C:30:3C:4E:0B:66:3B:45:02
```

## 🛠️ Solução Rápida

### 1. Acesse Firebase Console
1. Vá para [Firebase Console](https://console.firebase.google.com/)
2. Selecione seu projeto Afilaxy
3. Vá em **Project Settings** (⚙️)
4. Aba **General**

### 2. Adicione SHA-1 Fingerprint
1. Role até **Your apps**
2. Clique no app Android (`com.afilaxy.debug`)
3. Clique em **Add fingerprint**
4. Cole a SHA-1: `6B:9E:29:D2:5E:08:53:96:42:AA:C4:9C:30:3C:4E:0B:66:3B:45:02`
5. Clique **Save**

### 3. Aguarde Propagação
- **Tempo**: 5-10 minutos
- **Status**: Firebase precisa atualizar configurações

## 🚀 Teste Imediato

Após adicionar a SHA-1:

1. **Reinstale o APK**:
   ```bash
   adb uninstall com.afilaxy.debug
   adb install apk-output/afilaxy-optimized-v2.apk
   ```

2. **Teste o Login**:
   - Abra o app
   - Tente fazer login
   - Deve funcionar normalmente

## 📋 Verificação

### Logs para Monitorar
```bash
adb logcat | grep -E "FirebaseAuth|GoogleApiManager"
```

### Sucesso Esperado
```
FirebaseAuth: Logging in as usuario@email.com
FirebaseAuth: Notifying auth state listeners about user
```

## 🔍 Informações Técnicas

### Package Name
- **Debug**: `com.afilaxy.debug`
- **Release**: `com.afilaxy` (quando fizer release)

### Keystore Locations
- **Debug**: `~/.android/debug.keystore`
- **Release**: `app/afilaxy-release.keystore`

### SHA-1 Commands
```bash
# Debug SHA-1
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android

# Release SHA-1 (futuro)
keytool -list -v -keystore app/afilaxy-release.keystore -alias afilaxy
```

## ⚠️ Importante

### Para Produção
- Use keystore de release diferente
- Registre SHA-1 de release no Firebase
- Mude package para `com.afilaxy`

### Segurança
- **Nunca** commite keystores de release
- Mantenha senhas seguras
- Use diferentes SHA-1 para debug/release

## ✅ Checklist

- [ ] SHA-1 adicionada no Firebase Console
- [ ] Aguardado 5-10 minutos
- [ ] APK reinstalado no dispositivo
- [ ] Login testado com sucesso

**Status**: 🔧 **Aguardando configuração Firebase**