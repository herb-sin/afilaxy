# 🚀 Guia de Publicação - Google Play Store

Este guia descreve o processo para publicar o **Afilaxy** na Google Play Store.

## 📦 1. Gerar o App Bundle (AAB)

O Android App Bundle (.aab) é o formato exigido pela Google Play.

1. **Certifique-se de ter o keystore de release**:
   - O arquivo `afilaxy-release.keystore` deve estar na raiz do projeto.
   - Se não estiver, execute: `./generate_release_keystore.sh`

2. **Gere o bundle assinado**:
   ```bash
   ./gradlew bundleRelease
   ```
   - O arquivo será gerado em: `app/build/outputs/bundle/release/app-release.aab`

## 📝 2. Preparar a Ficha na Loja

No [Google Play Console](https://play.google.com/console):

### Detalhes do App
- **Nome do App**: Afilaxy
- **Breve descrição**: Conectando pessoas com asma em emergências. (Max 80 caracteres)
- **Descrição completa**: 
  > O Afilaxy é um aplicativo que conecta pessoas com asma a voluntários próximos que possuem medicamentos de emergência. 
  > 
  > **Funcionalidades:**
  > * Botão de emergência para solicitar ajuda rápida.
  > * Geolocalização para encontrar voluntários próximos.
  > * Comunidade com informações e eventos sobre saúde respiratória.
  > * Cadastro seguro e proteção de dados.

### Gráficos
- **Ícone**: 512x512 px (PNG)
- **Feature Graphic**: 1024x500 px (PNG/JPEG)
- **Screenshots**: Mínimo 2 capturas de tela (Recomendado: Tela Principal, Emergência, Comunidade).

### Política de Privacidade
- URL: `https://afilaxy.com/privacy` (ou a URL que você hospedou o arquivo `PRIVACIDADE_LGPD.md`)

## 🚀 3. Upload e Lançamento

1. **Criar nova release**:
   - Vá em **Teste e lançamento** > **Produção** (ou **Teste aberto** para beta).
   - Clique em **Criar nova versão**.

2. **Upload do AAB**:
   - Arraste o arquivo `app-release.aab` gerado.
   - Assegure-se de que a chave de assinatura foi aceita (Google Play App Signing).

3. **Notas da versão**:
   - Copie o conteúdo de `APK_RELEASE_NOTES.md` ou escreva as novidades.

4. **Revisão e Lançamento**:
   - Verifique se não há erros ou avisos impeditivos.
   - Clique em **Iniciar lançamento**.

## ⚠️ Importante

- **Keystore**: Guarde o arquivo `afilaxy-release.keystore` e as senhas em local seguro. Se perder, você não poderá atualizar o app.
- **Review**: O Google pode levar alguns dias para revisar o app.
