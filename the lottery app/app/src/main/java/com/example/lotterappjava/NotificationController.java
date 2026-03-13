package com.example.lotterappjava;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Controller class for managing Notification-related data operations in Firestore.
 * This class follows the Controller component of the MVC design pattern,
 * providing methods to send and retrieve notifications for users and administrators.
 *
 * Outstanding issues:
 * - Real-time listeners may fail if Firestore indexes are not properly configured.
 */
public class NotificationController {
    private static final String TAG = "NotificationController";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Listener interface for loading a list of notifications.
     */
    public interface OnNotificationsLoadedListener {
        /**
         * Called when notifications are loaded.
         * @param notifications The list of Notification objects.
         */
        void onNotificationsLoaded(List<Notification> notifications);
    }

    /**
     * Sends a group notification to a list of recipients and logs the action for administrators.
     * Logs the action once in the 'notifications' collection with userId = null for admin visibility,
     * and sends individual notifications to each recipient.
     *
     * @param eventId The identifier of the event associated with the notification.
     * @param organizerId The identifier of the organizer sending the notification.
     * @param targetGroup The group being notified (e.g., "all", "waiting").
     * @param message The content of the notification (truncated to 50 characters).
     * @param recipients The list of User objects to receive the notification.
     */
    public void sendGroupNotification(String eventId, String organizerId, String targetGroup, String message, List<User> recipients) {
        if (recipients == null || recipients.isEmpty()) {
            Log.d(TAG, "No recipients to send notification to.");
            return;
        }

        // message to only 50 characters
        String finalMessage = message.length() > 50 ? message.substring(0, 50) : message;


        String logId = UUID.randomUUID().toString();
        Notification groupLog = new Notification(logId, eventId, organizerId, finalMessage, null, targetGroup);
        
        db.collection("notifications").document(logId).set(groupLog)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Group notification log saved to Firestore.");
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error logging group notification", e));


        for (User user : recipients) {
            String notificationId = UUID.randomUUID().toString();
            

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
     * Sets up a real-time listener for administrator notification logs.
     *
     * @param listener The callback listener for changes in admin logs.
     * @return A ListenerRegistration object to manage the listener's lifecycle.
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

    /**
     * Fallback method to fetch all notifications and filter for admin logs manually if indexing fails.
     */
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
     * Sets up a real-time listener for personal notifications for a specific user.
     *
     * @param userId The ID of the user to listen for notifications.
     * @param listener The callback listener for notification updates.
     * @return A ListenerRegistration object.
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

    /**
     * Retrieves all notifications for a specific user from Firestore.
     *
     * @param userId The ID of the user.
     * @param listener The callback listener for the results.
     */
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

    /**
     * Retrieves all administrator notification logs from Firestore.
     *
     * @param listener The callback listener for the results.
     */
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
