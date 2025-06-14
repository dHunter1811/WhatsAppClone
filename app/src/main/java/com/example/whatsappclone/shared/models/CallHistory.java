package com.example.whatsappclone.shared.models;

public class CallHistory {
    private String historyId;
    private String otherUserId;
    private String otherUserName; // Bisa null jika outgoing
    private String otherUserPhotoUrl; // Bisa null jika outgoing
    private long timestamp;
    private String callType; // "video" atau "voice"
    private String callDirection; // "incoming" atau "outgoing"

    public CallHistory() {}

    public CallHistory(String historyId, String otherUserId, String otherUserName, String otherUserPhotoUrl, long timestamp, String callType, String callDirection) {
        this.historyId = historyId;
        this.otherUserId = otherUserId;
        this.otherUserName = otherUserName;
        this.otherUserPhotoUrl = otherUserPhotoUrl;
        this.timestamp = timestamp;
        this.callType = callType;
        this.callDirection = callDirection;
    }

    public String getHistoryId() {
        return historyId;
    }

    public void setHistoryId(String historyId) {
        this.historyId = historyId;
    }

    public String getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(String otherUserId) {
        this.otherUserId = otherUserId;
    }

    public String getOtherUserName() {
        return otherUserName;
    }

    public void setOtherUserName(String otherUserName) {
        this.otherUserName = otherUserName;
    }

    public String getOtherUserPhotoUrl() {
        return otherUserPhotoUrl;
    }

    public void setOtherUserPhotoUrl(String otherUserPhotoUrl) {
        this.otherUserPhotoUrl = otherUserPhotoUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public String getCallDirection() {
        return callDirection;
    }

    public void setCallDirection(String callDirection) {
        this.callDirection = callDirection;
    }
// --- Buat Getter dan Setter untuk semua field ---
    // (Klik kanan -> Generate -> Getter and Setter)
}
