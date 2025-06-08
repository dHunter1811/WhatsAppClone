package com.example.whatsappclone.models;

public class User {
    private String uid;
    private String name;
    private String photoUrl;

    public User() {
        // Diperlukan untuk Firebase
    }

    public User(String uid, String name, String photoUrl) {
        this.uid = uid;
        this.name = name;
        this.photoUrl = photoUrl;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
}
