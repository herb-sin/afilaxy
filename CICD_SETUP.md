# ⚙️ Configuração CI/CD - GitHub Actions

## 1. Configurar Secrets no GitHub

### Acessar Configurações:
1. Vá para seu repositório no GitHub
2. **Settings** > **Secrets and variables** > **Actions**
3. Clique em **New repository secret**

### Secrets Necessários:

#### FIREBASE_CONFIG
```json
{
  "project_info": {
    "project_number": "123456789012",
    "project_id": "afilaxy-prod",
    "storage_bucket": "afilaxy-prod.appspot.com"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:123456789012:android:abcd1234",
        "android_client_info": {
          "package_name": "com.afilaxy.debug"
        }
      },
      "api_key": [
        {
          "current_key": "AIzaSyYourRealApiKeyHere"
        }
      ],
      "services": {
        "appinvite_service": {
          "other_platform_oauth_client": []
        }
      }
    }
  ],
  "configuration_version": "1"
}
```

#### KEYSTORE_BASE64
```bash
# Gerar keystore (se não tiver)
keytool -genkey -v -keystore app/keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias afilaxy

# Converter para base64
base64 -i app/keystore.jks | tr -d '\n'
```

#### Outros Secrets:
- **KEYSTORE_PASSWORD**: Senha do keystore
- **KEY_ALIAS**: Alias da chave (ex: afilaxy)
- **KEY_PASSWORD**: Senha da chave

## 2. Configurar build.gradle.kts

### Adicionar configuração de assinatura:
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("keystore.jks")
            storePassword = project.findProperty("KEYSTORE_PASSWORD") as String? ?: ""
            keyAlias = project.findProperty("KEY_ALIAS") as String? ?: ""
            keyPassword = project.findProperty("KEY_PASSWORD") as String? ?: ""
        }
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

## 3. Testar Pipeline

### Push para main:
```bash
git add .
git commit -m "feat: Configure secure CI/CD pipeline"
git push origin main
```

### Verificar Actions:
1. Vá para **Actions** no GitHub
2. Verifique se o workflow executou com sucesso
3. Baixe os APKs gerados

## 4. Configurações Avançadas

### Deploy Automático:
```yaml
- name: Deploy to Firebase App Distribution
  uses: wzieba/Firebase-Distribution-Github-Action@v1
  with:
    appId: ${{ secrets.FIREBASE_APP_ID }}
    token: ${{ secrets.FIREBASE_TOKEN }}
    groups: testers
    file: app/build/outputs/apk/release/app-release.apk
```

### Notificações Slack:
```yaml
- name: Notify Slack
  if: always()
  uses: 8398a7/action-slack@v3
  with:
    status: ${{ job.status }}
    webhook_url: ${{ secrets.SLACK_WEBHOOK }}
```

## 5. Segurança

### Verificações Automáticas:
- ✅ Scan de credenciais hardcoded
- ✅ Limpeza de arquivos sensíveis
- ✅ Validação de assinatura
- ✅ Testes de segurança

### Monitoramento:
- Logs de build protegidos
- Secrets mascarados automaticamente
- Artefatos com acesso controlado