"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.cancelOtherHelpers = void 0;
const functions = require("firebase-functions");
const admin = require("firebase-admin");
if (!admin.apps.length) {
    admin.initializeApp();
}
exports.cancelOtherHelpers = functions.firestore
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
        const otherHelpers = notifiedHelpers.filter((helper) => helper.helperId !== acceptedHelperId &&
            typeof helper.fcmToken === 'string' &&
            helper.fcmToken.trim().length > 0);
        console.log(`📤 Sending cancellation to ${otherHelpers.length} other helpers`);
        const cancellationNotifications = [];
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
        }
        catch (error) {
            console.error('❌ Error sending cancellation notifications:', error);
        }
    }
});
//# sourceMappingURL=cancelOtherHelpers.js.map