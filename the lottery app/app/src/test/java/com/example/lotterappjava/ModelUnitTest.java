package com.example.lotterappjava;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Date;

public class ModelUnitTest {

    @Test
    public void testUserCreation() {
        User user = new User("device123");
        assertEquals("device123", user.getDeviceId());
        assertEquals(User.ROLE_ENTRANT, user.getRole());
        assertTrue(user.isNotificationsEnabled());

        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPhoneNumber("1234567890");
        user.setRole(User.ROLE_ORGANIZER);

        assertEquals("Test User", user.getName());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("1234567890", user.getPhoneNumber());
        assertEquals(User.ROLE_ORGANIZER, user.getRole());
    }

    @Test
    public void testEventCreation() {
        Event event = new Event("event123", "org456");
        assertEquals("event123", event.getEventId());
        assertEquals("org456", event.getOrganizerId());

        event.setTitle("Swimming Lessons");
        event.setDescription("Learn to swim");
        event.setCapacity(20);
        event.setPrice(60.0);
        Date now = new Date();
        event.setRegistrationStart(now);

        assertEquals("Swimming Lessons", event.getTitle());
        assertEquals("Learn to swim", event.getDescription());
        assertEquals(20, event.getCapacity());
        assertEquals(60.0, event.getPrice(), 0.001);
        assertEquals(now, event.getRegistrationStart());
    }

    @Test
    public void testFacilityCreation() {
        Facility facility = new Facility("org456", "Rec Centre", "123 Street");
        assertEquals("org456", facility.getOrganizerId());
        assertEquals("Rec Centre", facility.getName());
        assertEquals("123 Street", facility.getLocation());

        facility.setImageUrl("http://image.url");
        assertEquals("http://image.url", facility.getImageUrl());
    }

    @Test
    public void testNotificationCreation() {
        Notification notification = new Notification("notif1", "event1", "org1", "Message", "user1", "waiting");
        assertEquals("notif1", notification.getNotificationId());
        assertEquals("event1", notification.getEventId());
        assertEquals("org1", notification.getOrganizerId());
        assertEquals("Message", notification.getMessage());
        assertEquals("user1", notification.getUserId());
        assertEquals("waiting", notification.getTargetGroup());
        assertNotNull(notification.getTimestamp());
    }
}
