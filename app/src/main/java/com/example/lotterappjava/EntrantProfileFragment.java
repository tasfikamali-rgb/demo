package com.example.lotterappjava;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.lotterappjava.databinding.FragmentEntrantProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

/**
 * Fragment that displays and allows editing of the Entrant's profile.
 * This class follows the View component of the MVC design pattern, managing
 * user input for profile details and image uploads to Firebase Storage.
 *
 * Outstanding issues:
 * - Profile deletion does not currently handle removal from Firebase Authentication.
 */
public class EntrantProfileFragment extends Fragment {

    private static final String TAG = "EntrantProfile";
    private FragmentEntrantProfileBinding binding;
    private UserController userController;
    private String userId; // Standardized ID (UID or DeviceID)
    private User currentUser;
    private Uri imageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEntrantProfileBinding.inflate(inflater, container, false);
        userController = new UserController();
        
        // Consistent ID resolution
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fbUser != null) {
            userId = fbUser.getUid();
        } else {
            userId = DeviceIdManager.getDeviceId(requireContext());
        }

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        uploadProfileImage();
                    }
                });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        
        loadUserProfile();

        binding.btnEditImage.setOnClickListener(v -> openImagePicker());

        binding.btnSaveChanges.setOnClickListener(v -> saveChanges());

        binding.btnDeleteProfile.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                .setTitle("Delete Profile")
                .setMessage("Are you sure you want to delete your profile? This action cannot be undone.")
                .setPositiveButton("Yes", (dialog, which) -> deleteProfile())
                .setNegativeButton("No", null)
                .show();
        });
    }

    /**
     * Saves the changes made to the user's profile information to Firestore.
     */
    private void saveChanges() {
        if (currentUser == null) return;

        String name = binding.editFullName.getText().toString().trim();
        String email = binding.editEmail.getText().toString().trim();
        String phone = binding.editPhone.getText().toString().trim();
        boolean notifications = binding.switchNotifications.isChecked();

        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser.setName(name);
        currentUser.setEmail(email);
        currentUser.setPhoneNumber(phone);
        currentUser.setNotificationsEnabled(notifications);

        userController.updateUser(currentUser, aVoid -> {
            Toast.makeText(getContext(), "Changes saved successfully", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Deletes the user's profile document and their profile image from storage.
     */
    private void deleteProfile() {
        if (currentUser != null && currentUser.getProfileImageUrl() != null && !currentUser.getProfileImageUrl().isEmpty()) {
            try {
                StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(currentUser.getProfileImageUrl());
                photoRef.delete();
            } catch (Exception e) {
                // Ignore if the file doesn't exist or URL is malformed
            }
        }

        userController.deleteUser(userId, aVoid -> {
            Toast.makeText(getContext(), "Profile deleted successfully", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).navigate(R.id.loginFragment);
        });
    }

    /**
     * Opens an image picker to select a new profile picture.
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    /**
     * Uploads the selected profile image to Firebase Storage and updates the user's profile URL.
     */
    private void uploadProfileImage() {
        if (imageUri == null) return;
        
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        
        final String fileName = "profile-images/" + userId + "/" + UUID.randomUUID().toString() + ".jpg";
        final StorageReference profileImageRef = storageRef.child(fileName);
        
        Toast.makeText(getContext(), "Uploading profile image...", Toast.LENGTH_SHORT).show();
        
        profileImageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    Log.d(TAG, "Upload success: " + imageUrl);
                    
                    if (currentUser != null && currentUser.getProfileImageUrl() != null && !currentUser.getProfileImageUrl().isEmpty()) {
                        try {
                            StorageReference oldRef = storage.getReferenceFromUrl(currentUser.getProfileImageUrl());
                            oldRef.delete().addOnFailureListener(e -> Log.w(TAG, "Failed to delete old image", e));
                        } catch (Exception ignored) {}
                    }

                    if (currentUser != null) {
                        currentUser.setProfileImageUrl(imageUrl);
                        userController.updateUser(currentUser, aVoid -> {
                            Toast.makeText(getContext(), "Profile picture updated", Toast.LENGTH_SHORT).show();
                            loadUserProfile();
                        });
                    }
                }))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Upload failed", e);
                    String errorMsg = e.getMessage();
                    if (errorMsg != null && errorMsg.contains("404")) {
                        errorMsg = "Storage bucket not found. Please ensure Storage is enabled in Firebase Console.";
                    }
                    Toast.makeText(getContext(), "Upload failed: " + errorMsg, Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Loads the user's profile data from Firestore.
     */
    private void loadUserProfile() {
        userController.getUser(userId, user -> {
            if (user == null) {
                // Handle case where user document doesn't exist yet
                currentUser = new User(userId);
                currentUser.setName("New User");
                currentUser.setUid(userId);
                userController.updateUser(currentUser, aVoid -> displayUser(currentUser));
            } else {
                currentUser = user;
                displayUser(user);
            }
        });
    }

    /**
     * Updates the UI components with the provided user's data.
     * @param user The User object to display.
     */
    private void displayUser(User user) {
        if (binding == null) return;
        binding.textDeviceId.setText("ID: " + user.getDeviceId());
        binding.editFullName.setText(user.getName());
        binding.editEmail.setText(user.getEmail());
        binding.editPhone.setText(user.getPhoneNumber());
        binding.switchNotifications.setChecked(user.isNotificationsEnabled());

        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(user.getProfileImageUrl())
                    .circleCrop()
                    .into(binding.imageProfile);
        } else {
            Glide.with(this)
                    .load(android.R.drawable.ic_menu_myplaces)
                    .circleCrop()
                    .into(binding.imageProfile);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
