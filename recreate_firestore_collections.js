const admin = require('firebase-admin');

// Inicializar Firebase Admin
const serviceAccount = require('./functions/lib/serviceAccountKey.json');
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function recreateCollections() {
  console.log('🔄 Recriando collections do Firestore...');
  
  try {
    // 1. Collection: users (exemplo)
    console.log('📝 Criando collection users...');
    await db.collection('users').doc('example').set({
      name: 'Example User',
      email: 'example@test.com',
      isHelper: false,
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    });
    
    // 2. Collection: helpers
    console.log('🆘 Criando collection helpers...');
    await db.collection('helpers').doc('example-helper').set({
      id: 'example-helper',
      name: 'Helper Example',
      email: 'helper@test.com',
      location: new admin.firestore.GeoPoint(-23.5505, -46.6333), // São Paulo
      isActive: true,
      lastUpdate: Date.now()
    });
    
    // 3. Collection: emergency_requests
    console.log('🚨 Criando collection emergency_requests...');
    await db.collection('emergency_requests').doc('example-emergency').set({
      requesterId: 'example-user',
      location: new admin.firestore.GeoPoint(-23.5505, -46.6333),
      status: 'waiting',
      active: true,
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    });
    
    // 4. Collection: emergency_chats
    console.log('💬 Criando collection emergency_chats...');
    const chatRef = db.collection('emergency_chats').doc('example-emergency');
    await chatRef.set({
      emergencyId: 'example-emergency',
      participants: ['example-user', 'example-helper'],
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    });
    
    // Subcollection: messages
    await chatRef.collection('messages').doc('example-message').set({
      senderId: 'example-user',
      message: 'Preciso de ajuda com minha bombinha!',
      timestamp: admin.firestore.FieldValue.serverTimestamp()
    });
    
    // 5. Collection: notifications
    console.log('🔔 Criando collection notifications...');
    await db.collection('notifications').doc('example-notification').set({
      userId: 'example-helper',
      type: 'emergency_request',
      title: 'Pedido de Socorro',
      message: 'Alguém próximo precisa de ajuda!',
      data: {
        emergency_id: 'example-emergency',
        requester_location: {
          latitude: -23.5505,
          longitude: -46.6333
        }
      },
      read: false,
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    });
    
    console.log('✅ Collections recriadas com sucesso!');
    console.log('📋 Collections criadas:');
    console.log('   - users');
    console.log('   - helpers');
    console.log('   - emergency_requests');
    console.log('   - emergency_chats (com subcollection messages)');
    console.log('   - notifications');
    
    // Remover documentos de exemplo após 5 segundos
    setTimeout(async () => {
      console.log('🧹 Removendo documentos de exemplo...');
      await db.collection('users').doc('example').delete();
      await db.collection('helpers').doc('example-helper').delete();
      await db.collection('emergency_requests').doc('example-emergency').delete();
      await db.collection('emergency_chats').doc('example-emergency').delete();
      await db.collection('notifications').doc('example-notification').delete();
      console.log('✅ Documentos de exemplo removidos. Collections vazias criadas.');
      process.exit(0);
    }, 5000);
    
  } catch (error) {
    console.error('❌ Erro ao recriar collections:', error);
    process.exit(1);
  }
}

recreateCollections();