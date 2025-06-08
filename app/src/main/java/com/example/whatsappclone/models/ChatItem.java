package com.example.whatsappclone.models;

public class ChatItem {
    private String uid;
    private String name;
    private String photoUrl;
    private String lastMessage;
    private long lastMessageTime;

    public ChatItem() {}

    public ChatItem(String uid, String name, String photoUrl, String lastMessage, long lastMessageTime) {
        this.uid = uid;
        this.name = name;
        this.photoUrl = photoUrl;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
    }

    public String getUid() { return uid; }
    public String getName() { return name; }
    public String getPhotoUrl() { return photoUrl; }
    public String getLastMessage() { return lastMessage; }
    public long getLastMessageTime() { return lastMessageTime; }
}


