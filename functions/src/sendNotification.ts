import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';

if (!admin.apps.length) {
  admin.initializeApp();
}

export const sendEmergencyNotification = functions.firestore
  .document('emergency_requests/{requestId}')
  .onCreate(async (snap, context) => {
    const data = snap.data();
    
    if (!data.isActive) return;
    
    try {
      // Buscar helpers próximos
      const helpersSnapshot = await admin.firestore()
        .collection('helpers')
        .where('isActive', '==', true)
        .get();
      
      const notifications: Promise<any>[] = [];
      
      for (const helperDoc of helpersSnapshot.docs) {
        const helper = helperDoc.data();
        const helperId = helper.id;
        
        // Pular o próprio usuário
        if (helperId === data.requesterId) continue;
        
        // Calcular distância
        const distance = calculateDistance(
          data.latitude, data.longitude,
          helper.location.latitude, helper.location.longitude
        );
        
        // Só notificar se estiver próximo (260m)
        if (distance <= 0.26) {
          // Buscar token FCM do helper
          const userDoc = await admin.firestore()
            .collection('users')
            .doc(helperId)
            .get();
          
          const fcmToken = userDoc.data()?.fcmToken;
          
          if (fcmToken) {
            const message = {
              token: fcmToken,
              notification: {
                title: '🆘 Emergência de Asma',
                body: `Alguém precisa de ajuda a ${Math.round(distance * 1000)}m de você`
              },
              data: {
                type: 'emergency_request',
                emergency_id: context.params.requestId,
                requesterName: data.requesterName || 'Alguém',
                distance: Math.round(distance * 1000).toString()
              }
            };
            
            notifications.push(admin.messaging().send(message));
          }
        }
      }
      
      await Promise.all(notifications);
      console.log(`Enviadas ${notifications.length} notificações de emergência`);
      
    } catch (error) {
      console.error('Erro ao enviar notificações:', error);
    }
  });

function calculateDistance(lat1: number, lon1: number, lat2: number, lon2: number): number {
  const R = 6371; // km
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
    Math.sin(dLon/2) * Math.sin(dLon/2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
  return R * c;
}