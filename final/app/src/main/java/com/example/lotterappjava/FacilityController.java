package com.example.lotterappjava;

import android.util.Log;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Controller class for managing Facility-related data operations in Firestore.
 * This class follows the Controller component of the MVC design pattern,
 * providing methods to retrieve and update facility information.
 *
 * Outstanding issues:
 * - None currently identified.
 */
public class FacilityController {
    private static final String TAG = "FacilityController";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Listener interface for facility document load.
     */
    public interface OnFacilityLoadedListener {
        /**
         * Called when a facility is loaded.
         * @param facility The Facility object loaded from Firestore, or null if not found.
         */
        void onFacilityLoaded(Facility facility);
    }

    /**
     * Retrieves a facility from Firestore using the organizer's ID as the document identifier.
     *
     * @param organizerId The ID of the organizer associated with the facility.
     * @param listener The callback listener for the result.
     */
    public void getFacility(String organizerId, OnFacilityLoadedListener listener) {
        db.collection("facilities").document(organizerId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Facility facility = documentSnapshot.toObject(Facility.class);
                listener.onFacilityLoaded(facility);
            } else {
                listener.onFacilityLoaded(null);
            }
        });
    }

    /**
     * Updates an existing facility's information in Firestore or creates a new one.
     *
     * @param facility The Facility object containing updated information.
     * @param successListener Callback for successful update.
     */
    public void updateFacility(Facility facility, OnSuccessListener<Void> successListener) {
        db.collection("facilities").document(facility.getOrganizerId()).set(facility)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(e -> Log.e(TAG, "Error updating facility", e));
    }
}
