package com.example.eventlottery.models;

import com.google.firebase.firestore.DocumentId;

public class Comment {
    @DocumentId
    private String id;
    private String eventId;
    private String userId;
    private String userName;
    private String text;
    private long createdAt;

    public Comment() {
        this.createdAt = System.currentTimeMillis();
    }

    public Comment(String eventId, String userId, String userName, String text) {
        this.eventId = eventId;
        this.userId = userId;
        this.userName = userName;
        this.text = text;
        this.createdAt = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getInitials() {
        if (userName == null || userName.isEmpty()) return "?";
        String[] parts = userName.trim().split("\\s+");
        if (parts.length >= 2) {
            return String.valueOf(parts[0].charAt(0)).toUpperCase()
                    + String.valueOf(parts[1].charAt(0)).toUpperCase();
        }
        return String.valueOf(userName.charAt(0)).toUpperCase();
    }
}
