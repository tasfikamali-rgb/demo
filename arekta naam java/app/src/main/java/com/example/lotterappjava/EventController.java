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

public class EventController {
    private static final String TAG = "EventController";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnEventsLoadedListener {
        void onEventsLoaded(List<Event> events);
    }

    public interface OnEventsWithStatusLoadedListener {
        void onEventsLoaded(List<Event> events, Map<String, String> statuses);
    }

    public interface OnEventActionCompleteListener {
        void onComplete(boolean success);
    }

    public interface OnImageUrlsLoadedListener {
        void onImageUrlsLoaded(List<String> imageUrls);
    }

    public interface OnEntrantsLoadedListener {
        void onEntrantsLoaded(List<User> entrants);
    }

    public interface OnWaitlistCheckCompleteListener {
        void onComplete(boolean onWaitlist);
    }

    public interface OnEntrantStatusLoadedListener {
        void onStatusLoaded(String status);
    }

    public interface OnParticipantStatusLoadedListener {
        void onParticipantsLoaded(List<Participant> participants);
    }

    public static class Participant {
        private User user;
        private String status; 
        private Double latitude;
        private Double longitude;

        public Participant(User user, String status) {
            this.user = user;
            this.status = status;
        }

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

    public void drawLottery(String eventId, int numberOfWinners, OnEventActionCompleteListener listener) {
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
    }

    public void drawReplacement(String eventId, OnEventActionCompleteListener listener) {
        drawLottery(eventId, 1, listener);
    }

    public void sendNotificationToGroup(String eventId, String status, String message, OnEventActionCompleteListener listener) {
        Log.d(TAG, "Sending notification to " + status + " entrants of event " + eventId + ": " + message);
        if (listener != null) listener.onComplete(true);
    }

    public void updateEntrantStatus(String eventId, String userId, String newStatus, OnEventActionCompleteListener listener) {
        db.collection("events").document(eventId).collection("entrants").document(userId)
                .update("status", newStatus)
                .addOnCompleteListener(task -> {
                    if (listener != null) listener.onComplete(task.isSuccessful());
                });
    }

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

    public void leaveWaitlist(String eventId, String userId, OnEventActionCompleteListener listener) {
        db.collection("events").document(eventId).collection("entrants").document(userId).delete()
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) listener.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onComplete(false);
                });
    }

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
                                    .addOnSuccessListener(facilitySnaps -> {
                                        for (QueryDocumentSnapshot doc : facilitySnaps) {
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
