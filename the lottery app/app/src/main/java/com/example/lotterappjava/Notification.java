package com.example.lotterappjava;

import java.util.Date;

/**
 * The Notification model class representing a message sent to users.
 * This class follows the MVC design pattern by acting as a data holder for notification-related information.
 *
 * Outstanding issues:
 * - None currently identified.
 */
public class Notification {
    private String notificationId;
    private String eventId;
    private String organizerId;
    private String message;
    private String userId;
    private String targetGroup; // "all", "waiting", "invited", "enrolled", "cancelled"
    private Date timestamp;

    /**
     * Default constructor for Firebase database operations.
     */
    public Notification() {
        // Default constructor for Firebase
    }

    /**
     * Constructor for creating a notification with specific details.
     *
     * @param notificationId The unique identifier for the notification.
     * @param eventId The identifier of the event the notification is related to.
     * @param organizerId The identifier of the organizer who sent the notification.
     * @param message The content of the notification.
     * @param userId The identifier of the recipient user (may be null for group notifications).
     * @param targetGroup The group of users targeted by the notification (e.g., "all", "waiting").
     */
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

    /**
     * @return The notification identifier.
     */
    public String getNotificationId() { return notificationId; }

    /**
     * @param notificationId The notification identifier to set.
     */
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }

    /**
     * @return The event identifier related to this notification.
     */
    public String getEventId() { return eventId; }

    /**
     * @param eventId The event identifier to set.
     */
    public void setEventId(String eventId) { this.eventId = eventId; }

    /**
     * @return The organizer identifier who sent the notification.
     */
    public String getOrganizerId() { return organizerId; }

    /**
     * @param organizerId The organizer identifier to set.
     */
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    /**
     * @return The notification message.
     */
    public String getMessage() { return message; }

    /**
     * @param message The message to set.
     */
    public void setMessage(String message) { this.message = message; }

    /**
     * @return The identifier of the recipient user.
     */
    public String getUserId() { return userId; }

    /**
     * @param userId The recipient user identifier to set.
     */
    public void setUserId(String userId) { this.userId = userId; }

    /**
     * @return The target group of the notification.
     */
    public String getTargetGroup() { return targetGroup; }

    /**
     * @param targetGroup The target group to set.
     */
    public void setTargetGroup(String targetGroup) { this.targetGroup = targetGroup; }

    /**
     * @return The timestamp when the notification was created.
     */
    public Date getTimestamp() { return timestamp; }

    /**
     * @param timestamp The timestamp to set.
     */
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
