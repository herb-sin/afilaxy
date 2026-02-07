# Afilaxy KMM - Arquivos de Configuração

Este diretório contém todos os arquivos de configuração necessários para criar o projeto **afilaxy-kmm**.

## 📁 Conteúdo

### Gradle Configuration
- `settings.gradle.kts` - Configuração principal do projeto
- `build.gradle.kts` - Build script raiz
- `shared-build.gradle.kts` - Build do módulo shared (copiar para shared/build.gradle.kts)
- `androidApp-build.gradle.kts` - Build do androidApp (copiar para androidApp/build.gradle.kts)
- `libs.versions.toml` - Catálogo de versões (copiar para gradle/libs.versions.toml)

### Código Inicial
- `Greeting.kt` → `shared/src/commonMain/kotlin/com/afilaxy/shared/Greeting.kt`
- `Platform.kt` → `shared/src/commonMain/kotlin/com/afilaxy/shared/Platform.kt`
- `Platform.android.kt` → `shared/src/androidMain/kotlin/com/afilaxy/shared/Platform.kt`
- `Platform.ios.kt` → `shared/src/iosMain/kotlin/com/afilaxy/shared/Platform.kt`

### Documentação
- `README.md` - README do novo projeto
- `../KMM_SETUP_COMMANDS.md` - Guia de setup passo-a-passo

## 🚀 Como Usar

Siga os passos no arquivo `KMM_SETUP_COMMANDS.md` para criar o projeto completo.
