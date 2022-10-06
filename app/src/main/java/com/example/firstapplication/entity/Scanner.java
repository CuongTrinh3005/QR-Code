package com.example.firstapplication.entity;

public class Scanner {
    private String googleId;
    private String displayName;
    private String email;
    private String imageUrl;

    public Scanner(String googleId, String displayName, String email) {
        this.googleId = googleId;
        this.displayName = displayName;
        this.email = email;
    }

    public Scanner(String googleId, String displayName, String email, String imageUrl) {
        this.googleId = googleId;
        this.displayName = displayName;
        this.email = email;
        this.imageUrl = imageUrl;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
