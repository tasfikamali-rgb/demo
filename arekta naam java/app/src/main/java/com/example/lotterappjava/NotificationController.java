package com.example.lotterappjava;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
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

    /**
     * Sends a group notification.
     * Logs the action once in the 'notifications' collection for admin visibility (userId = null),
     * and sends individual notifications to each recipient.
     */
    public void sendGroupNotification(String eventId, String organizerId, String targetGroup, String message, List<User> recipients) {
        if (recipients == null || recipients.isEmpty()) {
            Log.d(TAG, "No recipients to send notification to.");
            return;
        }

        // Limit message to 50 characters
        String finalMessage = message.length() > 50 ? message.substring(0, 50) : message;

        // 1. Create a SINGLE log entry for the admin (userId is null)
        String logId = UUID.randomUUID().toString();
        Notification groupLog = new Notification(logId, eventId, organizerId, finalMessage, null, targetGroup);
        
        db.collection("notifications").document(logId).set(groupLog)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Group notification log saved to Firestore.");
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error logging group notification", e));

        // 2. Send personal notifications to each user
        for (User user : recipients) {
            String notificationId = UUID.randomUUID().toString();
            
            // CRITICAL: Use UID if available, else DeviceID
            String recipientId = (user.getUid() != null && !user.getUid().isEmpty()) ? user.getUid() : user.getDeviceId();
            
            String recipientName = (user.getName() != null && !user.getName().isEmpty()) ? user.getName() : recipientId;
            
            Notification personalNotif = new Notification(notificationId, eventId, organizerId, finalMessage, recipientId, targetGroup);
            
            db.collection("notifications").document(notificationId).set(personalNotif)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "--------------------------------------------------");
                    Log.d(TAG, "message sent by " + organizerId);
                    Log.d(TAG, "message received by " + recipientName);
                    Log.d(TAG, "message states \"" + finalMessage + "\"");
                    Log.d(TAG, "--------------------------------------------------");
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to send notification to user: " + recipientId, e));
        }
    }

    /**
     * Real-time listener for Admin logs (where userId is null).
     */
    public ListenerRegistration listenForAdminLogs(OnNotificationsLoadedListener listener) {
        return db.collection("notifications")
                .whereEqualTo("userId", null)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w(TAG, "Listen failed for admin logs.", error);
                        // Fallback: try without userId filter if index is missing
                        fetchAllAndFilterForAdmin(listener);
                        return;
                    }

                    List<Notification> notifications = new ArrayList<>();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            notifications.add(doc.toObject(Notification.class));
                        }
                    }
                    listener.onNotificationsLoaded(notifications);
                });
    }

    private void fetchAllAndFilterForAdmin(OnNotificationsLoadedListener listener) {
        db.collection("notifications").get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Notification> logs = new ArrayList<>();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Notification n = doc.toObject(Notification.class);
                if (n.getUserId() == null) {
                    logs.add(n);
                }
            }
            // Sort by timestamp descending
            logs.sort((n1, n2) -> n2.getTimestamp().compareTo(n1.getTimestamp()));
            listener.onNotificationsLoaded(logs);
        });
    }

    /**
     * Real-time listener for personal notifications.
     */
    public ListenerRegistration listenForUserNotifications(String userId, OnNotificationsLoadedListener listener) {
        return db.collection("notifications")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w(TAG, "Listen failed for user notifications: " + userId, error);
                        return;
                    }

                    List<Notification> notifications = new ArrayList<>();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            notifications.add(doc.toObject(Notification.class));
                        }
                    }
                    listener.onNotificationsLoaded(notifications);
                });
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
                    listener.onNotificationsLoaded(new ArrayList<>());
                });
    }

    public void getAdminLogs(OnNotificationsLoadedListener listener) {
        db.collection("notifications")
                .whereEqualTo("userId", null)
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
                    Log.w(TAG, "Error getting admin logs", e);
                    fetchAllAndFilterForAdmin(listener);
                });
    }
}
