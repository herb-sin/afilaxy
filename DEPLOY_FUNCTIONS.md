# 🔥 Deploy Firebase Functions

## Pré-requisitos
```bash
npm install -g firebase-tools
```

## Deploy
```bash
# 1. Login no Firebase
firebase login

# 2. Selecionar projeto
firebase use afilaxy-app

# 3. Instalar dependências
cd functions
npm install

# 4. Deploy
firebase deploy --only functions
```

## Verificar
- Firebase Console > Functions
- Logs: `firebase functions:log`

## Como funciona
1. App salva notificação no Firestore
2. Function detecta automaticamente
3. Envia FCM real para o token
4. Marca como enviado/erro

## Teste
1. Solicitar emergência no app
2. Verificar logs: `firebase functions:log --only sendEmergencyNotification`
3. Helper deve receber notificação real