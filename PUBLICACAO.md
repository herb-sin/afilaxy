# 📦 Guia de Publicação - Google Play Store

## Versão Atual
- **versionCode**: 13
- **versionName**: 2.0.2

## 🔑 Pré-requisitos

### 1. Keystore (Certificado de Assinatura)

Se ainda não tem o arquivo `keystore.properties`, crie na raiz do projeto:

```properties
storeFile=afilaxy-release.jks
storePassword=SUA_SENHA_AQUI
keyAlias=afilaxy
keyPassword=SUA_SENHA_AQUI
```

### 2. Variáveis de Ambiente

Certifique-se de que o arquivo `.env` existe com:
```
MAPS_API_KEY=sua_chave_aqui
```

## 🚀 Gerar AAB

### Opção 1: Android Studio (Mais Fácil)

1. **Build → Generate Signed Bundle / APK**
2. Selecione **Android App Bundle**
3. Escolha sua keystore ou crie uma nova
4. Selecione **release**
5. Clique em **Finish**

📁 Arquivo gerado em: `app/release/app-release.aab`

### Opção 2: Linha de Comando

```bash
# 1. Limpar build anterior
./gradlew clean

# 2. Gerar AAB assinado
./gradlew bundleRelease

# 3. Arquivo gerado em:
# app/build/outputs/bundle/release/app-release.aab
```

## 📋 Checklist Pré-Publicação

### ✅ Código
- [x] OnBackInvokedCallback habilitado
- [x] ProGuard configurado
- [x] Otimizações de performance aplicadas
- [x] Logs de debug removidos em release
- [x] Versão incrementada (versionCode: 13)

### ✅ Configurações
- [x] Firebase configurado
- [x] Maps API Key configurada
- [x] Permissões declaradas no manifest
- [x] Ícones e recursos otimizados

### ✅ Testes
- [ ] Testar em dispositivo físico
- [ ] Verificar todas as funcionalidades principais:
  - [ ] Login/Cadastro
  - [ ] Criar emergência
  - [ ] Chat funcionando
  - [ ] Localização precisa
  - [ ] Notificações

## 📤 Upload para Google Play Console

### 1. Acesse o Console
https://play.google.com/console

### 2. Selecione o App "Afilaxy"

### 3. Navegue até "Produção" ou "Teste Interno/Fechado"

### 4. Criar Nova Versão
- Clique em "Criar nova versão"
- Faça upload do `app-release.aab`
- Preencha as notas de versão

### 5. Notas de Versão Sugeridas (v2.0.2)

```
🎉 Novidades da versão 2.0.2:

✨ Melhorias de Performance
• Interface mais fluida e responsiva
• Otimização no carregamento de mapas
• Redução no consumo de bateria

🐛 Correções
• Corrigido problema de navegação back
• Melhorias na estabilidade geral
• Otimizações de memória

🔒 Segurança
• Melhorias na proteção de dados
• Atualização de bibliotecas de segurança
```

### 6. Revisar e Publicar
- Revise todas as informações
- Clique em "Revisar versão"
- Clique em "Iniciar lançamento"

## 🔄 Próxima Versão

Para a próxima atualização, incremente em `build.gradle.kts`:

```kotlin
versionCode = 14  // Sempre incrementar
versionName = "2.0.3"  // Seguir semver
```

## 📊 Monitoramento Pós-Publicação

Após publicar, monitore:
- **Crashes**: Play Console → Qualidade → Crashes
- **ANRs**: Play Console → Qualidade → ANRs
- **Avaliações**: Responda feedback dos usuários
- **Métricas**: Instalações, desinstalações, retenção

## 🆘 Troubleshooting

### Erro: "Keystore not found"
```bash
# Crie uma nova keystore:
keytool -genkey -v -keystore afilaxy-release.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias afilaxy
```

### Erro: "MAPS_API_KEY not found"
- Verifique se o arquivo `.env` existe
- Ou defina a variável de ambiente:
```bash
export MAPS_API_KEY="sua_chave_aqui"
```

### Erro: "Firebase configuration missing"
- Certifique-se de que `google-services.json` está em `app/`
- Não deve estar no `.gitignore` para build local

## 📞 Suporte

Em caso de dúvidas:
1. Verifique a documentação do Android: https://developer.android.com/studio/publish
2. Console do Google Play: https://support.google.com/googleplay/android-developer
3. Firebase: https://firebase.google.com/support

---

**Última atualização**: 2026-01-08
**Desenvolvido por**: @herb-sin
