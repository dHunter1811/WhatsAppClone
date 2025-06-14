package com.example.whatsappclone.shared.models;

import java.io.Serializable;
import java.util.List;

public class Group implements Serializable {
    private String groupId;
    private String groupName;
    private String groupPhotoUrl;
    private String createdBy;
    private List<String> participants;
    // Field di bawah ini untuk pembaruan di masa depan (daftar chat)
    private String lastMessage;
    private long lastMessageTime;

    public Group() {
        // Diperlukan untuk Firebase
    }

    public Group(String groupId, String groupName, String groupPhotoUrl, String createdBy, List<String> participants) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.groupPhotoUrl = groupPhotoUrl;
        this.createdBy = createdBy;
        this.participants = participants;
        this.lastMessage = "";
        this.lastMessageTime = 0;
    }

    // --- Buat Getter dan Setter untuk semua field ---
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public String getGroupPhotoUrl() { return groupPhotoUrl; }
    public void setGroupPhotoUrl(String groupPhotoUrl) { this.groupPhotoUrl = groupPhotoUrl; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }
    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public long getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(long lastMessageTime) { this.lastMessageTime = lastMessageTime; }
}
