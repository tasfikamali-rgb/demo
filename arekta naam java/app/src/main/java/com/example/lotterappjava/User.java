package com.example.lotterappjava;

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

    public User() {
        // Default constructor for Firebase
    }

    public User(String deviceId) {
        this.deviceId = deviceId;
        this.notificationsEnabled = true;
        this.role = ROLE_ENTRANT;
    }

    public User(String deviceId, String role) {
        this.deviceId = deviceId;
        this.role = role != null ? role : ROLE_ENTRANT;
        this.notificationsEnabled = true;
    }

    // Getters and Setters
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
