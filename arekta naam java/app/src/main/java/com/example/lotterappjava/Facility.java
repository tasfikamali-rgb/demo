package com.example.lotterappjava;

public class Facility {
    private String organizerId;
    private String name;
    private String location;
    private String imageUrl;

    public Facility() {}

    public Facility(String organizerId, String name, String location) {
        this.organizerId = organizerId;
        this.name = name;
        this.location = location;
    }

    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
