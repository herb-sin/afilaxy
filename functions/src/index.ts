import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';

admin.initializeApp();

export const sendEmergencyNotification = functions.firestore
  .document('notifications/{notificationId}')
  .onCreate(async (snap, context) => {
    const data = snap.data();
    
    console.log('🚨 Nova notificação de emergência:', data);
    
    if (data.type !== 'emergency_request') {
      console.log('❌ Tipo de notificação inválido');
      return null;
    }
    
    const fcmToken = data.fcmToken;
    if (!fcmToken) {
      console.log('❌ Token FCM não encontrado');
      return null;
    }
    
    const message = {
      token: fcmToken,
      notification: {
        title: data.title || '🆘 Emergência de Asma',
        body: data.body || 'Alguém precisa de ajuda'
      },
      data: {
        type: 'emergency_request',
        requesterName: data.requesterName || '',
        distance: data.distance || '0'
      },
      android: {
        priority: 'high' as const,
        notification: {
          priority: 'high' as const,
          defaultSound: true,
          defaultVibrateTimings: true
        }
      }
    };
    
    try {
      const response = await admin.messaging().send(message);
      console.log('✅ FCM enviado com sucesso:', response);
      
      // Marcar como enviado
      await snap.ref.update({ sent: true, sentAt: admin.firestore.FieldValue.serverTimestamp() });
      
      return response;
    } catch (error) {
      console.error('❌ Erro ao enviar FCM:', error);
      
      // Marcar como erro
      await snap.ref.update({ 
        sent: false, 
        error: error instanceof Error ? error.message : String(error),
        errorAt: admin.firestore.FieldValue.serverTimestamp() 
      });
      
      throw error;
    }
  });