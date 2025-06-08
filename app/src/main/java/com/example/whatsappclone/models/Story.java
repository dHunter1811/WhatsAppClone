package com.example.whatsappclone.models;

import java.io.Serializable;

public class Story implements Serializable { // PASTIKAN IMPLEMENTS INI ADA

    private String imageUrl;
    private long timestamp;

    public Story() {
        // Diperlukan untuk Firebase
    }

    public Story(String imageUrl, long timestamp) {
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
