package com.example.lotterappjava;

import android.app.Activity;
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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.lotterappjava.databinding.FragmentOrganizerProfileBinding;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class OrganizerProfileFragment extends Fragment {

    private static final String TAG = "OrganizerProfile";
    private FragmentOrganizerProfileBinding binding;
    private FacilityController facilityController;
    private String organizerId;
    private Facility currentFacility;
    private Uri imageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        facilityController = new FacilityController();
        organizerId = DeviceIdManager.getDeviceId(requireContext());

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        uploadFacilityImage();
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrganizerProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        
        loadFacility();

        binding.btnEditFacilityImage.setOnClickListener(v -> openImagePicker());

        binding.btnSaveFacility.setOnClickListener(v -> saveFacility());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void uploadFacilityImage() {
        if (imageUri == null) return;
        if (organizerId == null || organizerId.isEmpty()) {
            Toast.makeText(getContext(), "Error: Device ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Explicitly using the bucket from google-services.json to avoid 404 if default fails
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        
        final String fileName = "facility-images/" + organizerId + "/" + UUID.randomUUID().toString() + ".jpg";
        final StorageReference facilityImageRef = storageRef.child(fileName);
        
        Toast.makeText(getContext(), "Uploading facility image...", Toast.LENGTH_SHORT).show();
        
        facilityImageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> facilityImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    Log.d(TAG, "Upload success: " + imageUrl);
                    
                    if (currentFacility == null) {
                        currentFacility = new Facility(organizerId, "", "");
                    }
                    
                    // Delete old image if exists
                    if (currentFacility.getImageUrl() != null && !currentFacility.getImageUrl().isEmpty()) {
                        try {
                            StorageReference oldRef = storage.getReferenceFromUrl(currentFacility.getImageUrl());
                            oldRef.delete().addOnFailureListener(e -> Log.w(TAG, "Failed to delete old image", e));
                        } catch (Exception ignored) {}
                    }

                    currentFacility.setImageUrl(imageUrl);
                    facilityController.updateFacility(currentFacility, aVoid -> {
                        Toast.makeText(getContext(), "Facility image updated", Toast.LENGTH_SHORT).show();
                        displayFacility();
                    });
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

    private void loadFacility() {
        facilityController.getFacility(organizerId, facility -> {
            if (facility != null) {
                currentFacility = facility;
                displayFacility();
            } else {
                currentFacility = new Facility(organizerId, "", "");
            }
        });
    }

    private void displayFacility() {
        if (binding == null || currentFacility == null) return;
        binding.editFacilityName.setText(currentFacility.getName());
        binding.editFacilityLocation.setText(currentFacility.getLocation());

        if (currentFacility.getImageUrl() != null && !currentFacility.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentFacility.getImageUrl())
                    .into(binding.imageFacility);
        }
    }

    private void saveFacility() {
        String name = binding.editFacilityName.getText().toString().trim();
        String location = binding.editFacilityLocation.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Facility name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentFacility == null) {
            currentFacility = new Facility(organizerId, name, location);
        } else {
            currentFacility.setName(name);
            currentFacility.setLocation(location);
        }

        facilityController.updateFacility(currentFacility, aVoid -> {
            Toast.makeText(getContext(), "Facility profile saved", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
