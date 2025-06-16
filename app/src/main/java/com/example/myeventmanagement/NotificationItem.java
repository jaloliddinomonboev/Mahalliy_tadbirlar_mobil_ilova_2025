package com.example.myeventmanagement;

public class NotificationItem {
    private String title;
    private String body;
    private Long timestamp;

    public NotificationItem(String title, String body, Long timestamp) {
        this.title = title;
        this.body = body;
        this.timestamp = timestamp;
    }

    public String getTitle() { return title; }
    public String getBody() { return body; }
    public Long getTimestamp() { return timestamp; }
}