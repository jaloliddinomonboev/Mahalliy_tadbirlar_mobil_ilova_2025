package com.example.myeventmanagement;

public class UploadEvent {
    private String eventId; // eventId uchun yangi maydon
    private String eventType, date, design, budget, expDesign, name, contact, eventVenue;
    private String uploadImage;
    private long timestamp;

    public UploadEvent() {
    }

    public UploadEvent(String eventType, String date, String design, String budget, String expDesign, String name, String contact, String eventVenue, String uploadImage) {
        this.eventType = eventType;
        this.date = date;
        this.design = design;
        this.budget = budget;
        this.expDesign = expDesign;
        this.name = name;
        this.contact = contact;
        this.eventVenue = eventVenue;
        this.uploadImage = uploadImage;
        this.timestamp = timestamp;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDesign() {
        return design;
    }

    public void setDesign(String design) {
        this.design = design;
    }

    public String getBudget() {
        return budget;
    }

    public void setBudget(String budget) {
        this.budget = budget;
    }

    public String getExpDesign() {
        return expDesign;
    }

    public void setExpDesign(String expDesign) {
        this.expDesign = expDesign;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getEventVenue() {
        return eventVenue;
    }

    public void setEventVenue(String eventVenue) {
        this.eventVenue = eventVenue;
    }

    public String getUploadImage() {
        return uploadImage;
    }

    public void setUploadImage(String uploadImage) {
        this.uploadImage = uploadImage;
    }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}