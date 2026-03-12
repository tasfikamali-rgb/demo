package com.example.lotterappjava;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Handles Firebase Authentication and sync with Firestore user documents.
 * User documents are keyed by uid (Firebase Auth UID) for signed-in users,
 * and deviceId for anonymous device-only users.
 */
public class AuthManager {

    private static final String TAG = "AuthManager";

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final UserController userController = new UserController();

    public interface AuthResultListener {
        void onSuccess(User user);
        default void onError(String message) {}
    }

    /**
     * Check if there is already a signed-in Firebase user; if so, load their Firestore user and return.
     * Used on app start / login screen to skip login when already authenticated.
     */
    public void getCurrentUserIfSignedIn(Context context, AuthResultListener listener) {
        FirebaseUser fbUser = auth.getCurrentUser();
        if (fbUser == null) {
            listener.onSuccess(null);
            return;
        }
        
        db.collection("users").document(fbUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            listener.onSuccess(user);
                            return;
                        }
                    }
                    listener.onSuccess(null);
                })
                .addOnFailureListener(e -> listener.onSuccess(null));
    }

    /**
     * Sign in with email and password. Updates or creates Firestore user doc for this uid.
     */
    public void signInWithEmail(Context context, String email, String password, AuthResultListener listener) {
        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            listener.onError("Please enter email and password.");
            return;
        }
        auth.signInWithEmailAndPassword(email.trim(), password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                        FirebaseUser fbUser = task.getResult().getUser();
                        syncFirestoreUserAfterAuth(context, fbUser.getUid(), fbUser.getEmail(), null, null, password, listener);
                    } else {
                        String message = "Sign in failed.";
                        if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                            message = "No account found with this email.";
                        } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            message = "Invalid password.";
                        } else if (task.getException() != null && task.getException().getMessage() != null) {
                            message = task.getException().getMessage();
                        }
                        listener.onError(message);
                    }
                });
    }

    /**
     * Create account with name, email, password, and role. Creates Firestore user doc for this uid.
     */
    public void signUpWithEmail(Context context, String name, String email, String password, String role, AuthResultListener listener) {
        if (name == null || name.trim().isEmpty()) {
            listener.onError("Please enter your name.");
            return;
        }
        if (email == null || email.trim().isEmpty() || password == null || password.length() < 6) {
            listener.onError("Please enter a valid email and password (at least 6 characters).");
            return;
        }
        
        if (role == null || role.isEmpty()) {
            listener.onError("Please select a role.");
            return;
        }

        auth.createUserWithEmailAndPassword(email.trim(), password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                        FirebaseUser fbUser = task.getResult().getUser();
                        syncFirestoreUserAfterAuth(context, fbUser.getUid(), fbUser.getEmail(), name.trim(), role, password, listener);
                    } else {
                        String message = "Sign up failed.";
                        if (task.getException() != null && task.getException().getMessage() != null) {
                            String msg = task.getException().getMessage();
                            if (msg.contains("email address is already in use")) {
                                message = "This email is already registered.";
                            } else {
                                message = msg;
                            }
                        }
                        listener.onError(message);
                    }
                });
    }

    /**
     * Continue as entrant using device ID only (no email/password). US 01.07.01.
     * Also signs in anonymously to Firebase Auth to satisfy Storage rules.
     */
    public void continueWithDevice(Context context, AuthResultListener listener) {
        auth.signInAnonymously().addOnCompleteListener(authTask -> {
            if (authTask.isSuccessful()) {
                String deviceId = DeviceIdManager.getDeviceId(context);
                db.collection("users").document(deviceId).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                User existingUser = documentSnapshot.toObject(User.class);
                                if (existingUser != null) {
                                    existingUser.setRole(User.ROLE_ENTRANT);
                                    userController.updateUser(existingUser, aVoid -> listener.onSuccess(existingUser));
                                    return;
                                }
                            }
                            final User newUser = new User(deviceId, User.ROLE_ENTRANT);
                            newUser.setName("New User");
                            db.collection("users").document(deviceId).set(newUser)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Device-only entrant created: " + deviceId);
                                        listener.onSuccess(newUser);
                                    })
                                    .addOnFailureListener(e -> listener.onError("Could not create profile. Please try again."));
                        })
                        .addOnFailureListener(e -> listener.onError("Could not load profile. Please try again."));
            } else {
                listener.onError("Authentication failed: " + (authTask.getException() != null ? authTask.getException().getMessage() : "Unknown error"));
            }
        });
    }

    /**
     * After Firebase Auth sign-in/sign-up: ensure Firestore users/{uid} has uid, email, name, role.
     */
    private void syncFirestoreUserAfterAuth(Context context, String uid, String email, String name, String role, String password, AuthResultListener listener) {
        String deviceId = DeviceIdManager.getDeviceId(context);
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User userTemp;
                    if (documentSnapshot.exists()) {
                        userTemp = documentSnapshot.toObject(User.class);
                        if (userTemp == null) userTemp = new User(deviceId);
                    } else {
                        userTemp = new User(deviceId);
                    }
                    final User user = userTemp;
                    user.setUid(uid);
                    user.setEmail(email != null ? email : "");
                    if (name != null && !name.isEmpty()) user.setName(name);
                    
                    if (role != null && !role.isEmpty()) {
                        user.setRole(role);
                    }

                    // Save password to Firestore, but it's not shown in UI
                    if (password != null) user.setPassword(password);
                    
                    db.collection("users").document(uid).set(user)
                            .addOnSuccessListener(aVoid -> listener.onSuccess(user))
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error syncing user to Firestore", e);
                                listener.onError("Account created but failed to save profile. Please try again.");
                            });
                })
                .addOnFailureListener(e -> {
                    final User user = new User(deviceId);
                    user.setUid(uid);
                    user.setEmail(email != null ? email : "");
                    if (name != null && !name.isEmpty()) user.setName(name);
                    
                    user.setRole(role != null && !role.isEmpty() ? role : User.ROLE_ENTRANT);
                    
                    if (password != null) user.setPassword(password);

                    db.collection("users").document(uid).set(user)
                            .addOnSuccessListener(aVoid -> listener.onSuccess(user))
                            .addOnFailureListener(e2 -> listener.onError("Could not save profile."));
                });
    }

    public void signOut() {
        auth.signOut();
    }

    public boolean isSignedInWithEmail() {
        return auth.getCurrentUser() != null;
    }
}
