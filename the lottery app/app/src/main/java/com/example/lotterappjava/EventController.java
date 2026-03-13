package com.example.lotterappjava;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controller class for managing Event-related data operations in Firestore.
 * This class follows the Controller component of the MVC design pattern,
 * handling business logic for event creation, lottery draws, and entrant management.
 *
 * Outstanding issues:
 * - Notification sending is currently a log-only stub.
 */
public class EventController {
    private static final String TAG = "EventController";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Listener interface for loading a list of events.
     */
    public interface OnEventsLoadedListener {
        /**
         * @param events The list of loaded events.
         */
        void onEventsLoaded(List<Event> events);
    }

    /**
     * Listener interface for loading a single event.
     */
    public interface OnEventLoadedListener {
        /**
         * @param event The loaded event, or null if not found.
         */
        void onEventLoaded(Event event);
    }

    /**
     * Listener interface for loading events along with their statuses for a user.
     */
    public interface OnEventsWithStatusLoadedListener {
        /**
         * @param events The list of loaded events.
         * @param statuses A map of event IDs to the user's status for that event.
         */
        void onEventsLoaded(List<Event> events, Map<String, String> statuses);
    }

    /**
     * Listener interface for completing an event-related action.
     */
    public interface OnEventActionCompleteListener {
        /**
         * @param success True if the action was successful, false otherwise.
         */
        void onComplete(boolean success);
    }

    /**
     * Listener interface for loading a list of image URLs.
     */
    public interface OnImageUrlsLoadedListener {
        /**
         * @param imageUrls The list of image URLs.
         */
        void onImageUrlsLoaded(List<String> imageUrls);
    }

    /**
     * Listener interface for loading a list of entrants (users).
     */
    public interface OnEntrantsLoadedListener {
        /**
         * @param entrants The list of loaded users.
         */
        void onEntrantsLoaded(List<User> entrants);
    }

    /**
     * Listener interface for checking if a user is on a waitlist.
     */
    public interface OnWaitlistCheckCompleteListener {
        /**
         * @param onWaitlist True if the user is on the waitlist, false otherwise.
         */
        void onComplete(boolean onWaitlist);
    }

    /**
     * Listener interface for loading an entrant's status.
     */
    public interface OnEntrantStatusLoadedListener {
        /**
         * @param status The entrant's status string.
         */
        void onStatusLoaded(String status);
    }

    /**
     * Listener interface for loading participant information.
     */
    public interface OnParticipantStatusLoadedListener {
        /**
         * @param participants The list of participants with their statuses and locations.
         */
        void onParticipantsLoaded(List<Participant> participants);
    }

    /**
     * Represents a participant in an event, combining user data with status and location.
     */
    public static class Participant {
        private User user;
        private String status; 
        private Double latitude;
        private Double longitude;

        /**
         * Constructor for a participant without location data.
         * @param user The user object.
         * @param status The user's status in the event.
         */
        public Participant(User user, String status) {
            this.user = user;
            this.status = status;
        }

        /**
         * Constructor for a participant with location data.
         * @param user The user object.
         * @param status The user's status in the event.
         * @param latitude The user's latitude.
         * @param longitude The user's longitude.
         */
        public Participant(User user, String status, Double latitude, Double longitude) {
            this.user = user;
            this.status = status;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public User getUser() { return user; }
        public String getStatus() { return status; }
        public Double getLatitude() { return latitude; }
        public Double getLongitude() { return longitude; }
    }

    /**
     * Retrieves an event from Firestore by its unique identifier.
     * @param eventId The event ID.
     * @param listener The callback listener.
     */
    public void getEvent(String eventId, OnEventLoadedListener listener) {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        listener.onEventLoaded(documentSnapshot.toObject(Event.class));
                    } else {
                        listener.onEventLoaded(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error getting event", e);
                    listener.onEventLoaded(null);
                });
    }

