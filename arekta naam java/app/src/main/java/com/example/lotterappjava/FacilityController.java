package com.example.lotterappjava;

import android.util.Log;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class FacilityController {
    private static final String TAG = "FacilityController";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnFacilityLoadedListener {
        void onFacilityLoaded(Facility facility);
    }

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

    public void updateFacility(Facility facility, OnSuccessListener<Void> successListener) {
        db.collection("facilities").document(facility.getOrganizerId()).set(facility)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(e -> Log.e(TAG, "Error updating facility", e));
    }
}
