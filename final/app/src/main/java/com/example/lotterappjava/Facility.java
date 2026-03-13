package com.example.lotterappjava;

/**
 * The Facility model class representing a venue or location where events are held.
 * This class serves as a data holder for facility information, following the MVC design pattern.
 *
 * Outstanding issues:
 * - None currently identified.
 */
public class Facility {
    private String organizerId;
    private String name;
    private String location;
    private String imageUrl;

    /**
     * Default constructor for Firebase database operations.
     */
    public Facility() {}

    /**
     * Constructor for creating a facility with specified details.
     *
     * @param organizerId The unique identifier of the organizer who owns or manages the facility.
     * @param name The name of the facility.
     * @param location The address or description of the facility's location.
     */
    public Facility(String organizerId, String name, String location) {
        this.organizerId = organizerId;
        this.name = name;
        this.location = location;
    }

    // Getters and Setters

    /**
     * @return The identifier of the organizer.
     */
    public String getOrganizerId() { return organizerId; }

    /**
     * @param organizerId The identifier of the organizer to set.
     */
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    /**
     * @return The name of the facility.
     */
    public String getName() { return name; }

    /**
     * @param name The facility name to set.
     */
    public void setName(String name) { this.name = name; }

    /**
     * @return The location of the facility.
     */
    public String getLocation() { return location; }

    /**
     * @param location The facility location to set.
     */
    public void setLocation(String location) { this.location = location; }

    /**
     * @return The URL of the image representing the facility.
     */
    public String getImageUrl() { return imageUrl; }

    /**
     * @param imageUrl The image URL to set.
     */
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
