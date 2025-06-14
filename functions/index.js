// Ganti seluruh isi file functions/index.js Anda dengan kode di bawah ini

const {onDocumentCreated} = require("firebase-functions/v2/firestore");
const {setGlobalOptions} = require("firebase-functions/v2");
const admin = require("firebase-admin");

// Inisialisasi Firebase Admin SDK
admin.initializeApp();
const db = admin.firestore();

// (Praktik Terbaik) Tetapkan lokasi server untuk fungsi Anda
setGlobalOptions({ region: "asia-southeast1" }); // Contoh: Singapura

/**
 * PERBAIKAN: Menggunakan sintaks onDocumentCreated dari Functions v2
 *
 * Cloud Function yang terpicu setiap kali ada pesan BARU dibuat di dalam
 * sub-koleksi 'messages' di dalam koleksi 'chats'.
 */
exports.sendChatNotification = onDocumentCreated("chats/{chatId}/messages/{messageId}", async (event) => {
    // PERBAIKAN: Data sekarang ada di dalam event.data
    const snapshot = event.data;
    if (!snapshot) {
        console.log("Tidak ada data yang terkait dengan event.");
        return;
    }
    const messageData = snapshot.data();

    const senderId = messageData.senderId;
    // PERBAIKAN: Parameter sekarang ada di dalam event.params
    const chatId = event.params.chatId;

    console.log(`Pesan baru terdeteksi di chat: ${chatId} dari: ${senderId}`);

    // 1. Dapatkan info chat, terutama daftar partisipan
    const chatDoc = await db.collection("chats").doc(chatId).get();
    if (!chatDoc.exists) {
        console.log("Dokumen chat tidak ditemukan.");
        return null;
    }
    const participants = chatDoc.data().participants;

    // 2. Cari ID penerima (yang bukan pengirim)
    const receiverId = participants.find((id) => id !== senderId);
    if (!receiverId) {
        console.log("Penerima tidak ditemukan dalam partisipan.");
        return null;
    }
    console.log(`Penerima ditemukan: ${receiverId}`);

    // 3. Dapatkan info pengirim (untuk nama di notifikasi)
    const senderDoc = await db.collection("users").doc(senderId).get();
    if (!senderDoc.exists) {
        console.log("Dokumen pengirim tidak ditemukan.");
        return null;
    }
    const senderName = senderDoc.data().name;

    // 4. Dapatkan token FCM dari dokumen penerima
    const receiverDoc = await db.collection("users").doc(receiverId).get();
    if (!receiverDoc.exists) {
        console.log("Dokumen penerima tidak ditemukan.");
        return null;
    }
    const fcmToken = receiverDoc.data().fcmToken;

    if (!fcmToken) {
        console.log("Token FCM penerima tidak ditemukan.");
        return null;
    }

    // 5. Buat payload (isi) notifikasi
    const payload = {
        notification: {
            title: `Pesan baru dari ${senderName}`,
            body: messageData.message,
        },
    };

    // 6. Kirim notifikasi menggunakan FCM
    console.log(`Mengirim notifikasi ke token: ${fcmToken}`);
    try {
        const response = await admin.messaging().sendToDevice(fcmToken, payload);
        console.log("Notifikasi berhasil dikirim:", response);
        return response;
    } catch (error) {
        console.error("Gagal mengirim notifikasi:", error);
        return null;
    }
});
