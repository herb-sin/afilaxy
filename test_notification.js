const admin = require('firebase-admin');

// Inicializar Firebase Admin
const serviceAccount = require('./functions/service-account-key.json');
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: 'https://afilaxy-test-default-rtdb.firebaseio.com'
});

const db = admin.firestore();

async function testNotification() {
  try {
    console.log('Criando documento de teste na coleção push_notifications...');
    
    const testDoc = {
      to: 'test_user_id',
      data: {
        type: 'emergency_alert',
        title: '🚨 TESTE EMERGÊNCIA AFILAXY',
        body: 'Teste de notificação de emergência',
        emergencyId: 'test_emergency_123',
        requesterName: 'Usuário Teste'
      },
      timestamp: admin.firestore.FieldValue.serverTimestamp()
    };
    
    const docRef = await db.collection('push_notifications').add(testDoc);
    console.log('Documento criado com ID:', docRef.id);
    
    // Aguardar um pouco para ver se a função é executada
    setTimeout(() => {
      console.log('Teste concluído. Verifique os logs das Cloud Functions.');
      process.exit(0);
    }, 5000);
    
  } catch (error) {
    console.error('Erro no teste:', error);
    process.exit(1);
  }
}

testNotification();