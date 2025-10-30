# 📱 Como Instalar o APK do Afilaxy

## 📦 APK Gerado

✅ **APK Debug disponível**: `apk-output/afilaxy-debug.apk` (20MB)

## 🔧 Instalação no Android

### Passo 1: Transferir o APK
1. Conecte o dispositivo Android ao computador via USB
2. Copie o arquivo `afilaxy-debug.apk` para o dispositivo
3. Ou envie por WhatsApp/email para o dispositivo

### Passo 2: Habilitar Fontes Desconhecidas
1. Vá em **Configurações** > **Segurança**
2. Ative **Fontes desconhecidas** ou **Instalar apps desconhecidos**
3. No Android 8+: Permita para o app específico (Arquivos, Chrome, etc.)

### Passo 3: Instalar
1. Abra o gerenciador de arquivos no dispositivo
2. Navegue até onde salvou o APK
3. Toque no arquivo `afilaxy-debug.apk`
4. Toque em **Instalar**
5. Aguarde a instalação concluir

## ⚠️ Importante

- **APK Debug**: Versão de desenvolvimento com logs habilitados
- **Permissões**: O app solicitará permissões de localização
- **Firebase**: Certifique-se que o Firebase está configurado
- **Internet**: Necessária para autenticação e funcionalidades

## 🚀 Gerando APK Release

Para gerar uma versão otimizada para produção:

```bash
# APK Release (otimizado)
./gradlew assembleRelease

# Localização do APK Release
app/build/outputs/apk/release/app-release.apk
```

## 🔐 Assinatura Digital

Para distribuição oficial, o APK deve ser assinado:

```bash
# Gerar keystore (apenas uma vez)
./generate_keystore.sh

# Build assinado
./gradlew assembleRelease
```

## 📋 Resolução de Problemas

### "App não instalado"
- Verifique se há espaço suficiente no dispositivo
- Desinstale versões anteriores do app
- Reinicie o dispositivo

### "Arquivo corrompido"
- Baixe o APK novamente
- Verifique a integridade do arquivo

### Permissões negadas
- Vá em Configurações > Apps > Afilaxy > Permissões
- Ative todas as permissões necessárias

## 📞 Suporte

Em caso de problemas:
1. Verifique os logs do dispositivo
2. Teste em modo avião (offline)
3. Contate o desenvolvedor: [@herb-sin](https://github.com/herb-sin)