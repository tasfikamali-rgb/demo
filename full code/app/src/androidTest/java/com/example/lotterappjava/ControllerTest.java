package com.example.lotterappjava;

import static org.junit.Assert.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class ControllerTest {
    private UserController userController;
    private final String TEST_DEVICE_ID = "test-device-id-123";

    @Before
    public void setUp() {
        userController = new UserController();
    }

    @Test
    public void testUserLifecycle() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final User testUser = new User(TEST_DEVICE_ID);
        testUser.setName("JUnit Test User");
        testUser.setEmail("junit@test.com");

        // Step 1: Create/Update
        userController.updateUser(testUser, aVoid -> {
            // Step 2: Retrieve and verify
            userController.getUser(TEST_DEVICE_ID, user -> {
                if (user != null) {
                    assertEquals("JUnit Test User", user.getName());
                    assertEquals("junit@test.com", user.getEmail());
                    
                    // Step 3: Delete
                    userController.deleteUser(TEST_DEVICE_ID, aVoid2 -> {
                        latch.countDown();
                    });
                } else {
                    fail("User was not found after update");
                    latch.countDown();
                }
            });
        });

        // Wait for asynchronous Firebase calls to complete
        boolean success = latch.await(15, TimeUnit.SECONDS);
        assertTrue("Timeout waiting for Firebase operations", success);
    }
}
