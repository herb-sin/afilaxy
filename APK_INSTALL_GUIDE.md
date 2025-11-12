# 📱 Guia de Instalação - Afilaxy APK

## 📦 APK Gerado
- **Arquivo**: `afilaxy-v1.0-debug.apk`
- **Tamanho**: 20MB
- **Versão**: 1.0 (Debug)
- **Data**: 12/11/2024

## 🔧 Instalação no Dispositivo

### 1. **Transferir APK**
```bash
# Via ADB (se conectado)
adb install afilaxy-v1.0-debug.apk

# Via cabo USB
# Copie o arquivo para o dispositivo
```

### 2. **Habilitar Fontes Desconhecidas**
- Configurações → Segurança → Fontes Desconhecidas ✅
- Ou: Configurações → Apps → Acesso Especial → Instalar apps desconhecidos

### 3. **Instalar APK**
- Abra o arquivo APK no dispositivo
- Toque em "Instalar"
- Aguarde a instalação

## ⚠️ Permissões Necessárias
- 📍 **Localização** (Obrigatória)
- 🔔 **Notificações** (Recomendada)
- 📱 **Internet** (Automática)

## 🧪 Funcionalidades para Testar

### ✅ **Autenticação**
- Login/Cadastro Firebase
- Verificação de email

### ✅ **Helper Toggle**
- Ativar/desativar status de helper
- Verificação de permissões de localização

### ✅ **Emergência**
- Solicitar ajuda
- Buscar helpers próximos
- Cancelar solicitação

### ✅ **Perfil**
- Editar informações pessoais
- Dados de saúde (tipo de asma, medicamentos)
- Contato de emergência

### ✅ **Navegação**
- Menu lateral
- Transições entre telas
- Botões de voltar

## 🐛 Relatório de Bugs
Se encontrar problemas:
1. Anote o erro exato
2. Passos para reproduzir
3. Modelo do dispositivo
4. Versão do Android

## 📊 Melhorias Implementadas
- ✅ Arquitetura Clean + MVVM
- ✅ Segurança robusta (CWE-306 corrigido)
- ✅ Performance otimizada
- ✅ Error handling melhorado
- ✅ Dependency Injection
- ✅ Logging seguro

**APK pronto para testes em dispositivos físicos!** 🚀