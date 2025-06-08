package com.example.whatsappclone.models;

public class MessageItem {
    private String senderId;
    private String message;
    private long timestamp;

    public MessageItem() {
        // Diperlukan Firestore
    }

    public MessageItem(String senderId, String message, long timestamp) {
        this.senderId = senderId;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // Method tambahan untuk cek apakah pesan ini dikirim oleh user saat ini
    public boolean isSent(String currentUserId) {
        return senderId != null && senderId.equals(currentUserId);
    }
}
