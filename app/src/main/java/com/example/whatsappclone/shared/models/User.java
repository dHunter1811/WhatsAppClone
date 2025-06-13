package com.example.whatsappclone.shared.models;

import java.io.Serializable;

public class User implements Serializable {

    // Nama variabel dibuat konsisten
    private String userId;
    private String name;
    private String profileImageUrl;

    public User() {
        // Diperlukan untuk Firebase
    }

    public User(String userId, String name, String profileImageUrl) {
        this.userId = userId;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
    }

    // --- GETTER DAN SETTER DIPERBAIKI AGAR KONSISTEN ---

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    // Nama setter ini sekarang cocok dengan field di Firestore
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
