package com.example.lotterappjava;

/**
 * The User model class representing a user within the application.
 * This class follows the Model-View-Controller (MVC) design pattern by serving as a data holder
 * for user information including identification, contact details, and role.
 *
 * Outstanding issues:
 * - Password is currently stored as plain text, which is insecure.
 */
public class User {
    public static final String ROLE_ENTRANT = "entrant";
    public static final String ROLE_ORGANIZER = "organizer";
    public static final String ROLE_ADMIN = "admin";

    private String deviceId;
    private String uid;           // Firebase Auth UID when signed in with email
    private String role;          // entrant | organizer | admin
    private String name;
    private String email;
    private String phoneNumber;
    private boolean notificationsEnabled;
    private String profileImageUrl;
    private String fcmToken;
    private String password;      // Stored as plain text (INSECURE - for database viewing only)

    /**
     * Default constructor for Firebase database operations.
     */
    public User() {
        // Default constructor for Firebase
    }

    /**
     * Constructor for creating a user with a specific device identifier.
     * Defaults role to ROLE_ENTRANT and enables notifications.
     *
     * @param deviceId The unique device identifier.
     */
    public User(String deviceId) {
        this.deviceId = deviceId;
        this.notificationsEnabled = true;
        this.role = ROLE_ENTRANT;
    }

    /**
     * Constructor for creating a user with a specific device identifier and role.
     * Defaults notifications to enabled.
     *
     * @param deviceId The unique device identifier.
     * @param role The role of the user (e.g., entrant, organizer, admin).
     */
    public User(String deviceId, String role) {
        this.deviceId = deviceId;
        this.role = role != null ? role : ROLE_ENTRANT;
        this.notificationsEnabled = true;
    }

    // Getters and Setters

    /**
     * @return The device identifier.
     */
    public String getDeviceId() { return deviceId; }

    /**
     * @param deviceId The device identifier to set.
     */
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    /**
     * @return The Firebase Auth UID.
     */
    public String getUid() { return uid; }

    /**
     * @param uid The Firebase Auth UID to set.
     */
    public void setUid(String uid) { this.uid = uid; }

    /**
     * @return The user's role.
     */
    public String getRole() { return role; }

    /**
     * @param role The user's role to set.
     */
    public void setRole(String role) { this.role = role; }

    /**
     * @return The user's name.
     */
    public String getName() { return name; }

    /**
     * @param name The user's name to set.
     */
    public void setName(String name) { this.name = name; }

    /**
     * @return The user's email address.
     */
    public String getEmail() { return email; }

    /**
     * @param email The user's email address to set.
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * @return The user's phone number.
     */
    public String getPhoneNumber() { return phoneNumber; }

    /**
     * @param phoneNumber The user's phone number to set.
     */
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    /**
     * @return True if notifications are enabled, false otherwise.
     */
    public boolean isNotificationsEnabled() { return notificationsEnabled; }

    /**
     * @param notificationsEnabled Boolean value to set notification preference.
     */
    public void setNotificationsEnabled(boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }

    /**
     * @return The URL of the user's profile image.
     */
    public String getProfileImageUrl() { return profileImageUrl; }

    /**
     * @param profileImageUrl The URL of the profile image to set.
     */
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    /**
     * @return The Firebase Cloud Messaging token.
     */
    public String getFcmToken() { return fcmToken; }

    /**
     * @param fcmToken The FCM token to set.
     */
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }

    /**
     * @return The user's password (plain text).
     */
    public String getPassword() { return password; }

    /**
     * @param password The password to set.
     */
    public void setPassword(String password) { this.password = password; }
}
