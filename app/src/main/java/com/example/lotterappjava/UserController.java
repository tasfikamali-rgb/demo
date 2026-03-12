package com.example.lotterappjava;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller class for managing User-related data operations in Firestore.
 * This class follows the Controller component of the MVC design pattern,
 * providing methods to create, retrieve, update, and delete user records.
 *
 * Outstanding issues:
 * - None currently identified.
 */
public class UserController {
    private static final String TAG = "UserController";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Listener interface for a single user document load.
     */
    public interface OnUserLoadedListener {
        /**
         * Called when a user is loaded.
         * @param user The User object loaded from Firestore, or null if not found.
         */
        void onUserLoaded(User user);
    }

    /**
     * Listener interface for loading all users.
     */
    public interface OnAllUsersLoadedListener {
        /**
         * Called when all users are loaded.
         * @param users A list of all User objects retrieved from Firestore.
         */
        void onAllUsersLoaded(List<User> users);
    }

    /**
     * Checks if a user document exists for the current device and creates one if it doesn't.
     *
     * @param context The application context to retrieve the device ID.
     */
    public void checkAndCreateUser(Context context) {
        String deviceId = DeviceIdManager.getDeviceId(context);
        db.collection("users").document(deviceId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (!document.exists()) {
                    User newUser = new User(deviceId);
                    newUser.setName("New User"); // Default name
                    db.collection("users").document(deviceId).set(newUser)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "New user created with deviceId: " + deviceId))
                            .addOnFailureListener(e -> Log.w(TAG, "Error creating new user", e));
                }
            }
        });
    }

    /**
     * Retrieves a user from Firestore by their unique user ID.
     *
     * @param userId The ID of the user to retrieve (UID or DeviceID).
     * @param listener The callback listener for the result.
     */
    public void getUser(String userId, OnUserLoadedListener listener) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        listener.onUserLoaded(user);
                    } else {
                        listener.onUserLoaded(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting user", e);
                    listener.onUserLoaded(null);
                });
    }

    /**
     * Retrieves all users currently stored in the Firestore "users" collection.
     *
     * @param listener The callback listener for the result.
     */
    public void getAllUsers(OnAllUsersLoadedListener listener) {
        db.collection("users").get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<User> users = new ArrayList<>();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                users.add(document.toObject(User.class));
            }
            listener.onAllUsersLoaded(users);
        });
    }

    /**
     * Updates an existing user's information in Firestore.
     * Uses UID if available, otherwise uses DeviceID as the document identifier.
     *
     * @param user The User object containing updated information.
     * @param successListener Callback for successful update.
     */
    public void updateUser(User user, OnSuccessListener<Void> successListener) {
        String docId = (user.getUid() != null && !user.getUid().isEmpty()) ? user.getUid() : user.getDeviceId();
        db.collection("users").document(docId).set(user)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(e -> Log.e(TAG, "Error updating user", e));
    }

    /**
     * Deletes a user document from Firestore.
     *
     * @param userId The ID of the user to delete.
     * @param successListener Callback for successful deletion.
     */
    public void deleteUser(String userId, OnSuccessListener<Void> successListener) {
        db.collection("users").document(userId).delete()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(e -> Log.e(TAG, "Error deleting user", e));
    }
}
