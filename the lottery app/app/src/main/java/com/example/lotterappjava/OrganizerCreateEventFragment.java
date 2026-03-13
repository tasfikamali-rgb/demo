package com.example.lotterappjava;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import com.example.lotterappjava.databinding.FragmentOrganizerCreateEventBinding;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class OrganizerCreateEventFragment extends Fragment {

    private static final String TAG = "CreateEvent";
    private FragmentOrganizerCreateEventBinding binding;
    private EventController eventController;
    private String organizerId;
    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private String eventIdToEdit;
    private boolean isEditMode = false;
    private String currentPosterUrl;
    private String currentQrCodeUrl;

    private ActivityResultLauncher<Intent> posterPickerLauncher;
    private ActivityResultLauncher<Intent> qrPickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventController = new EventController();
        organizerId = DeviceIdManager.getDeviceId(requireContext());
        
        if (getArguments() != null) {
            eventIdToEdit = getArguments().getString("eventId");
            isEditMode = eventIdToEdit != null;
        }

        posterPickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        uploadImage(imageUri, true);
                    }
                });

        qrPickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        uploadImage(imageUri, false);
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrganizerCreateEventBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        binding.editEventDate.setOnClickListener(v -> showDatePicker(binding.editEventDate));
        binding.editStartTime.setOnClickListener(v -> showTimePicker(binding.editStartTime));
        binding.editEndTime.setOnClickListener(v -> showTimePicker(binding.editEndTime));
        
        binding.editRegOpen.setOnClickListener(v -> showDatePicker(binding.editRegOpen));
        binding.editRegClose.setOnClickListener(v -> showDatePicker(binding.editRegClose));

        binding.btnUploadPoster.setOnClickListener(v -> openImagePicker(posterPickerLauncher));
        binding.btnUploadQr.setOnClickListener(v -> openImagePicker(qrPickerLauncher));

        if (isEditMode) {
            binding.textTitle.setText("Edit Event");
            binding.btnPublish.setText("Save Changes");
            loadEventData();
        }

        binding.btnPublish.setOnClickListener(v -> publishEvent());
    }

    private void openImagePicker(ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        launcher.launch(intent);
    }

    private void uploadImage(Uri uri, boolean isPoster) {
        if (uri == null) return;
        
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        
        String path = isPoster ? "event-posters/" : "event-qrs/";
        final String fileName = path + UUID.randomUUID().toString() + ".jpg";
        final StorageReference fileRef = storageRef.child(fileName);
        
        Toast.makeText(getContext(), "Uploading image...", Toast.LENGTH_SHORT).show();
        
        fileRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    String imageUrl = downloadUri.toString();
                    Log.d(TAG, "Upload success: " + imageUrl);
                    
                    if (isPoster) {
                        currentPosterUrl = imageUrl;
                        binding.imagePosterPreview.setVisibility(View.VISIBLE);
                        binding.textUploadPoster.setText("Tap to change event poster");
                        Glide.with(this).load(currentPosterUrl).into(binding.imagePosterPreview);
                    } else {
                        currentQrCodeUrl = imageUrl;
                        binding.imageQrPreview.setVisibility(View.VISIBLE);
                        binding.textUploadQr.setText("Tap to change promotional QR code");
                        Glide.with(this).load(currentQrCodeUrl).into(binding.imageQrPreview);
                    }
                    Toast.makeText(getContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show();
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

    private void loadEventData() {
        eventController.getEvent(eventIdToEdit, event -> {
            if (event != null && binding != null) {
                // Check if the current user is the owner
                if (!organizerId.equals(event.getOrganizerId())) {
                    Toast.makeText(getContext(), "Access denied: You are not the organizer of this event", Toast.LENGTH_LONG).show();
                    NavHostFragment.findNavController(this).navigateUp();
                    return;
                }

                binding.editEventTitle.setText(event.getTitle());
                binding.editDescription.setText(event.getDescription());
                if (event.getEventDate() != null) binding.editEventDate.setText(dateFormat.format(event.getEventDate()));
                if (event.getEventStartTime() != null) binding.editStartTime.setText(timeFormat.format(event.getEventStartTime()));
                if (event.getEventEndTime() != null) binding.editEndTime.setText(timeFormat.format(event.getEventEndTime()));

                binding.editPrice.setText(String.valueOf(event.getPrice()));
                binding.editCapacity.setText(String.valueOf(event.getCapacity()));
                if (event.getMaxWaitingListEntrants() != null) binding.editWaitlistLimit.setText(String.valueOf(event.getMaxWaitingListEntrants()));
                if (event.getRegistrationStart() != null) binding.editRegOpen.setText(dateFormat.format(event.getRegistrationStart()));
                if (event.getRegistrationEnd() != null) binding.editRegClose.setText(dateFormat.format(event.getRegistrationEnd()));
                binding.switchLocation.setChecked(event.isGeolocationRequired());

                if (event.getPosterUrl() != null && !event.getPosterUrl().isEmpty()) {
                    currentPosterUrl = event.getPosterUrl();
                    binding.imagePosterPreview.setVisibility(View.VISIBLE);
                    binding.textUploadPoster.setText("Tap to change event poster");
                    Glide.with(this).load(currentPosterUrl).into(binding.imagePosterPreview);
                }
                if (event.getQrCodeUrl() != null && !event.getQrCodeUrl().isEmpty()) {
                    currentQrCodeUrl = event.getQrCodeUrl();
                    binding.imageQrPreview.setVisibility(View.VISIBLE);
                    binding.textUploadQr.setText("Tap to change promotional QR code");
                    Glide.with(this).load(currentQrCodeUrl).into(binding.imageQrPreview);
                }
            } else if (binding != null) {
                Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(this).navigateUp();
            }
        });
    }

    private void showDatePicker(android.widget.EditText editText) {
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            editText.setText(dateFormat.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker(android.widget.EditText editText) {
        new TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            editText.setText(timeFormat.format(calendar.getTime()));
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    private void publishEvent() {
        String title = binding.editEventTitle.getText().toString().trim();
        String description = binding.editDescription.getText().toString().trim();
        String dateStr = binding.editEventDate.getText().toString().trim();
        String startTimeStr = binding.editStartTime.getText().toString().trim();
        String endTimeStr = binding.editEndTime.getText().toString().trim();
        String capStr = binding.editCapacity.getText().toString().trim();
        String priceStr = binding.editPrice.getText().toString().trim();
        String waitlistStr = binding.editWaitlistLimit.getText().toString().trim();
        String regOpenStr = binding.editRegOpen.getText().toString().trim();
        String regCloseStr = binding.editRegClose.getText().toString().trim();
        boolean geoRequired = binding.switchLocation.isChecked();

        if (title.isEmpty() || dateStr.isEmpty() || capStr.isEmpty()) {
            Toast.makeText(getContext(), "Title, Date and Capacity are required", Toast.LENGTH_SHORT).show();
            return;
        }

        Event event = new Event();
        if (isEditMode) {
            event.setEventId(eventIdToEdit);
        }
        event.setPosterUrl(currentPosterUrl);
        event.setQrCodeUrl(currentQrCodeUrl);
        event.setOrganizerId(organizerId);
        event.setTitle(title);
        event.setDescription(description);
        try {
            event.setEventDate(dateFormat.parse(dateStr));
            if (!startTimeStr.isEmpty()) event.setEventStartTime(timeFormat.parse(startTimeStr));
            if (!endTimeStr.isEmpty()) event.setEventEndTime(timeFormat.parse(endTimeStr));
            if (!regOpenStr.isEmpty()) event.setRegistrationStart(dateFormat.parse(regOpenStr));
            if (!regCloseStr.isEmpty()) event.setRegistrationEnd(dateFormat.parse(regCloseStr));
        } catch (Exception ignored) {}
        
        event.setCapacity(Integer.parseInt(capStr));
        if (!priceStr.isEmpty()) event.setPrice(Double.parseDouble(priceStr));
        if (!waitlistStr.isEmpty()) event.setMaxWaitingListEntrants(Integer.parseInt(waitlistStr));
        event.setGeolocationRequired(geoRequired);

        if (isEditMode) {
            // Verify ownership again before saving
            eventController.getEvent(eventIdToEdit, existingEvent -> {
                if (existingEvent != null && organizerId.equals(existingEvent.getOrganizerId())) {
                    eventController.createEvent(event, organizerId, success -> {
                        if (success) {
                            Toast.makeText(getContext(), "Event updated successfully", Toast.LENGTH_SHORT).show();
                            NavHostFragment.findNavController(this).navigateUp();
                        } else {
                            Toast.makeText(getContext(), "Failed to update event", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            eventController.createEvent(event, organizerId, success -> {
                if (success) {
                    Toast.makeText(getContext(), "Event published successfully", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(this).navigateUp();
                } else {
                    Toast.makeText(getContext(), "Failed to publish event", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
