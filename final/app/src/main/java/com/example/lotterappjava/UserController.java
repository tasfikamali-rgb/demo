package com.example.lotterappjava;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserController {
    private static final String TAG = "UserController";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnUserLoadedListener {
        void onUserLoaded(User user);
    }

    public interface OnAllUsersLoadedListener {
        void onAllUsersLoaded(List<User> users);
    }

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

    public void getAllUsers(OnAllUsersLoadedListener listener) {
        db.collection("users").get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<User> users = new ArrayList<>();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                users.add(document.toObject(User.class));
            }
            listener.onAllUsersLoaded(users);
        });
    }

    public void updateUser(User user, OnSuccessListener<Void> successListener) {
        String docId = (user.getUid() != null && !user.getUid().isEmpty()) ? user.getUid() : user.getDeviceId();
        db.collection("users").document(docId).set(user)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(e -> Log.e(TAG, "Error updating user", e));
    }

    public void deleteUser(String userId, OnSuccessListener<Void> successListener) {
        db.collection("users").document(userId).delete()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(e -> Log.e(TAG, "Error deleting user", e));
    }
}
