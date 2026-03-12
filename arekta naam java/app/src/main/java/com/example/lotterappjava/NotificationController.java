package com.example.lotterappjava;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NotificationController {
    private static final String TAG = "NotificationController";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnNotificationsLoadedListener {
        void onNotificationsLoaded(List<Notification> notifications);
    }

    public void sendNotification(String eventId, String message, List<User> recipients) {
        for (User user : recipients) {
            String notificationId = UUID.randomUUID().toString();
            Notification notification = new Notification(notificationId, eventId, message, user.getDeviceId());
            db.collection("notifications").document(notificationId).set(notification)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Notification logged with ID: " + notificationId + " for user: " + user.getDeviceId()))
                    .addOnFailureListener(e -> Log.w(TAG, "Error logging notification for user: " + user.getDeviceId(), e));

            // Log for debugging
            Log.d(TAG, "Sending notification to: " + user.getName());
        }
    }

    public void getAllNotifications(OnNotificationsLoadedListener listener) {
        db.collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Notification> notifications = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        notifications.add(document.toObject(Notification.class));
                    }
                    listener.onNotificationsLoaded(notifications);
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error getting notifications", e));
    }

    public void getNotificationsForUser(String userId, OnNotificationsLoadedListener listener) {
        db.collection("notifications")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Notification> notifications = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        notifications.add(document.toObject(Notification.class));
                    }
                    listener.onNotificationsLoaded(notifications);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error getting notifications for user: " + userId, e);
                    // Return empty list on failure to avoid hanging UI
                    listener.onNotificationsLoaded(new ArrayList<>());
                });
    }
}
