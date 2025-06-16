package com.example.myeventmanagement;

public class ChatMessage {
    private String messageId;
    private String senderId;
    private String senderName;
    private String text;
    private String senderProfile;
    private String senderRole;
    private long timestamp;

    public ChatMessage() {}

    public ChatMessage(String messageId, String senderId, String senderName, String senderProfile, String senderRole, String text, long timestamp) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderProfile = (senderProfile != null) ? senderProfile : "";
        this.senderRole = (senderRole != null) ? senderRole : "user";
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSenderProfile() {
        return senderProfile;
    }

    public void setSenderProfile(String senderProfile) {
        this.senderProfile = senderProfile;
    }

    public String getSenderRole() {
        return senderRole;
    }

    public void setSenderRole(String senderRole) {
        this.senderRole = senderRole;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}