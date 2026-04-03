package com.example.eventlottery.repositories;

import com.example.eventlottery.models.Registration;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class RegistrationRepository {
    private static final String COLLECTION = "registrations";
    private final CollectionReference regRef;

    public RegistrationRepository() {
        regRef = FirebaseFirestore.getInstance().collection(COLLECTION);
    }

    public Task<Void> createRegistration(Registration registration) {
        String docId = registration.getEventId() + "_" + registration.getUserId();
        return regRef.document(docId).set(registration);
    }

    public Task<DocumentSnapshot> getRegistration(String eventId, String userId) {
        String docId = eventId + "_" + userId;
        return regRef.document(docId).get();
    }

    /**
     * Update registration status.
     * Only sets respondedAt for accept/decline/cancel transitions (not for lottery selection).
     */
    public Task<Void> updateStatus(String eventId, String userId, String status) {
        String docId = eventId + "_" + userId;
        boolean isResponse = Registration.STATUS_ACCEPTED.equals(status)
                || Registration.STATUS_DECLINED.equals(status)
                || Registration.STATUS_CANCELLED.equals(status);
        if (isResponse) {
            return regRef.document(docId).update(
                    "status", status,
                    "respondedAt", System.currentTimeMillis()
            );
        } else {
            return regRef.document(docId).update("status", status);
        }
    }

    public Task<Void> deleteRegistration(String eventId, String userId) {
        String docId = eventId + "_" + userId;
        return regRef.document(docId).delete();
    }

    /** All registrations for a given event */
    public Task<QuerySnapshot> getRegistrationsForEvent(String eventId) {
        // Removed orderBy to avoid requiring a composite index.
        return regRef.whereEqualTo("eventId", eventId)
                .get();
    }

    /** Registrations filtered by status for an event */
    public Task<QuerySnapshot> getRegistrationsByStatus(String eventId, String status) {
        // Removed orderBy to avoid requiring a composite index.
        return regRef.whereEqualTo("eventId", eventId)
                .whereEqualTo("status", status)
                .get();
    }

    /** All events a user has registered for */
    public Task<QuerySnapshot> getRegistrationsForUser(String userId) {
        // Removed orderBy to avoid requiring a composite index.
        return regRef.whereEqualTo("userId", userId)
                .get();
    }

    /** Check if user is already on the waiting list for an event */
    public Task<QuerySnapshot> checkExistingRegistration(String eventId, String userId) {
        return regRef.whereEqualTo("eventId", eventId)
                .whereEqualTo("userId", userId)
                .limit(1)
                .get();
    }

    /** Count waiting list entries for an event */
    public Task<QuerySnapshot> countWaitingList(String eventId) {
        return regRef.whereEqualTo("eventId", eventId)
                .whereEqualTo("status", Registration.STATUS_WAITING)
                .get();
    }
}
