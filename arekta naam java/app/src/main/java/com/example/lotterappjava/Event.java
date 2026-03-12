package com.example.lotterappjava;

import java.util.Date;

public class Event {
    private String eventId;
    private String organizerId;
    private String title;
    private String description;
    private Date eventDate;
    private Date eventStartTime;
    private Date eventEndTime;
    private String location;
    private int capacity;
    private double price; // Added for story requirement
    private Date registrationStart;
    private Date registrationEnd;
    private String posterUrl;
    private String qrCodeUrl;
    private boolean geolocationRequired;
    private Integer maxWaitingListEntrants;

    public Event() {
        // Default constructor for Firebase
    }

    public Event(String eventId, String organizerId) {
        this.eventId = eventId;
        this.organizerId = organizerId;
    }

    // Getters and Setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getEventDate() { return eventDate; }
    public void setEventDate(Date eventDate) { this.eventDate = eventDate; }

    public Date getEventStartTime() { return eventStartTime; }
    public void setEventStartTime(Date eventStartTime) { this.eventStartTime = eventStartTime; }

    public Date getEventEndTime() { return eventEndTime; }
    public void setEventEndTime(Date eventEndTime) { this.eventEndTime = eventEndTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public Date getRegistrationStart() { return registrationStart; }
    public void setRegistrationStart(Date registrationStart) { this.registrationStart = registrationStart; }

    public Date getRegistrationEnd() { return registrationEnd; }
    public void setRegistrationEnd(Date registrationEnd) { this.registrationEnd = registrationEnd; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public String getQrCodeUrl() { return qrCodeUrl; }
    public void setQrCodeUrl(String qrCodeUrl) { this.qrCodeUrl = qrCodeUrl; }

    public boolean isGeolocationRequired() { return geolocationRequired; }
    public void setGeolocationRequired(boolean geolocationRequired) { this.geolocationRequired = geolocationRequired; }

    public Integer getMaxWaitingListEntrants() { return maxWaitingListEntrants; }
    public void setMaxWaitingListEntrants(Integer maxWaitingListEntrants) { this.maxWaitingListEntrants = maxWaitingListEntrants; }
}
