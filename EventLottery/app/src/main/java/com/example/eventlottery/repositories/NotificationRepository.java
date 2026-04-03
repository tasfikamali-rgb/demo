package com.example.eventlottery.repositories;

import com.example.eventlottery.models.AppNotification;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class NotificationRepository {
    private static final String COLLECTION = "notifications";
    private final CollectionReference notifRef;

    public NotificationRepository() {
        notifRef = FirebaseFirestore.getInstance().collection(COLLECTION);
    }

    public Task<Void> createNotification(AppNotification notification) {
        return notifRef.document().set(notification);
    }

    public Task<QuerySnapshot> getNotificationsForUser(String userId) {
        return notifRef.whereEqualTo("userId", userId)
                .get();
    }

    public Task<QuerySnapshot> getUnreadNotifications(String userId) {
        return notifRef.whereEqualTo("userId", userId)
                .whereEqualTo("read", false)
                .get();
    }

    public Task<Void> markAsRead(String notificationId) {
        return notifRef.document(notificationId).update("read", true);
    }

    public Task<Void> updateActionRequired(String notificationId, boolean required) {
        return notifRef.document(notificationId).update("actionRequired", required);
    }

    public Task<Void> markAllReadForUser(String userId) {
        return getUnreadNotifications(userId).continueWithTask(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                return Tasks.forResult(null);
            }
            List<Task<Void>> updates = new ArrayList<>();
            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                updates.add(notifRef.document(doc.getId()).update("read", true));
            }
            return updates.isEmpty() ? Tasks.forResult(null) : Tasks.whenAll(updates);
        });
    }

    public Task<QuerySnapshot> getAllNotificationLogs() {
        return notifRef.orderBy("createdAt", Query.Direction.DESCENDING).limit(200).get();
    }

    public Task<Void> deleteNotification(String notificationId) {
        return notifRef.document(notificationId).delete();
    }
}
