package com.example.eventlottery.models;

import com.google.firebase.firestore.DocumentId;

public class AppNotification {
    @DocumentId
    private String id;
    private String userId;
    private String eventId;
    private String eventTitle;
    private String type;
    private String title;
    private String message;
    private boolean read;           // field name "read" in Firestore (NOT "isRead")
    private boolean actionRequired;
    private long createdAt;
    private String senderName;
    private int recipientCount;

    public static final String TYPE_WIN          = "win";
    public static final String TYPE_LOSS         = "loss";
    public static final String TYPE_INVITATION   = "invitation";
    public static final String TYPE_UPDATE       = "update";
    public static final String TYPE_CO_ORGANIZER = "co_organizer";
    public static final String TYPE_CANCELLED    = "cancelled";

    public AppNotification() {
        this.createdAt = System.currentTimeMillis();
        this.read = false;
    }

    public AppNotification(String userId, String eventId, String eventTitle,
                           String type, String title, String message, boolean actionRequired) {
        this.userId        = userId;
        this.eventId       = eventId;
        this.eventTitle    = eventTitle;
        this.type          = type;
        this.title         = title;
        this.message       = message;
        this.actionRequired = actionRequired;
        this.read          = false;
        this.createdAt     = System.currentTimeMillis();
    }

    public String getRelativeTime() {
        long diff = System.currentTimeMillis() - createdAt;
        long minutes = diff / 60000;
        if (minutes < 1) return "just now";
        if (minutes < 60) return minutes + "m";
        long hours = minutes / 60;
        if (hours < 24) return hours + "h";
        return (hours / 24) + "d";
    }

    public String getId()                              { return id; }
    public void   setId(String id)                     { this.id = id; }
    public String getUserId()                          { return userId; }
    public void   setUserId(String userId)             { this.userId = userId; }
    public String getEventId()                         { return eventId; }
    public void   setEventId(String eventId)           { this.eventId = eventId; }
    public String getEventTitle()                      { return eventTitle; }
    public void   setEventTitle(String t)              { this.eventTitle = t; }
    public String getType()                            { return type; }
    public void   setType(String type)                 { this.type = type; }
    public String getTitle()                           { return title; }
    public void   setTitle(String title)               { this.title = title; }
    public String getMessage()                         { return message; }
    public void   setMessage(String message)           { this.message = message; }
    // isRead() is the correct Java-bean getter for a boolean field named "read"
    public boolean isRead()                            { return read; }
    public void    setRead(boolean read)               { this.read = read; }
    public boolean isActionRequired()                  { return actionRequired; }
    public void    setActionRequired(boolean v)        { this.actionRequired = v; }
    public long   getCreatedAt()                       { return createdAt; }
    public void   setCreatedAt(long createdAt)         { this.createdAt = createdAt; }
    public String getSenderName()                      { return senderName; }
    public void   setSenderName(String senderName)     { this.senderName = senderName; }
    public int    getRecipientCount()                  { return recipientCount; }
    public void   setRecipientCount(int recipientCount){ this.recipientCount = recipientCount; }
}
