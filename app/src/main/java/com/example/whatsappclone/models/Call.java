package com.example.whatsappclone.models;

public class Call {
    private String callId; // Akan sama dengan channelName
    private String callerId;
    private String callerName;
    private String callerPhotoUrl;
    private String receiverId;
    private String callType; // "video" atau "voice"
    private String status;   // "dialing", "accepted", "declined", "ended"
    private long timestamp;

    public Call() {
        // Diperlukan untuk Firebase
    }

    public Call(String callId, String callerId, String callerName, String callerPhotoUrl, String receiverId, String callType, String status, long timestamp) {
        this.callId = callId;
        this.callerId = callerId;
        this.callerName = callerName;
        this.callerPhotoUrl = callerPhotoUrl;
        this.receiverId = receiverId;
        this.callType = callType;
        this.status = status;
        this.timestamp = timestamp;
    }

    // --- Buat Getter dan Setter untuk semua properti di atas ---
    // (Klik kanan -> Generate -> Getter and Setter -> Select All)

    public String getCallId() { return callId; }
    public void setCallId(String callId) { this.callId = callId; }
    public String getCallerId() { return callerId; }
    public void setCallerId(String callerId) { this.callerId = callerId; }
    public String getCallerName() { return callerName; }
    public void setCallerName(String callerName) { this.callerName = callerName; }
    public String getCallerPhotoUrl() { return callerPhotoUrl; }
    public void setCallerPhotoUrl(String callerPhotoUrl) { this.callerPhotoUrl = callerPhotoUrl; }
    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }
    public String getCallType() { return callType; }
    public void setCallType(String callType) { this.callType = callType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
