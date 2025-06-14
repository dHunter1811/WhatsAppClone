package com.example.whatsappclone.shared.models;

public class Message {

    private String messageId;
    private String senderId;
    private String message;
    private long timestamp;
    private boolean read; // <-- FIELD BARU

    public Message() {
        // Diperlukan untuk Firebase
    }

    public Message(String messageId, String senderId, String message, long timestamp) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.message = message;
        this.timestamp = timestamp;
        this.read = false; // <-- Nilai default saat dibuat
    }

    // --- Getter dan Setter lainnya ... ---

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    // --- Getter dan Setter untuk field baru ---
    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
