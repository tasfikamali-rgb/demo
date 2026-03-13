package com.example.lotterappjava;

import java.util.Date;

/**
 * The Event model class representing an event organized in the application.
 * This class serves as a data holder for event-specific information, following the MVC design pattern.
 *
 * Outstanding issues:
 * - None currently identified.
 */
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

    /**
     * Default constructor for Firebase database operations.
     */
    public Event() {
        // Default constructor for Firebase
    }

    /**
     * Constructor for creating an event with a specific event ID and organizer ID.
     *
     * @param eventId The unique identifier for the event.
     * @param organizerId The unique identifier for the organizer who created the event.
     */
    public Event(String eventId, String organizerId) {
        this.eventId = eventId;
        this.organizerId = organizerId;
    }

    // Getters and Setters

    /**
     * @return The event identifier.
     */
    public String getEventId() { return eventId; }

    /**
     * @param eventId The event identifier to set.
     */
    public void setEventId(String eventId) { this.eventId = eventId; }

    /**
     * @return The identifier of the organizer.
     */
    public String getOrganizerId() { return organizerId; }

    /**
     * @param organizerId The identifier of the organizer to set.
     */
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    /**
     * @return The title of the event.
     */
    public String getTitle() { return title; }

    /**
     * @param title The event title to set.
     */
    public void setTitle(String title) { this.title = title; }

    /**
     * @return The description of the event.
     */
    public String getDescription() { return description; }

    /**
     * @param description The event description to set.
     */
    public void setDescription(String description) { this.description = description; }

    /**
     * @return The date of the event.
     */
    public Date getEventDate() { return eventDate; }

    /**
     * @param eventDate The date of the event to set.
     */
    public void setEventDate(Date eventDate) { this.eventDate = eventDate; }

    /**
     * @return The start time of the event.
     */
    public Date getEventStartTime() { return eventStartTime; }

    /**
     * @param eventStartTime The start time of the event to set.
     */
    public void setEventStartTime(Date eventStartTime) { this.eventStartTime = eventStartTime; }

    /**
     * @return The end time of the event.
     */
    public Date getEventEndTime() { return eventEndTime; }

    /**
     * @param eventEndTime The end time of the event to set.
     */
    public void setEventEndTime(Date eventEndTime) { this.eventEndTime = eventEndTime; }

    /**
     * @return The location of the event.
     */
    public String getLocation() { return location; }

    /**
     * @param location The event location to set.
     */
    public void setLocation(String location) { this.location = location; }

    /**
     * @return The capacity of the event.
     */
    public int getCapacity() { return capacity; }

    /**
     * @param capacity The event capacity to set.
     */
    public void setCapacity(int capacity) { this.capacity = capacity; }
    
    /**
     * @return The price of the event.
     */
    public double getPrice() { return price; }

    /**
     * @param price The event price to set.
     */
    public void setPrice(double price) { this.price = price; }

    /**
     * @return The registration start date.
     */
    public Date getRegistrationStart() { return registrationStart; }

    /**
     * @param registrationStart The registration start date to set.
     */
    public void setRegistrationStart(Date registrationStart) { this.registrationStart = registrationStart; }

    /**
     * @return The registration end date.
     */
    public Date getRegistrationEnd() { return registrationEnd; }

    /**
     * @param registrationEnd The registration end date to set.
     */
    public void setRegistrationEnd(Date registrationEnd) { this.registrationEnd = registrationEnd; }

    /**
     * @return The URL of the event poster.
     */
    public String getPosterUrl() { return posterUrl; }

    /**
     * @param posterUrl The URL of the event poster to set.
     */
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    /**
     * @return The URL of the QR code for the event.
     */
    public String getQrCodeUrl() { return qrCodeUrl; }

    /**
     * @param qrCodeUrl The URL of the QR code to set.
     */
    public void setQrCodeUrl(String qrCodeUrl) { this.qrCodeUrl = qrCodeUrl; }

    /**
     * @return True if geolocation is required for registration, false otherwise.
     */
    public boolean isGeolocationRequired() { return geolocationRequired; }

    /**
     * @param geolocationRequired Boolean to set if geolocation is required.
     */
    public void setGeolocationRequired(boolean geolocationRequired) { this.geolocationRequired = geolocationRequired; }

    /**
     * @return The maximum number of entrants on the waiting list.
     */
    public Integer getMaxWaitingListEntrants() { return maxWaitingListEntrants; }

    /**
     * @param maxWaitingListEntrants The maximum number of entrants to set for the waiting list.
     */
    public void setMaxWaitingListEntrants(Integer maxWaitingListEntrants) { this.maxWaitingListEntrants = maxWaitingListEntrants; }
}
