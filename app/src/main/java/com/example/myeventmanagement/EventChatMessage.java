package com.example.myeventmanagement;

public class EventChatMessage {
    private String userId;
    private String message;
    private long timestamp;
    private String userName;      // Foydalanuvchi ismi
    private String profileImage;  // Profil rasm URL’si

    public EventChatMessage() {}

    public EventChatMessage(String userId, String message, long timestamp, String userName, String profileImage) {
        this.userId = userId;
        this.message = message;
        this.timestamp = timestamp;
        this.userName = userName;
        this.profileImage = profileImage;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
}