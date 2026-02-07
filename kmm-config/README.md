# 🫁 Afilaxy KMM

> Versão Kotlin Multiplatform Mobile do Afilaxy - disponível para Android e iOS

## 🎯 Sobre

Este é o projeto **Afilaxy** migrado para **Kotlin Multiplatform Mobile (KMM)**, permitindo compartilhar 60-80% do código entre Android e iOS.

### Estrutura do Projeto

```
afilaxy-kmm/
├── shared/                    # 60-80% código compartilhado
│   ├── src/commonMain/        # Kotlin comum (Domain, Data, ViewModels)
│   ├── src/androidMain/       # Específico Android
│   └── src/iosMain/           # Específico iOS
├── androidApp/                # UI Android (Jetpack Compose)
└── iosApp/                    # UI iOS (SwiftUI)
```

## ✨ Novo nesta Versão

- ✅ **Código compartilhado**: Domain, Data, ViewModels funcionam em ambas plataformas
- ✅ **Firebase KMM**: Auth, Firestore, Messaging funcionando em Android e iOS
- ✅ **Koin DI**: Substituiu Hilt para compatibilidade multiplataforma
- ✅ **KMM-ViewModel**: ViewModels compartilhados entre plataformas
- ✅ **UI Nativa**: Compose no Android, SwiftUI no iOS

## 🚀 Começando

### Pré-requisitos

- **Android Studio** Hedgehog ou superior
- **JDK 17**
- **Xcode 15+** (apenas para desenvolvimento iOS, em macOS)
- **CocoaPods** (apenas para iOS): `sudo gem install cocoapods`

### Setup

1. **Clone o repositório**:
   ```bash
   git clone https://github.com/herb-sin/afilaxy-kmm.git
   cd afilaxy-kmm
   ```

2. **Configure Firebase**:
   - Copie `google-services.json` para `androidApp/`
   - Configure `.env` com credenciais

3. **Abra no Android Studio**:
   ```bash
   open -a "Android Studio" .
   ```

4. **Sync Gradle**:
   - Aguarde o sync completar
   - Verifique que não há erros

### Rodando

**Android**:
```bash
./gradlew :androidApp:installDebug
```

**iOS** (apenas em macOS):
```bash
cd iosApp
pod install
open iosApp.xcworkspace
```

## 📦 Tecnologias

| Camada | Tecnologia |
|--------|------------|
| **Shared** | Kotlin Multiplatform |
| **DI** | Koin |
| **Firebase** | GitLive Firebase KMM |
| **ViewModel** | KMM-ViewModel |
| **Settings** | Multiplatform Settings |
| **UI Android** | Jetpack Compose |
| **UI iOS** | SwiftUI |

## 📁 Camadas

### Shared (Código Comum)

- **Domain**: Models, Use Cases, Repository Interfaces
- **Data**: Repository Implementations, Firebase wrappers
- **Presentation**: ViewModels compartilhados

### Android App

- UI em Jetpack Compose
- Integração com ViewModels compartilhados
- Google Maps Android SDK

### iOS App

- UI em SwiftUI
- Integração com ViewModels compartilhados via KMM
- Apple Maps (nativo)

## 🔄 Migração do Original

Este projeto foi migrado do [afilaxy](https://github.com/herb-sin/afilaxy) original (Android nativo).

**Diferenças principais**:
- Hilt → Koin
- Código compartilhado entre plataformas
- Dual UI (Compose + SwiftUI)

## 📝 Licença

MIT License - veja [LICENSE](LICENSE)

## 👨‍💻 Desenvolvedor

Desenvolvido com ❤️ por [@herb-sin](https://github.com/herb-sin)
