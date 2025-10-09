const {onDocumentCreated} = require("firebase-functions/v2/firestore");
const {onCall} = require("firebase-functions/v2/https");
const admin = require("firebase-admin");

admin.initializeApp();

// Função para enviar notificações push de emergência
exports.sendEmergencyNotification = onDocumentCreated(
    "push_notifications/{notificationId}",
    async (event) => {
      const data = event.data.data();

      try {
        // Buscar token FCM do usuário destinatário
        const userDoc = await admin.firestore()
            .collection("users")
            .doc(data.to)
            .get();

        if (!userDoc.exists) {
          console.log("Usuário não encontrado:", data.to);
          return null;
        }

        const fcmToken = userDoc.data().fcmToken;
        if (!fcmToken) {
          console.log("Token FCM não encontrado para usuário:", data.to);
          return null;
        }

        // Configurar mensagem de emergência
        const message = {
          token: fcmToken,
          notification: {
            title: data.data.title,
            body: data.data.body,
          },
          data: {
            type: data.data.type,
            emergencyId: data.data.emergencyId,
            requesterName: data.data.requesterName,
          },
          android: {
            priority: "high",
            notification: {
              priority: "max",
              defaultSound: true,
              defaultVibrateTimings: true,
              channelId: "afilaxy_emergency",
            },
          },
        };

        // Enviar notificação
        const response = await admin.messaging().send(message);
        console.log("Notificação enviada com sucesso:", response);

        // Marcar como processada
        await event.data.ref.update({
          processed: true,
          sentAt: admin.firestore.FieldValue.serverTimestamp(),
        });
      } catch (error) {
        console.error("Erro ao enviar notificação:", error);
        await event.data.ref.update({
          error: error.message,
          processed: false,
        });
      }

      return null;
    },
);

// Função para atualizar token FCM do usuário
exports.updateFCMToken = onCall(async (request) => {
  if (!request.auth) {
    throw new Error("Usuário não autenticado");
  }

  const {token} = request.data;
  const userId = request.auth.uid;

  try {
    await admin.firestore()
        .collection("users")
        .doc(userId)
        .update({
          fcmToken: token,
          tokenUpdatedAt: admin.firestore.FieldValue.serverTimestamp(),
        });

    return {success: true};
  } catch (error) {
    console.error("Erro ao atualizar token FCM:", error);
    throw new Error("Erro ao atualizar token");
  }
});
