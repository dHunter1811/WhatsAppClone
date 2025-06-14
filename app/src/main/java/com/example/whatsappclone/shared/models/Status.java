package com.example.whatsappclone.shared.models;

import java.io.Serializable;
import java.util.List;

public class Status implements Serializable {
    // PASTIKAN NAMA-NAMA INI SAMA PERSIS DENGAN FIELD DI FIRESTORE
    private String userId;
    private String userName;
    private String profileImageUrl; // Perhatikan 'U', 'r', 'l'
    private long lastUpdated;
    private List<Story> stories;

    public Status() {
        // Diperlukan untuk Firebase
    }

    public Status(String userId, String userName, String profileImageUrl, long lastUpdated, List<Story> stories) {
        this.userId = userId;
        this.userName = userName;
        this.profileImageUrl = profileImageUrl;
        this.lastUpdated = lastUpdated;
        this.stories = stories;
    }

    // Pastikan semua Getter dan Setter juga ada
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }
    public List<Story> getStories() { return stories; }
    public void setStories(List<Story> stories) { this.stories = stories; }
}
