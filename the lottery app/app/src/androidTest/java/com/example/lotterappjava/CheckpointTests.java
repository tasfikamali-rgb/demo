package com.example.lotterappjava;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Integration tests for Halfway Checkpoint Release User Stories.
 * These tests verify the business logic and Firebase integration for Entrants, Organizers, and Admins.
 */
@RunWith(AndroidJUnit4.class)
public class CheckpointTests {

    private UserController userController;
    private EventController eventController;
    private String testEntrantId;
    private String testOrganizerId;
    private String testEventId;

    @Before
    public void setUp() throws InterruptedException {
        userController = new UserController();
        eventController = new EventController();
        testEntrantId = "test_entrant_" + System.currentTimeMillis();
        testOrganizerId = "test_organizer_" + System.currentTimeMillis();
        testEventId = "test_event_" + System.currentTimeMillis();

        // Ensure the test user exists in the 'users' collection for tests that fetch entrant details
        CountDownLatch latch = new CountDownLatch(1);
        User user = new User(testEntrantId);
        user.setName("Test Entrant");
        userController.updateUser(user, aVoid -> latch.countDown());
        assertTrue("Setup timed out: failed to create test user", latch.await(10, TimeUnit.SECONDS));
    }

    @After
    public void tearDown() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        userController.deleteUser(testEntrantId, aVoid -> {
            eventController.deleteEvent(testEventId, aVoid2 -> latch.countDown());
        });
        latch.await(10, TimeUnit.SECONDS);
    }

    // --- Entrant Stories ---

    /**
     * US 01.07.01 & 01.02.01 & 01.02.02: Profile Management
     */
    @Test
    public void testEntrantProfileFlow() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        userController.getUser(testEntrantId, loadedUser -> {
            assertNotNull(loadedUser);
            
            // Update profile
            loadedUser.setName("Updated Name");
            userController.updateUser(loadedUser, aVoid2 -> {
                userController.getUser(testEntrantId, updatedUser -> {
                    assertEquals("Updated Name", updatedUser.getName());
                    latch.countDown();
                });
            });
        });

        assertTrue(latch.await(15, TimeUnit.SECONDS));
    }

    /**
     * US 01.01.01 & 01.01.02 & 01.05.04: Waitlist Management
     */
    @Test
    public void testWaitlistFlow() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Event event = new Event(testEventId, testOrganizerId);
        event.setTitle("Waitlist Test Event");

        eventController.createEvent(event, testOrganizerId, success -> {
            assertTrue(success);
            // US 01.01.01: Join waitlist
            eventController.joinWaitlist(testEventId, testEntrantId, 0.0, 0.0, successJoin -> {
                assertTrue(successJoin);
                
                // US 01.05.04: View total entrants (verify existence)
                eventController.getEntrantsForEvent(testEventId, entrants -> {
                    // This now works because the user document exists in 'users' collection
                    assertEquals(1, entrants.size());
                    
                    // US 01.01.02: Leave waitlist
                    eventController.leaveWaitlist(testEventId, testEntrantId, successLeave -> {
                        assertTrue(successLeave);
                        latch.countDown();
                    });
                });
            });
        });

        assertTrue(latch.await(20, TimeUnit.SECONDS));
    }

    // --- Organizer Stories ---

    /**
     * US 02.01.01 & 02.01.04: Event Creation and QR Generation
     */
    @Test
    public void testOrganizerEventCreation() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Event event = new Event(testEventId, testOrganizerId);
        event.setTitle("Lottery Event");
        event.setRegistrationStart(new Date());
        event.setRegistrationEnd(new Date(System.currentTimeMillis() + 86400000));

        eventController.createEvent(event, testOrganizerId, success -> {
            assertTrue(success);
            eventController.getEvent(testEventId, loadedEvent -> {
                assertNotNull(loadedEvent);
                assertNotNull(loadedEvent.getQrCodeUrl());
                latch.countDown();
            });
        });

        assertTrue(latch.await(15, TimeUnit.SECONDS));
    }

    /**
     * US 02.05.02 & 01.05.02 & 01.05.03: Lottery Draw and Invitation
     */
    @Test
    public void testLotteryAndInvitation() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Event event = new Event(testEventId, testOrganizerId);
        
        eventController.createEvent(event, testOrganizerId, success -> {
            eventController.joinWaitlist(testEventId, testEntrantId, 0.0, 0.0, successJoin -> {
                // US 02.05.02: Draw lottery (1 winner)
                eventController.drawLottery(testEventId, testOrganizerId, 1, successDraw -> {
                    assertTrue(successDraw);
                    
                    // US 01.04.01: Check if entrant is chosen (status = invited)
                    eventController.getEntrantStatus(testEventId, testEntrantId, status -> {
                        assertEquals("invited", status);
                        
                        // US 01.05.02: Accept invitation
                        eventController.updateEntrantStatus(testEventId, testEntrantId, "accepted", successAccept -> {
                            assertTrue(successAccept);
                            eventController.getEntrantStatus(testEventId, testEntrantId, finalStatus -> {
                                assertEquals("accepted", finalStatus);
                                latch.countDown();
                            });
                        });
                    });
                });
            });
        });

        assertTrue(latch.await(25, TimeUnit.SECONDS));
    }

    /**
     * US 03.04.01 & 03.02.01: Admin Browse and Remove
     */
    @Test
    public void testAdminManagement() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        
        Event event = new Event(testEventId, testOrganizerId);
        eventController.createEvent(event, testOrganizerId, success -> {
            eventController.getAllEvents(events -> {
                boolean found = false;
                for (Event e : events) {
                    if (e.getEventId().equals(testEventId)) found = true;
                }
                assertTrue(found);
                latch.countDown();
            });
        });

        assertTrue(latch.await(15, TimeUnit.SECONDS));
    }
}
