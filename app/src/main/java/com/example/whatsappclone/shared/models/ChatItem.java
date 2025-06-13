package com.example.whatsappclone.shared.models;

public class ChatItem {
    private String uid;
    private String name;
    private String photoUrl;
    private String lastMessage;
    private long lastMessageTime;

    public ChatItem(String uid, String name, String photoUrl, String lastMessage, long lastMessageTime) {
        this.uid = uid;
        this.name = name;
        this.photoUrl = photoUrl;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }
}


