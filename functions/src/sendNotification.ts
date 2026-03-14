import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';

if (!admin.apps.length) {
  admin.initializeApp();
}

export const sendEmergencyNotification = functions.firestore
  .document('emergency_requests/{requestId}')
  .onCreate(async (snap, context) => {
    const data = snap.data();
    const requestId = context.params.requestId;
    
    console.log('🔥 Firebase Function triggered:', {
      requestId,
      data: JSON.stringify(data)
    });
    
    if (!data.active) return;

    if (typeof data.latitude !== 'number' || typeof data.longitude !== 'number') {
      console.warn('Emergency request sem coordenadas válidas. Notificação ignorada.', { requestId });
      return;
    }
    
    try {
      // Buscar helpers próximos
      const helpersSnapshot = await admin.firestore()
        .collection('helpers')
        .where('isActive', '==', true)
        .get();
      
      const nearbyHelpers: Array<{helper: any, helperId: string, distance: number, fcmToken: string}> = [];
      
      // Buscar helpers próximos e ordenar por distância
      for (const helperDoc of helpersSnapshot.docs) {
        const helper = helperDoc.data();
        const helperId = helperDoc.id || helper.id;

        if (!helperId) continue;
        
        // Pular o próprio usuário
        if (helperId === data.requesterId) continue;

        const helperLatitude = helper?.location?.latitude;
        const helperLongitude = helper?.location?.longitude;

        if (typeof helperLatitude !== 'number' || typeof helperLongitude !== 'number') {
          continue;
        }
        
        // Calcular distância
        const distance = calculateDistance(
          data.latitude, data.longitude,
          helperLatitude, helperLongitude
        );
        
        // Só considerar se estiver próximo (250m)
        if (distance <= 0.25) {
          // Buscar token FCM do helper
          const userDoc = await admin.firestore()
            .collection('users')
            .doc(helperId)
            .get();
          
          const fcmToken = userDoc.data()?.fcmToken;
          
          if (typeof fcmToken === 'string' && fcmToken.trim().length > 0) {
            nearbyHelpers.push({ helper, helperId, distance, fcmToken });
          }
        }
      }
      
      // Ordenar por distância e pegar apenas os 3 mais próximos
      const closestHelpers = nearbyHelpers
        .sort((a, b) => a.distance - b.distance)
        .slice(0, 3);
      
      const notifications: Promise<any>[] = [];
      
      // Enviar notificações apenas para os 3 mais próximos
      for (const { distance, fcmToken, helperId } of closestHelpers) {
        const message = {
          token: fcmToken,
          data: {
            type: 'emergency_request',
            emergency_id: requestId,
            requesterName: data.requesterName || 'Alguém',
            distance: Math.round(distance * 1000).toString(),
            isEmergencyResponse: 'true',
            helper_id: helperId
          }
        };
        
        console.log('📨 Sending FCM to closest helper:', {
          token: fcmToken.substring(0, 20) + '...',
          emergency_id: requestId,
          distance: Math.round(distance * 1000) + 'm',
          helper_id: helperId
        });
        
        notifications.push(admin.messaging().send(message));
      }
      
      // Armazenar helpers notificados para cancelamento posterior
      await admin.firestore()
        .collection('emergency_requests')
        .doc(requestId)
        .update({
          notifiedHelpers: closestHelpers.map(h => ({
            helperId: h.helperId,
            fcmToken: h.fcmToken,
            distance: h.distance
          }))
        });
      
      await Promise.all(notifications);
      console.log(`Enviadas ${notifications.length} notificações para os helpers mais próximos (máx 3, raio 250m)`);
      
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