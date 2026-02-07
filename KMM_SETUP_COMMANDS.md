# 🚀 Setup do Projeto Afilaxy KMM

Este documento contém instruções passo-a-passo para criar o novo projeto **afilaxy-kmm**.

## Estrutura Final

```
/home/afilaxy/Projetos/
├── afilaxy Kotlin/              ✅ Original preservado
├── afilaxy Kotlin (Backup)/     ✅ Backup de segurança
└── afilaxy-kmm/                 🆕 Novo projeto KMM
    ├── shared/                  # Código compartilhado (60-80%)
    │   ├── src/
    │   │   ├── commonMain/kotlin/
    │   │   ├── androidMain/kotlin/
    │   │   └── iosMain/kotlin/
    │   └── build.gradle.kts
    ├── androidApp/              # App Android (Compose)
    │   ├── src/main/kotlin/
    │   └── build.gradle.kts
    ├── iosApp/                  # App iOS (SwiftUI)
    ├── gradle/
    │   └── libs.versions.toml
    ├── build.gradle.kts
    └── settings.gradle.kts
```

---

## Passo 1: Criar Diretórios

```bash
cd /home/afilaxy/Projetos

# Criar estrutura completa
mkdir -p afilaxy-kmm/shared/src/commonMain/kotlin
mkdir -p afilaxy-kmm/shared/src/androidMain/kotlin
mkdir -p afilaxy-kmm/shared/src/iosMain/kotlin
mkdir -p afilaxy-kmm/androidApp/src/main/kotlin
mkdir -p afilaxy-kmm/androidApp/src/main/res
mkdir -p afilaxy-kmm/iosApp
mkdir -p afilaxy-kmm/gradle

# Verificar
ls -la afilaxy-kmm/
```

---

## Passo 2: Copiar Arquivos de Configuração

```bash
cd "/home/afilaxy/Projetos/afilaxy Kotlin/afilaxy"

# Copiar configurações do diretório kmm-config para o novo projeto
cp kmm-config/settings.gradle.kts ../../../afilaxy-kmm/
cp kmm-config/build.gradle.kts ../../../afilaxy-kmm/
cp kmm-config/shared-build.gradle.kts ../../../afilaxy-kmm/shared/build.gradle.kts
cp kmm-config/androidApp-build.gradle.kts ../../../afilaxy-kmm/androidApp/build.gradle.kts
cp kmm-config/libs.versions.toml ../../../afilaxy-kmm/gradle/

# Copiar gradle wrapper do projeto original
cp -r gradle ../../../afilaxy-kmm/
cp gradlew ../../../afilaxy-kmm/
cp gradlew.bat ../../../afilaxy-kmm/
cp gradle.properties ../../../afilaxy-kmm/

# Copiar .gitignore
cp .gitignore ../../../afilaxy-kmm/
```

---

## Passo 3: Copiar Firebase e Secrets

```bash
# Copiar configuração Firebase
cp google-services.json ../../../afilaxy-kmm/androidApp/
cp .env ../../../afilaxy-kmm/
cp .env.example ../../../afilaxy-kmm/
```

---

## Passo 4: Verificar Estrutura

```bash
cd /home/afilaxy/Projetos/afilaxy-kmm
tree -L 3 -I 'build|.gradle'
```

---

## Passo 5: Sync Gradle (Android Studio)

1. Abra o Android Studio
2. **Open** → `/home/afilaxy/Projetos/afilaxy-kmm`
3. Aguarde o Gradle sync completar
4. Verifique que não há erros

---

## Próximos Passos

Após completar o setup:

1. ✅ Estrutura KMM criada
2. 🔄 Migrar models da camada Domain
3. 🔄 Migrar repositórios da camada Data
4. 🔄 Migrar ViewModels
5. 🔄 Refatorar UI Android
6. 🔄 Criar UI iOS em SwiftUI

---

## Troubleshooting

### Erro: "Plugin not found"
- Verifique que `libs.versions.toml` está em `gradle/`
- Execute `./gradlew --refresh-dependencies`

### Erro: Firebase
- Confirme que `google-services.json` existe em `androidApp/`
- Adicione `classpath("com.google.gms:google-services:4.4.3")` no root build.gradle.kts

### Erro: CocoaPods (iOS)
- Execute `sudo gem install cocoapods` (apenas em macOS/CI)
- Para desenvolvimento apenas Android, comente `kotlinCocoapods` no shared/build.gradle.kts
