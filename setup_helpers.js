// Script para configurar usuários como helpers
const admin = require('firebase-admin');

// Inicializar Firebase Admin
const serviceAccount = require('./serviceAccountKey.json');
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function setupHelpers() {
  try {
    // Buscar todos os usuários
    const usersSnapshot = await db.collection('users').get();
    
    console.log(`Encontrados ${usersSnapshot.size} usuários`);
    
    // Configurar todos como helpers com localização fictícia
    const batch = db.batch();
    
    usersSnapshot.forEach((doc) => {
      const userRef = db.collection('users').doc(doc.id);
      batch.update(userRef, {
        isHelper: true,
        location: new admin.firestore.GeoPoint(-23.5505, -46.6333), // São Paulo
        lastLocationUpdate: admin.firestore.FieldValue.serverTimestamp()
      });
      console.log(`Configurando usuário ${doc.id} como helper`);
    });
    
    await batch.commit();
    console.log('Todos os usuários configurados como helpers!');
    
  } catch (error) {
    console.error('Erro:', error);
  }
  
  process.exit(0);
}

setupHelpers();