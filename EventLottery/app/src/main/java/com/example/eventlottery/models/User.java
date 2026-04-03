package com.example.eventlottery.models;

import com.google.firebase.firestore.DocumentId;
import java.util.ArrayList;
import java.util.List;

public class User {
    @DocumentId
    private String id;
    private String name;
    private String nameLowercase; // For case-insensitive search
    private String email;
    private String phone;
    private List<String> roles; // "entrant", "organizer", "admin"
    private boolean pushNotificationsEnabled;
    private String deviceId;
    private String fcmToken;
    private long createdAt;

    public User() {
        this.roles = new ArrayList<>();
        this.pushNotificationsEnabled = true;
        this.createdAt = System.currentTimeMillis();
    }

    public User(String name, String email, String phone, List<String> roles, String deviceId) {
        this.name = name;
        this.setName(name); // Use setter to populate nameLowercase
        this.email = email != null ? email.toLowerCase() : null;
        this.phone = phone;
        this.roles = roles != null ? roles : new ArrayList<>();
        this.deviceId = deviceId;
        this.pushNotificationsEnabled = true;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
        this.nameLowercase = name != null ? name.toLowerCase() : null;
    }

    public String getNameLowercase() { return nameLowercase; }
    public void setNameLowercase(String nameLowercase) { this.nameLowercase = nameLowercase; }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        this.email = email != null ? email.toLowerCase() : null;
    }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public boolean isPushNotificationsEnabled() { return pushNotificationsEnabled; }
    public void setPushNotificationsEnabled(boolean pushNotificationsEnabled) {
        this.pushNotificationsEnabled = pushNotificationsEnabled;
    }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    public String getInitials() {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return String.valueOf(parts[0].charAt(0)).toUpperCase()
                    + String.valueOf(parts[1].charAt(0)).toUpperCase();
        }
        return String.valueOf(name.charAt(0)).toUpperCase();
    }
}
