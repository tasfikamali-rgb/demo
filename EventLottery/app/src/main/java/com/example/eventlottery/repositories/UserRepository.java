package com.example.eventlottery.repositories;

import com.example.eventlottery.models.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private static final String COLLECTION = "users";
    private final CollectionReference usersRef;

    public UserRepository() {
        usersRef = FirebaseFirestore.getInstance().collection(COLLECTION);
    }

    public Task<DocumentReference> addUser(User user) {
        return usersRef.add(user);
    }

    public Task<Void> updateUser(String userId, User user) {
        return usersRef.document(userId).set(user);
    }

    public Task<DocumentSnapshot> getUserById(String userId) {
        return usersRef.document(userId).get();
    }

    public Task<QuerySnapshot> getUserByDeviceId(String deviceId) {
        return usersRef.whereEqualTo("deviceId", deviceId).get();
    }

    public Task<QuerySnapshot> getUserByEmail(String email) {
        // Search by lowercase email for new data
        return usersRef.whereEqualTo("email", email.toLowerCase()).limit(1).get();
    }

    // Support for legacy case-sensitive email search
    public Task<QuerySnapshot> getUserByEmailOriginal(String email) {
        return usersRef.whereEqualTo("email", email).limit(1).get();
    }

    public Task<QuerySnapshot> searchUsers(String query) {
        String lowerQuery = query.toLowerCase();
        Task<QuerySnapshot> emailTask = usersRef.whereEqualTo("email", lowerQuery).get();
        Task<QuerySnapshot> phoneTask = usersRef.whereEqualTo("phone", query).get();
        Task<QuerySnapshot> nameTask = usersRef.orderBy("nameLowercase").startAt(lowerQuery).endAt(lowerQuery + "\uf8ff").get();

        return Tasks.whenAllComplete(emailTask, phoneTask, nameTask).continueWith(task -> {
            return null; 
        });
    }
    
    public Task<QuerySnapshot> searchByName(String namePrefix) {
        // Case-insensitive prefix search using the nameLowercase field
        String lowerPrefix = namePrefix.toLowerCase();
        return usersRef.orderBy("nameLowercase").startAt(lowerPrefix).endAt(lowerPrefix + "\uf8ff").get();
    }

    // Support for legacy case-sensitive name prefix search
    public Task<QuerySnapshot> searchByNameOriginal(String namePrefix) {
        return usersRef.orderBy("name").startAt(namePrefix).endAt(namePrefix + "\uf8ff").get();
    }

    public Task<QuerySnapshot> searchByPhone(String phone) {
        return usersRef.whereEqualTo("phone", phone).get();
    }

    public Task<QuerySnapshot> getAllUsers() {
        return usersRef.orderBy("name", Query.Direction.ASCENDING).get();
    }

    public Task<Void> deleteUser(String userId) {
        return usersRef.document(userId).delete();
    }

    public Task<Void> updateFcmToken(String userId, String token) {
        return usersRef.document(userId).update("fcmToken", token);
    }

    public Task<Void> updateNotificationPreference(String userId, boolean enabled) {
        return usersRef.document(userId).update("pushNotificationsEnabled", enabled);
    }
}
