import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';

if (!admin.apps.length) {
  admin.initializeApp();
}

export const cancelOtherHelpers = functions.firestore
  .document('emergency_requests/{requestId}')
  .onUpdate(async (change, context) => {
    const beforeData = change.before.data();
    const afterData = change.after.data();
    const requestId = context.params.requestId;
    
    // Verificar se mudou de waiting para matched
    if (beforeData.status === 'waiting' && afterData.status === 'matched') {
      console.log('🔄 Emergency matched, cancelling other helpers for:', requestId);
      
      const notifiedHelpers = afterData.notifiedHelpers || [];
      const acceptedHelperId = afterData.helperId;
      
      // Filtrar helpers que não aceitaram
      const otherHelpers = notifiedHelpers.filter((helper: any) => 
        helper.helperId !== acceptedHelperId &&
        typeof helper.fcmToken === 'string' &&
        helper.fcmToken.trim().length > 0
      );
      
      console.log(`📤 Sending cancellation to ${otherHelpers.length} other helpers`);
      
      const cancellationNotifications: Promise<any>[] = [];
      
      // Enviar notificação de cancelamento para outros helpers
      for (const helper of otherHelpers) {
        const message = {
          token: helper.fcmToken,
          data: {
            type: 'emergency_cancelled',
            emergency_id: requestId,
            reason: 'another_helper_accepted'
          }
        };
        
        console.log('❌ Sending cancellation to helper:', {
          helper_id: helper.helperId,
          token: helper.fcmToken.substring(0, 20) + '...'
        });
        
        cancellationNotifications.push(admin.messaging().send(message));
      }
      
      try {
        await Promise.all(cancellationNotifications);
        console.log(`✅ Sent ${cancellationNotifications.length} cancellation notifications`);
      } catch (error) {
        console.error('❌ Error sending cancellation notifications:', error);
      }
    }
  });