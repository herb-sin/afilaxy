# Configuração de Credenciais - Afilaxy

## Arquivos Necessários

### 1. google-services.json
- Copie `app/google-services.json.example` para `app/google-services.json`
- Substitua os valores pelos dados do seu projeto Firebase:
  - `YOUR_PROJECT_NUMBER`
  - `your-project-id` 
  - `YOUR_MOBILE_SDK_APP_ID`
  - `YOUR_CLIENT_ID`
  - `YOUR_API_KEY`

### 2. local.properties
- Copie `local.properties.example` para `local.properties`
- Configure as variáveis:
  - `sdk.dir`: Caminho para o Android SDK
  - `MAPS_API_KEY`: Sua chave da API do Google Maps
  - `GEMINI_API_KEY`: Sua chave da API do Gemini

## Segurança
- ⚠️ NUNCA commite os arquivos reais com credenciais
- Use apenas os arquivos .example no controle de versão
- Mantenha as credenciais em variáveis de ambiente em produção