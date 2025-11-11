# Setup Helper de Teste

## Adicionar Helper Fictício no Firestore

1. Acesse Firebase Console > Firestore
2. Coleção: `helpers`
3. Adicione documento com ID: `test_helper_001`

```json
{
  "userId": "test_helper_001",
  "location": {
    "latitude": -23.940395,
    "longitude": -46.322205
  },
  "fcmToken": "fake_token_for_testing",
  "isActive": true,
  "lastUpdate": "2025-11-10T18:15:00Z"
}
```

Isso criará um helper próximo à sua localização de teste para receber a notificação.