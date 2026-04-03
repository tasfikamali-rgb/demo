package com.example.eventlottery.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.DocumentId;

/**
 * Registration document.
 * Document ID in Firestore = "{eventId}_{userId}" (composite key set by RegistrationRepository).
 */
public class Registration {
    @DocumentId
    private String id;
    private String eventId;
    private String userId;
    private String userName;
    private String userEmail;
    private String status;      // "invited", "waiting", "selected", "accepted", "declined", "cancelled"
    private long joinedAt;
    private long respondedAt;
    private double latitude;
    private double longitude;
    private boolean geoVerified; // renamed from hasGeolocation to avoid boolean serialization issues

    public static final String STATUS_INVITED   = "invited";
    public static final String STATUS_WAITING   = "waiting";
    public static final String STATUS_SELECTED  = "selected";
    public static final String STATUS_ACCEPTED  = "accepted";
    public static final String STATUS_DECLINED  = "declined";
    public static final String STATUS_CANCELLED = "cancelled";

    public Registration() {
        this.joinedAt = System.currentTimeMillis();
        this.status   = STATUS_WAITING;
    }

    public Registration(String eventId, String userId, String userName, String userEmail) {
        this.eventId   = eventId;
        this.userId    = userId;
        this.userName  = userName;
        this.userEmail = userEmail;
        this.status    = STATUS_WAITING;
        this.joinedAt  = System.currentTimeMillis();
    }

    public String getId()               { return id; }
    public void   setId(String id)      { this.id = id; }

    public String getEventId()                    { return eventId; }
    public void   setEventId(String eventId)      { this.eventId = eventId; }

    public String getUserId()                     { return userId; }
    public void   setUserId(String userId)        { this.userId = userId; }

    public String getUserName()                   { return userName; }
    public void   setUserName(String userName)    { this.userName = userName; }

    public String getUserEmail()                  { return userEmail; }
    public void   setUserEmail(String userEmail)  { this.userEmail = userEmail; }

    public String getStatus()                     { return status; }
    public void   setStatus(String status)        { this.status = status; }

    public long   getJoinedAt()                   { return joinedAt; }
    public void   setJoinedAt(long joinedAt)      { this.joinedAt = joinedAt; }

    public long   getRespondedAt()                { return respondedAt; }
    public void   setRespondedAt(long respondedAt){ this.respondedAt = respondedAt; }

    public double getLatitude()                   { return latitude; }
    public void   setLatitude(double latitude)    { this.latitude = latitude; }

    public double getLongitude()                  { return longitude; }
    public void   setLongitude(double longitude)  { this.longitude = longitude; }

    // "geoVerified" stores cleanly as boolean in Firestore
    public boolean isGeoVerified()                   { return geoVerified; }
    public void    setGeoVerified(boolean geoVerified){ this.geoVerified = geoVerified; }

    // Keep old name as alias so existing callers compile
    public boolean isHasGeolocation()                { return geoVerified; }
    public void    setHasGeolocation(boolean v)      { this.geoVerified = v; }
}
