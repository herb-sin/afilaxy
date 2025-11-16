"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.sendEmergencyNotification = void 0;
const functions = require("firebase-functions");
const admin = require("firebase-admin");
if (!admin.apps.length) {
    admin.initializeApp();
}
exports.sendEmergencyNotification = functions.firestore
    .document('emergency_requests/{requestId}')
    .onCreate(async (snap, context) => {
    var _a;
    const data = snap.data();
    const requestId = context.params.requestId;
    console.log('🔥 Firebase Function triggered:', {
        requestId,
        data: JSON.stringify(data)
    });
    if (!data.active)
        return;
    try {
        // Buscar helpers próximos
        const helpersSnapshot = await admin.firestore()
            .collection('helpers')
            .where('isActive', '==', true)
            .get();
        const nearbyHelpers = [];
        // Buscar helpers próximos e ordenar por distância
        for (const helperDoc of helpersSnapshot.docs) {
            const helper = helperDoc.data();
            const helperId = helper.id;
            // Pular o próprio usuário
            if (helperId === data.requesterId)
                continue;
            // Calcular distância
            const distance = calculateDistance(data.latitude, data.longitude, helper.location.latitude, helper.location.longitude);
            // Só considerar se estiver próximo (360m)
            if (distance <= 0.36) {
                // Buscar token FCM do helper
                const userDoc = await admin.firestore()
                    .collection('users')
                    .doc(helperId)
                    .get();
                const fcmToken = (_a = userDoc.data()) === null || _a === void 0 ? void 0 : _a.fcmToken;
                if (fcmToken) {
                    nearbyHelpers.push({ helper, helperId, distance, fcmToken });
                }
            }
        }
        // Ordenar por distância e pegar apenas os 3 mais próximos
        const closestHelpers = nearbyHelpers
            .sort((a, b) => a.distance - b.distance)
            .slice(0, 3);
        const notifications = [];
        // Enviar notificações apenas para os 3 mais próximos
        for (const { distance, fcmToken } of closestHelpers) {
            const message = {
                token: fcmToken,
                notification: {
                    title: '🆘 Emergência de Asma',
                    body: `Alguém precisa de ajuda a ${Math.round(distance * 1000)}m de você`
                },
                data: {
                    type: 'emergency_request',
                    emergency_id: requestId,
                    requesterName: data.requesterName || 'Alguém',
                    distance: Math.round(distance * 1000).toString()
                }
            };
            console.log('📨 Sending FCM to closest helper:', {
                token: fcmToken.substring(0, 20) + '...',
                emergency_id: requestId,
                distance: Math.round(distance * 1000) + 'm'
            });
            notifications.push(admin.messaging().send(message));
        }
        await Promise.all(notifications);
        console.log(`Enviadas ${notifications.length} notificações para os helpers mais próximos (máx 3, raio 360m)`);
    }
    catch (error) {
        console.error('Erro ao enviar notificações:', error);
    }
});
function calculateDistance(lat1, lon1, lat2, lon2) {
    const R = 6371; // km
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLon = (lon2 - lon1) * Math.PI / 180;
    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
}
//# sourceMappingURL=sendNotification.js.map