    /**
     * Creates or updates an event in Firestore.
     * @param event The event object.
     * @param organizerId The identifier of the organizer.
     * @param listener The callback listener.
     */
    public void createEvent(Event event, String organizerId, OnEventActionCompleteListener listener) {
        String eventId = event.getEventId();
        if (eventId == null || eventId.isEmpty()) {
            eventId = UUID.randomUUID().toString();
            event.setEventId(eventId);
        }
        event.setOrganizerId(organizerId);
        
        if (event.getQrCodeUrl() == null) {
            event.setQrCodeUrl("lotterapp://event/" + eventId);
        }

        db.collection("events").document(eventId).set(event)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Event created/updated with ID: " + event.getEventId());
                    if (listener != null) listener.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error creating event", e);
                    if (listener != null) listener.onComplete(false);
                });
    }

    /**
     * Retrieves all events created by a specific organizer.
     * @param organizerId The organizer's ID.
     * @param listener The callback listener.
     */
    public void getEventsForOrganizer(String organizerId, OnEventsLoadedListener listener) {
        db.collection("events").whereEqualTo("organizerId", organizerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> events = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        events.add(document.toObject(Event.class));
                    }
                    listener.onEventsLoaded(events);
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error getting events for organizer", e));
    }

    /**
     * Retrieves all events from Firestore.
     * @param listener The callback listener.
     */
    public void getAllEvents(OnEventsLoadedListener listener) {
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> events = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        events.add(document.toObject(Event.class));
                    }
                    listener.onEventsLoaded(events);
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error getting all events", e));
    }

    /**
     * Retrieves all entrants registered for a specific event.
     * @param eventId The event ID.
     * @param listener The callback listener.
     */
    public void getEntrantsForEvent(String eventId, OnEntrantsLoadedListener listener) {
        db.collection("events").document(eventId).collection("entrants").get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Task<DocumentSnapshot>> userTasks = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String userId = doc.getString("userId");
                        if (userId != null) {
                            userTasks.add(db.collection("users").document(userId).get());
                        }
                    }

                    if (userTasks.isEmpty()) {
                        listener.onEntrantsLoaded(new ArrayList<>());
                        return;
                    }

                    Tasks.whenAllSuccess(userTasks).addOnSuccessListener(userSnapshots -> {
                        List<User> users = new ArrayList<>();
                        for (Object snapshot : userSnapshots) {
                            DocumentSnapshot userDoc = (DocumentSnapshot) snapshot;
                            if (userDoc.exists()) {
                                User user = userDoc.toObject(User.class);
                                if (user != null) users.add(user);
                            }
                        }
                        listener.onEntrantsLoaded(users);
                    });
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error getting entrants", e));
    }

    /**
     * Retrieves entrants with a specific status for an event.
     * @param eventId The event ID.
     * @param status The status to filter by (e.g., "waiting", "invited").
     * @param listener The callback listener.
     */
    public void getEntrantsWithStatus(String eventId, String status, OnParticipantStatusLoadedListener listener) {
        db.collection("events").document(eventId).collection("entrants")
                .whereEqualTo("status", status)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Task<DocumentSnapshot>> userTasks = new ArrayList<>();
                    Map<String, DocumentSnapshot> entrantDocs = new HashMap<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String userId = doc.getString("userId");
                        if (userId != null) {
                            userTasks.add(db.collection("users").document(userId).get());
                            entrantDocs.put(userId, doc);
                        }
                    }

                    if (userTasks.isEmpty()) {
                        listener.onParticipantsLoaded(new ArrayList<>());
                        return;
                    }

                    Tasks.whenAllSuccess(userTasks).addOnSuccessListener(userSnapshots -> {
                        List<Participant> participants = new ArrayList<>();
                        for (Object snapshot : userSnapshots) {
                            DocumentSnapshot userDoc = (DocumentSnapshot) snapshot;
                            if (userDoc.exists()) {
                                User user = userDoc.toObject(User.class);
                                if (user != null) {
                                    String key = (user.getUid() != null && !user.getUid().isEmpty()) ? user.getUid() : user.getDeviceId();
                                    DocumentSnapshot entrantDoc = entrantDocs.get(key);
                                    if (entrantDoc != null) {
                                        Double lat = entrantDoc.getDouble("latitude");
                                        Double lon = entrantDoc.getDouble("longitude");
                                        participants.add(new Participant(user, status, lat, lon));
                                    }
                                }
                            }
                        }
                        listener.onParticipantsLoaded(participants);
                    });
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error getting participants", e));
    }

    /**
     * Randomly draws a specified number of winners from the waiting list.
     * @param eventId The event ID.
     * @param organizerId The identifier of the organizer.
     * @param numberOfWinners The number of entrants to select.
     * @param listener The callback listener.
     */
    public void drawLottery(String eventId, String organizerId, int numberOfWinners, OnEventActionCompleteListener listener) {
        getEvent(eventId, event -> {
            if (event != null && organizerId.equals(event.getOrganizerId())) {
                db.collection("events").document(eventId).collection("entrants")
                        .whereEqualTo("status", "waiting")
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            List<DocumentSnapshot> waitingList = new ArrayList<>(querySnapshot.getDocuments());
                            Collections.shuffle(waitingList);

                            int actualWinners = Math.min(numberOfWinners, waitingList.size());
                            WriteBatch batch = db.batch();

                            for (int i = 0; i < actualWinners; i++) {
                                batch.update(waitingList.get(i).getReference(), "status", "invited");
                            }

                            batch.commit().addOnCompleteListener(task -> {
                                if (listener != null) listener.onComplete(task.isSuccessful());
                            });
                        });
            } else {
                if (listener != null) listener.onComplete(false);
            }
        });
    }

    /**
     * Draws a single replacement winner from the waiting list.
     * @param eventId The event ID.
     * @param organizerId The organizer's ID.
     * @param listener The callback listener.
     */
    public void drawReplacement(String eventId, String organizerId, OnEventActionCompleteListener listener) {
        drawLottery(eventId, organizerId, 1, listener);
    }

    /**
     * Sends a notification message to a specific group of entrants.
     * @param eventId The event ID.
     * @param status The status group (e.g., "waiting", "invited").
     * @param message The message content.
     * @param listener The callback listener.
     */
    public void sendNotificationToGroup(String eventId, String status, String message, OnEventActionCompleteListener listener) {
        Log.d(TAG, "Sending notification to " + status + " entrants of event " + eventId + ": " + message);
        if (listener != null) listener.onComplete(true);
    }

    /**
     * Updates the status of a specific entrant for an event.
     * @param eventId The event ID.
     * @param userId The ID of the entrant.
     * @param newStatus The new status to set.
     * @param listener The callback listener.
     */
    public void updateEntrantStatus(String eventId, String userId, String newStatus, OnEventActionCompleteListener listener) {
        db.collection("events").document(eventId).collection("entrants").document(userId)
                .update("status", newStatus)
                .addOnCompleteListener(task -> {
                    if (listener != null) listener.onComplete(task.isSuccessful());
                });
    }

    /**
     * Adds a user to the waiting list for an event, respecting the maximum capacity if set.
     * @param eventId The event ID.
     * @param userId The ID of the user.
     * @param lat Optional latitude of the user's location.
     * @param lon Optional longitude of the user's location.
     * @param listener The callback listener.
     */
    public void joinWaitlist(String eventId, String userId, Double lat, Double lon, OnEventActionCompleteListener listener) {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(eventDoc -> {
                    Event event = eventDoc.toObject(Event.class);
                    Integer max = event != null ? event.getMaxWaitingListEntrants() : null;
                    if (max == null || max <= 0) {
                        writeEntrant(eventId, userId, lat, lon, listener);
                        return;
                    }

                    db.collection("events").document(eventId).collection("entrants")
                            .whereEqualTo("status", "waiting")
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                if (querySnapshot.size() >= max) {
                                    if (listener != null) listener.onComplete(false);
                                } else {
                                    writeEntrant(eventId, userId, lat, lon, listener);
                                }
                            })
                            .addOnFailureListener(e -> {
                                if (listener != null) listener.onComplete(false);
                            });
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onComplete(false);
                });
    }

    /**
     * Internal method to write entrant data to Firestore.
     */
    private void writeEntrant(String eventId, String userId, Double lat, Double lon, OnEventActionCompleteListener listener) {
        Map<String, Object> entrantData = new HashMap<>();
        entrantData.put("userId", userId);
        entrantData.put("joinedAt", com.google.firebase.Timestamp.now());
        entrantData.put("status", "waiting");
        entrantData.put("latitude", lat);
        entrantData.put("longitude", lon);

        db.collection("events").document(eventId).collection("entrants").document(userId).set(entrantData)
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) listener.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onComplete(false);
                });
    }

    /**
     * Checks if a specific user is currently on the waitlist for an event.
     * @param eventId The event ID.
     * @param userId The user ID.
     * @param listener The callback listener.
     */
    public void isUserOnWaitlist(String eventId, String userId, OnWaitlistCheckCompleteListener listener) {
        db.collection("events").document(eventId).collection("entrants").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String status = documentSnapshot.getString("status");
                        listener.onComplete("waiting".equals(status));
                    } else {
                        listener.onComplete(false);
                    }
                })
                .addOnFailureListener(e -> listener.onComplete(false));
    }

    /**
     * Retrieves the current status of an entrant for a specific event.
     * @param eventId The event ID.
     * @param userId The user ID.
     * @param listener The callback listener.
     */
    public void getEntrantStatus(String eventId, String userId, OnEntrantStatusLoadedListener listener) {
        db.collection("events").document(eventId).collection("entrants").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        listener.onStatusLoaded(documentSnapshot.getString("status"));
                    } else {
                        listener.onStatusLoaded(null);
                    }
                })
                .addOnFailureListener(e -> listener.onStatusLoaded(null));
    }

    /**
     * Removes a user from the waitlist for an event.
     * @param eventId The event ID.
     * @param userId The user ID.
     * @param listener The callback listener.
     */
    public void leaveWaitlist(String eventId, String userId, OnEventActionCompleteListener listener) {
        db.collection("events").document(eventId).collection("entrants").document(userId).delete()
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) listener.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onComplete(false);
                });
    }

    /**
     * Retrieves all events that a specific user has joined or registered for.
     * @param userId The user ID.
     * @param listener The callback listener.
     */
    public void getEventsForUser(String userId, OnEventsLoadedListener listener) {
        getAllEvents(allEvents -> {
            List<Event> userEvents = new ArrayList<>();
            AtomicInteger remaining = new AtomicInteger(allEvents.size());
            
            if (allEvents.isEmpty()) {
                listener.onEventsLoaded(userEvents);
                return;
            }

            for (Event event : allEvents) {
                db.collection("events").document(event.getEventId())
                        .collection("entrants").document(userId).get()
                        .addOnSuccessListener(doc -> {
                            if (doc.exists()) {
                                userEvents.add(event);
                            }
                            if (remaining.decrementAndGet() == 0) {
                                listener.onEventsLoaded(userEvents);
                            }
                        })
                        .addOnFailureListener(e -> {
                            if (remaining.decrementAndGet() == 0) {
                                listener.onEventsLoaded(userEvents);
                            }
                        });
            }
        });
    }

    /**
     * Retrieves all events for a user along with their status for each event.
     * @param userId The user ID.
     * @param listener The callback listener.
     */
    public void getEventsWithStatusForUser(String userId, OnEventsWithStatusLoadedListener listener) {
        getAllEvents(allEvents -> {
            List<Event> userEvents = new ArrayList<>();
            Map<String, String> statuses = new HashMap<>();
            AtomicInteger remaining = new AtomicInteger(allEvents.size());
            
            if (allEvents.isEmpty()) {
                listener.onEventsLoaded(userEvents, statuses);
                return;
            }

            for (Event event : allEvents) {
                db.collection("events").document(event.getEventId())
                        .collection("entrants").document(userId).get()
                        .addOnSuccessListener(doc -> {
                            if (doc.exists()) {
                                userEvents.add(event);
                                String status = doc.getString("status");
                                if (status != null) {
                                    statuses.put(event.getEventId(), status);
                                }
                            }
                            if (remaining.decrementAndGet() == 0) {
                                listener.onEventsLoaded(userEvents, statuses);
                            }
                        })
                        .addOnFailureListener(e -> {
                            if (remaining.decrementAndGet() == 0) {
                                listener.onEventsLoaded(userEvents, statuses);
                            }
                        });
            }
        });
    }

    /**
     * Aggregates all image URLs from events, users, and facilities for administration.
     * @param listener The callback listener.
     */
    public void getAllImageUrls(OnImageUrlsLoadedListener listener) {
        List<String> imageUrls = new ArrayList<>();
        
        db.collection("events").get().addOnSuccessListener(eventSnapshots -> {
            for (DocumentSnapshot doc : eventSnapshots) {
                String poster = doc.getString("posterUrl");
                if (poster != null && !poster.isEmpty()) imageUrls.add(poster);
                
                String qr = doc.getString("qrCodeUrl");
                if (qr != null && !qr.isEmpty() && qr.startsWith("http")) imageUrls.add(qr);
            }
            
            db.collection("users").get().addOnSuccessListener(userSnapshots -> {
                for (DocumentSnapshot doc : userSnapshots) {
                    String url = doc.getString("profileImageUrl");
                    if (url != null && !url.isEmpty()) imageUrls.add(url);
                }
                
                db.collection("facilities").get().addOnSuccessListener(facilitySnapshots -> {
                    for (DocumentSnapshot doc : facilitySnapshots) {
                        String url = doc.getString("imageUrl");
                        if (url != null && !url.isEmpty()) imageUrls.add(url);
                    }
                    Log.d(TAG, "Aggregated " + imageUrls.size() + " images for Admin.");
                    listener.onImageUrlsLoaded(imageUrls);
                }).addOnFailureListener(e -> listener.onImageUrlsLoaded(imageUrls));
                
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error fetching user images", e);
                listener.onImageUrlsLoaded(imageUrls);
            });
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error fetching event images", e);
            listener.onImageUrlsLoaded(new ArrayList<>());
        });
    }

    /**
     * Deletes an image reference across all collections (events, users, facilities).
     * @param imageUrl The URL of the image to remove.
     * @param listener The callback listener.
     */
    public void deleteEventImage(String imageUrl, OnEventActionCompleteListener listener) {
        WriteBatch batch = db.batch();
        
        // 1. Check events collection (Poster)
        db.collection("events").whereEqualTo("posterUrl", imageUrl).get()
            .addOnSuccessListener(eventPosterSnaps -> {
                for (QueryDocumentSnapshot doc : eventPosterSnaps) {
                    batch.update(doc.getReference(), "posterUrl", null);
                }
                
                // 2. Check events collection (QR Code)
                db.collection("events").whereEqualTo("qrCodeUrl", imageUrl).get()
                    .addOnSuccessListener(eventQrSnaps -> {
                        for (QueryDocumentSnapshot doc : eventQrSnaps) {
                            batch.update(doc.getReference(), "qrCodeUrl", null);
                        }
                        
                        // 3. Check users collection
                        db.collection("users").whereEqualTo("profileImageUrl", imageUrl).get()
                            .addOnSuccessListener(userSnaps -> {
                                for (QueryDocumentSnapshot doc : userSnaps) {
                                    batch.update(doc.getReference(), "profileImageUrl", null);
                                }
                                
                                // 4. Check facilities collection
                                db.collection("facilities").whereEqualTo("imageUrl", imageUrl).get()
                                    .addOnSuccessListener(facilitySnapshots -> {
                                        for (QueryDocumentSnapshot doc : facilitySnapshots) {
                                            batch.update(doc.getReference(), "imageUrl", null);
                                        }
                                        
                                        batch.commit().addOnCompleteListener(task -> {
                                            if (listener != null) listener.onComplete(task.isSuccessful());
                                        });
                                    })
                                    .addOnFailureListener(e -> batch.commit().addOnCompleteListener(t -> listener.onComplete(t.isSuccessful())));
                            });
                    });
            })
            .addOnFailureListener(e -> {
                if (listener != null) listener.onComplete(false);
            });
    }

    /**
     * Deletes an event if the provided organizer ID matches the event's organizer.
     * @param eventId The event ID.
     * @param organizerId The organizer's ID.
     * @param listener The callback listener.
     */
    public void deleteEvent(String eventId, String organizerId, OnEventActionCompleteListener listener) {
        getEvent(eventId, event -> {
            if (event != null && organizerId.equals(event.getOrganizerId())) {
                deleteEvent(eventId, listener);
            } else {
                if (listener != null) listener.onComplete(false);
            }
        });
    }

    /**
     * Directly deletes an event from Firestore by ID.
     * @param eventId The ID of the event to delete.
     * @param listener The callback listener.
     */
    public void deleteEvent(String eventId, OnEventActionCompleteListener listener) {
        db.collection("events").document(eventId).delete()
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) listener.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onComplete(false);
                });
    }
}
