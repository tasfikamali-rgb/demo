package com.example.lotterappjava;

import java.util.Date;

public class Notification {
    private String notificationId;
    private String eventId;
    private String message;
    private String userId;
    private Date timestamp;

    public Notification() {
        // Default constructor for Firebase
    }

    public Notification(String notificationId, String eventId, String message, String userId) {
        this.notificationId = notificationId;
        this.eventId = eventId;
        this.message = message;
        this.userId = userId;
        this.timestamp = new Date();
    }

    // Getters and Setters
    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}