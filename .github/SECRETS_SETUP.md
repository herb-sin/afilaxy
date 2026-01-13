# 🔐 Configuração de Secrets para CI/CD

Para que a pipeline CI/CD funcione corretamente, você precisa configurar os seguintes secrets no GitHub:

## Como configurar secrets:
1. Vá para **Settings** → **Secrets and variables** → **Actions**
2. Clique em **New repository secret**
3. Adicione cada secret abaixo

## 🔑 Secrets Necessários

### Firebase Configuration
```
GOOGLE_SERVICES_JSON
```
- **Valor**: Conteúdo do arquivo `google-services.json` codificado em base64
- **Como obter**: `base64 -w 0 app/google-services.json`

```
FIREBASE_PROJECT_ID
```
- **Valor**: ID do seu projeto Firebase (ex: `afilaxy-app`)

```
FIREBASE_APP_ID
```
- **Valor**: ID da aplicação Firebase (ex: `1:123456789:android:abcdef`)

```
FIREBASE_API_KEY
```
- **Valor**: Chave da API Firebase

```
FIREBASE_STORAGE_BUCKET
```
- **Valor**: Bucket do Firebase Storage (ex: `afilaxy-app.appspot.com`)

```
FIREBASE_SERVICE_ACCOUNT
```
- **Valor**: JSON da conta de serviço do Firebase para App Distribution

```
FIREBASE_CONFIG
```
- **Valor**: (DEPRECATED) Use GOOGLE_SERVICES_JSON instead
- **Observação**: Mantido apenas para compatibilidade com workflows antigos

### Android Signing
```
KEYSTORE_BASE64
```
- **Valor**: Arquivo keystore codificado em base64
- **Como obter**: `base64 -w 0 app/keystore.jks`

```
KEYSTORE_PASSWORD
```
- **Valor**: Senha do keystore

```
KEY_ALIAS
```
- **Valor**: Alias da chave de assinatura

```
KEY_PASSWORD
```
- **Valor**: Senha da chave de assinatura

### Play Store (Opcional)
```
PLAY_STORE_SERVICE_ACCOUNT
```
- **Valor**: JSON da conta de serviço do Google Play Console

## 🚀 Como usar

### CI (Continuous Integration)
- Executa automaticamente em push/PR para `main` e `develop`
- Roda testes unitários e lint
- Gera APK de debug

### CD (Continuous Deployment)
- **Firebase App Distribution**: Deploy automático no push para `main`
- **Play Store**: Deploy automático em tags `v*` (ex: `v1.0.0`)

### Release Manual
- Acesse **Actions** → **Release**
- Clique em **Run workflow**
- Informe versão e notas de release

## 📋 Checklist de Configuração

- [ ] Todos os secrets configurados
- [ ] Firebase App Distribution configurado
- [ ] Keystore de produção criado
- [ ] Play Store Console configurado (opcional)
- [ ] Testadores adicionados no Firebase App Distribution

## 🔧 Comandos Úteis

```bash
# Gerar keystore
keytool -genkey -v -keystore app/keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias afilaxy

# Codificar arquivo em base64
base64 -w 0 arquivo.json

# Testar build local
./gradlew assembleRelease
```

## 🔍 Troubleshooting

### "GOOGLE_SERVICES_JSON secret is not configured"
- Verifique se o secret foi criado em Settings → Secrets and variables → Actions
- O valor deve ser o arquivo `google-services.json` codificado em base64
- Comando: `base64 -w 0 app/google-services.json | pbcopy`

### "GOOGLE_SERVICES_JSON is not valid JSON"
- O conteúdo do secret pode estar corrompido
- Recrie o secret usando: `base64 -w 0 app/google-services.json`
- Certifique-se de copiar TODO o output sem quebras de linha

### "KEYSTORE_BASE64 secret is not set"
- Necessário para builds de produção (release)
- Gere um keystore: `keytool -genkey -v -keystore app/keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias afilaxy`
- Codifique: `base64 -w 0 app/keystore.jks`

### "FIREBASE_SERVICE_ACCOUNT secret is not configured"
- Necessário para deploy no Firebase App Distribution
- Obtenha em: Firebase Console → Project Settings → Service Accounts
- Gere uma nova chave privada e cole o conteúdo JSON completo no secret

### "No hardcoded secrets detected" mas o workflow falha
- Verifique se não há API keys do Firebase diretamente no código
- Padrão detectado: `AIza` seguido de 35 caracteres
- Todos os secrets devem estar em `local.properties` ou secrets do GitHub