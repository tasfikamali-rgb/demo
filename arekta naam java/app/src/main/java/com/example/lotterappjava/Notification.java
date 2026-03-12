package com.example.lotterappjava;

import java.util.Date;

public class Notification {
    private String notificationId;
    private String eventId;
    private String organizerId;
    private String message;
    private String userId; // Can be null for group notifications
    private String targetGroup; // "all", "waiting", "invited", "enrolled", "cancelled"
    private Date timestamp;

    public Notification() {
        // Default constructor for Firebase
    }

    public Notification(String notificationId, String eventId, String organizerId, String message, String userId, String targetGroup) {
        this.notificationId = notificationId;
        this.eventId = eventId;
        this.organizerId = organizerId;
        this.message = message;
        this.userId = userId;
        this.targetGroup = targetGroup;
        this.timestamp = new Date();
    }

    // Getters and Setters
    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTargetGroup() { return targetGroup; }
    public void setTargetGroup(String targetGroup) { this.targetGroup = targetGroup; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}