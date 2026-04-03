package com.example.eventlottery.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;
import java.util.ArrayList;
import java.util.List;

public class Event {
    @DocumentId
    private String id;
    private String title;
    private String description;
    private String location;
    private String organizerId;
    private String organizerName;
    private List<String> tags;
    private long eventStartDate;
    private long eventEndDate;
    private long registrationOpenDate;
    private long registrationCloseDate;
    private int capacity;
    private int maxWaitingList; // 0 = unlimited
    private double price;
    private String posterUrl;
    private boolean privateEvent;
    private boolean requireGeolocation;
    private String status; // "open", "closed", "drawn", "completed"
    private String qrCodeContent; // deep link encoded in QR
    private int waitingListCount;
    private int acceptedCount;
    private long createdAt;
    private List<String> coOrganizerIds;

    // Status constants
    public static final String STATUS_OPEN = "open";
    public static final String STATUS_CLOSED = "closed";
    public static final String STATUS_DRAWN = "drawn";
    public static final String STATUS_COMPLETED = "completed";

    public Event() {
        this.tags = new ArrayList<>();
        this.coOrganizerIds = new ArrayList<>();
        this.status = STATUS_OPEN;
        this.createdAt = System.currentTimeMillis();
    }

    public Event(String title, String description, String location, String organizerId,
                 String organizerName, long eventStartDate, long eventEndDate,
                 long registrationOpenDate, long registrationCloseDate,
                 int capacity, int maxWaitingList, double price, String posterUrl,
                 boolean isPrivate, boolean requireGeolocation) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.organizerId = organizerId;
        this.organizerName = organizerName;
        this.eventStartDate = eventStartDate;
        this.eventEndDate = eventEndDate;
        this.registrationOpenDate = registrationOpenDate;
        this.registrationCloseDate = registrationCloseDate;
        this.capacity = capacity;
        this.maxWaitingList = maxWaitingList;
        this.price = price;
        this.posterUrl = posterUrl;
        this.privateEvent = isPrivate;
        this.requireGeolocation = requireGeolocation;
        this.tags = new ArrayList<>();
        this.coOrganizerIds = new ArrayList<>();
        this.status = STATUS_OPEN;
        this.createdAt = System.currentTimeMillis();
    }

    // Computed helpers
    public boolean isRegistrationOpen() {
        long now = System.currentTimeMillis();
        return now >= registrationOpenDate && now <= registrationCloseDate
                && STATUS_OPEN.equals(status);
    }

    public long getDaysLeftToRegister() {
        long now = System.currentTimeMillis();
        if (now > registrationCloseDate) return 0;
        return (registrationCloseDate - now) / (1000 * 60 * 60 * 24);
    }

    public String getFormattedPrice() {
        if (price <= 0) return "Free";
        return "$" + String.format("%.0f", price);
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    public String getOrganizerName() { return organizerName; }
    public void setOrganizerName(String organizerName) { this.organizerName = organizerName; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public long getEventStartDate() { return eventStartDate; }
    public void setEventStartDate(long eventStartDate) { this.eventStartDate = eventStartDate; }

    public long getEventEndDate() { return eventEndDate; }
    public void setEventEndDate(long eventEndDate) { this.eventEndDate = eventEndDate; }

    public long getRegistrationOpenDate() { return registrationOpenDate; }
    public void setRegistrationOpenDate(long registrationOpenDate) {
        this.registrationOpenDate = registrationOpenDate;
    }

    public long getRegistrationCloseDate() { return registrationCloseDate; }
    public void setRegistrationCloseDate(long registrationCloseDate) {
        this.registrationCloseDate = registrationCloseDate;
    }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getMaxWaitingList() { return maxWaitingList; }
    public void setMaxWaitingList(int maxWaitingList) { this.maxWaitingList = maxWaitingList; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    @PropertyName("private")
    public boolean isPrivate() { return privateEvent; }
    @PropertyName("private")
    public void setPrivate(boolean aPrivate) { this.privateEvent = aPrivate; }

    public boolean isRequireGeolocation() { return requireGeolocation; }
    public void setRequireGeolocation(boolean requireGeolocation) {
        this.requireGeolocation = requireGeolocation;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getQrCodeContent() { return qrCodeContent; }
    public void setQrCodeContent(String qrCodeContent) { this.qrCodeContent = qrCodeContent; }

    public int getWaitingListCount() { return waitingListCount; }
    public void setWaitingListCount(int waitingListCount) { this.waitingListCount = waitingListCount; }

    public int getAcceptedCount() { return acceptedCount; }
    public void setAcceptedCount(int acceptedCount) { this.acceptedCount = acceptedCount; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public List<String> getCoOrganizerIds() { return coOrganizerIds; }
    public void setCoOrganizerIds(List<String> coOrganizerIds) {
        this.coOrganizerIds = coOrganizerIds;
    }
}
