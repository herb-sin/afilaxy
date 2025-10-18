const {onDocumentCreated} = require("firebase-functions/v2/firestore");
const {onCall} = require("firebase-functions/v2/https");
const admin = require("firebase-admin");

admin.initializeApp();

// Função para enviar notificações push de emergência
exports.sendEmergencyNotification = onDocumentCreated(
    {
      document: "push_notifications/{notificationId}",
      region: "us-central1",
    },
    async (event) => {
      console.log("🚨 FUNÇÃO EXECUTADA - sendEmergencyNotification iniciada");

      const data = event.data.data();
      console.log("📋 Dados recebidos:", JSON.stringify(data, null, 2));

      try {
        console.log(`🔍 Buscando usuário: ${data.to}`);

        // Buscar token FCM do usuário destinatário
        const userDoc = await admin.firestore()
            .collection("users")
            .doc(data.to)
            .get();

        if (!userDoc.exists) {
          console.log("❌ Usuário não encontrado:", data.to);
          return null;
        }

        const userData = userDoc.data();
        console.log("👤 Dados do usuário:", JSON.stringify(userData, null, 2));

        const fcmToken = userData.fcmToken;
        if (!fcmToken) {
          console.log("⚠️ Token FCM não encontrado para usuário:", data.to);
          return null;
        }

        console.log(`🔑 Token FCM encontrado: ${fcmToken.substring(0, 20)}...`);

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

        console.log("📤 Enviando mensagem:", JSON.stringify(message, null, 2));

        // Enviar notificação
        const response = await admin.messaging().send(message);
        console.log("✅ Notificação enviada com sucesso:", response);

        // Marcar como processada
        await event.data.ref.update({
          processed: true,
          sentAt: admin.firestore.FieldValue.serverTimestamp(),
        });
      } catch (error) {
        console.error("❌ Erro ao enviar notificação:", error);
        console.error("❌ Stack trace:", error.stack);
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
  console.log("🔄 FUNÇÃO EXECUTADA - updateFCMToken iniciada");

  if (!request.auth) {
    console.log("❌ Usuário não autenticado");
    throw new Error("Usuário não autenticado");
  }

  const {token} = request.data;
  const userId = request.auth.uid;

  console.log(`📱 Atualizando token para usuário: ${userId}`);
  console.log(`🔑 Token: ${token ? token.substring(0, 20) + "..." : "null"}`);

  try {
    await admin.firestore()
        .collection("users")
        .doc(userId)
        .update({
          fcmToken: token,
          tokenUpdatedAt: admin.firestore.FieldValue.serverTimestamp(),
        });

    console.log(`✅ Token FCM atualizado com sucesso para: ${userId}`);
    return {success: true};
  } catch (error) {
    console.error("❌ Erro ao atualizar token FCM:", error);
    throw new Error("Erro ao atualizar token");
  }
});
