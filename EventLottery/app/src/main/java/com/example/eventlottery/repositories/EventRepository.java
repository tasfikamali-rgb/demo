package com.example.eventlottery.repositories;

import com.example.eventlottery.models.Event;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class EventRepository {
    private static final String COLLECTION = "events";
    private final CollectionReference eventsRef;

    public EventRepository() {
        eventsRef = FirebaseFirestore.getInstance().collection(COLLECTION);
    }

    public Task<DocumentReference> createEvent(Event event) {
        return eventsRef.add(event);
    }

    public Task<Void> updateEvent(Event event) {
        if (event.getId() == null) return Tasks.forException(new Exception("Event ID missing"));
        return eventsRef.document(event.getId()).set(event);
    }

    public Task<Void> updateEvent(String eventId, Event event) {
        return eventsRef.document(eventId).set(event);
    }

    public Task<DocumentSnapshot> getEventById(String eventId) {
        return eventsRef.document(eventId).get();
    }

    /** All public events */
    public Task<QuerySnapshot> getAllPublicEvents() {
        // Note: Removed orderBy("createdAt") to avoid requiring a composite index.
        return eventsRef
                .whereEqualTo("private", false)
                .get();
    }

    /** All events for the organizer dashboard */
    public Task<QuerySnapshot> getEventsByOrganizer(String organizerId) {
        // Note: Removed orderBy("createdAt") to avoid requiring a composite index.
        return eventsRef
                .whereEqualTo("organizerId", organizerId)
                .get();
    }

    /** Events where user is co-organizer */
    public Task<QuerySnapshot> getCoOrganizedEvents(String userId) {
        return eventsRef
                .whereArrayContains("coOrganizerIds", userId)
                .get();
    }

    /** Events by status */
    public Task<QuerySnapshot> getEventsByStatus(String status) {
        return eventsRef
                .whereEqualTo("status", status)
                .whereEqualTo("private", false)
                .get();
    }

    public Task<Void> updateEventStatus(String eventId, String status) {
        return eventsRef.document(eventId).update("status", status);
    }

    /** Atomically increment waiting list count (use +1 to join, -1 to leave) */
    public Task<Void> incrementWaitingListCount(String eventId, long delta) {
        return eventsRef.document(eventId).update("waitingListCount", FieldValue.increment(delta));
    }

    /** Atomically increment accepted count */
    public Task<Void> incrementAcceptedCount(String eventId, long delta) {
        return eventsRef.document(eventId).update("acceptedCount", FieldValue.increment(delta));
    }

    /** Legacy: kept for compatibility — prefer increment methods above */
    public Task<Void> updateWaitingListCount(String eventId, int count) {
        return eventsRef.document(eventId).update("waitingListCount", count);
    }

    public Task<Void> updateAcceptedCount(String eventId, int count) {
        return eventsRef.document(eventId).update("acceptedCount", count);
    }

    public Task<Void> setQrCodeContent(String eventId, String qrContent) {
        return eventsRef.document(eventId).update("qrCodeContent", qrContent);
    }

    public Task<Void> deleteEvent(String eventId) {
        return eventsRef.document(eventId).delete();
    }

    public Task<QuerySnapshot> getAllEvents() {
        return eventsRef.orderBy("createdAt", Query.Direction.DESCENDING).get();
    }

    /** Search events by title (prefix match) */
    public Task<QuerySnapshot> searchEvents(String keyword) {
        String lower = keyword.toLowerCase();
        return eventsRef
                .whereEqualTo("private", false)
                .orderBy("title")
                .startAt(lower)
                .endAt(lower + "\uf8ff")
                .get();
    }

    public Task<Void> addCoOrganizer(String eventId, String userId) {
        return eventsRef.document(eventId).update("coOrganizerIds", FieldValue.arrayUnion(userId));
    }
}
