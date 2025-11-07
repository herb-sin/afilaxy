# 📱 Afilaxy APK - Instruções de Instalação

## 📦 Arquivos Gerados:

### 🔧 Debug Version (Para Desenvolvimento)
- **Arquivo:** afilaxy-v0.1.5-alpha-debug.apk
- **Tamanho:** 20MB
- **Uso:** Testes e desenvolvimento
- **Logs:** Habilitados para debug

### 🚀 Release Version (Para Produção)
- **Arquivo:** afilaxy-v0.1.5-alpha-release.apk  
- **Tamanho:** 6.4MB (otimizado)
- **Uso:** Instalação final em dispositivos
- **Performance:** Otimizada com ProGuard

## 📲 Como Instalar:

### Método 1: Via USB (Android Studio)
1. Conecte o dispositivo via USB
2. Ative 'Depuração USB' nas Opções do Desenvolvedor
3. Execute: `adb install afilaxy-v0.1.5-alpha-release.apk`

### Método 2: Instalação Manual
1. Transfira o APK para o dispositivo
2. Ative 'Fontes Desconhecidas' nas Configurações
3. Abra o arquivo APK no dispositivo
4. Toque em 'Instalar'

### Método 3: Via QR Code/Link
1. Hospede o APK em servidor web
2. Gere QR code com link do APK
3. Escaneie com dispositivo Android

## ⚙️ Requisitos do Sistema:
- **Android:** 6.0+ (API 23)
- **RAM:** 2GB mínimo
- **Armazenamento:** 50MB livres
- **GPS:** Obrigatório
- **Internet:** Obrigatório (Firebase)

## 🔐 Permissões Necessárias:
- Localização (GPS)
- Internet
- Notificações
- Câmera (futuramente)

## ✅ Funcionalidades Testáveis:
- Login/Cadastro Firebase
- Ativação de Helper
- Solicitação de Emergência  
- Visualização no Mapa
- Notificações Push
- Comunidade (produtos/eventos)

**Recomendação:** Use a versão Release para testes finais.
