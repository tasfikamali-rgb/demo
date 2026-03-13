package com.example.lotterappjava;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.lotterappjava.databinding.FragmentEntrantEventDetailsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EntrantEventDetailsFragment extends Fragment {

    private FragmentEntrantEventDetailsBinding binding;
    private String eventId;
    private EventController eventController;
    private UserController userController;
    private String userId;
    private boolean onWaitlist = false;
    private String currentStatus = null; // waiting, invited, enrolled, cancelled
    private boolean geoRequired = false;
    private Date registrationEnd;
    private Date registrationStart;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }
        eventController = new EventController();
        userController = new UserController();
        
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fbUser != null) {
            userId = fbUser.getUid();
        } else {
            userId = DeviceIdManager.getDeviceId(requireContext());
        }
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentEntrantEventDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (eventId != null) {
            fetchEventDetails();
            loadEntrantStatus();
            fetchWaitlistCount();
        } else {
            Toast.makeText(getContext(), "Error: Event ID missing", Toast.LENGTH_SHORT).show();
        }

        binding.btnJoinWaitlist.setOnClickListener(v -> {
            if (eventId != null) {
                if (onWaitlist) {
                    leaveWaitlist();
                } else {
                    checkRegistrationAndJoin();
                }
            }
        });

        binding.btnAcceptInvite.setOnClickListener(v -> respondToInvitation(true));
        binding.btnDeclineInvite.setOnClickListener(v -> respondToInvitation(false));
    }

    private void checkRegistrationAndJoin() {
        Date now = new Date();
        if (registrationStart != null && now.before(registrationStart)) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Toast.makeText(getContext(), "Registration opens on " + sdf.format(registrationStart), Toast.LENGTH_LONG).show();
            return;
        }
        if (registrationEnd != null && now.after(registrationEnd)) {
            Toast.makeText(getContext(), "Registration period has ended.", Toast.LENGTH_LONG).show();
            return;
        }
        joinWaitlist();
    }

    private void fetchEventDetails() {
        eventController.getAllEvents(events -> {
            for (Event event : events) {
                if (event.getEventId().equals(eventId)) {
                    binding.textEventTitle.setText(event.getTitle());
                    
                    String desc = event.getDescription();
                    if (event.getPrice() > 0) {
                        desc = "Price: $" + String.format(Locale.getDefault(), "%.2f", event.getPrice()) + "\n\n" + desc;
                    }
                    binding.textEventDescription.setText(desc);
                    
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    if (event.getEventDate() != null) {
                        binding.textEventDate.setText("Event Date: " + sdf.format(event.getEventDate()));
                    } else {
                        binding.textEventDate.setText("Date TBD");
                    }

                    registrationEnd = event.getRegistrationEnd();
                    registrationStart = event.getRegistrationStart();
                    if (registrationEnd != null) {
                        binding.textRegDeadline.setText("Register by: " + sdf.format(registrationEnd));
                        binding.textRegDeadline.setVisibility(View.VISIBLE);
                    } else {
                        binding.textRegDeadline.setVisibility(View.GONE);
                    }

                    geoRequired = event.isGeolocationRequired();
                    binding.textGeolocked.setText(geoRequired ? "Geo-locked" : "No Geo-lock");

                    if (event.getPosterUrl() != null && !event.getPosterUrl().isEmpty()) {
                        Glide.with(this)
                                .load(event.getPosterUrl())
                                .into(binding.imageEventPoster);
                    }

                    if (event.getQrCodeUrl() != null && !event.getQrCodeUrl().isEmpty()) {
                        Glide.with(this)
                                .load(event.getQrCodeUrl())
                                .into(binding.imageQrCode);
                    }
                    break;
                }
            }
        });
    }

    private void fetchWaitlistCount() {
        eventController.getEntrantsForEvent(eventId, entrants -> {
            if (binding != null) {
                binding.textWaitlistCount.setText(entrants.size() + " waitlisted");
            }
        });
    }

    private void loadEntrantStatus() {
        eventController.getEntrantStatus(eventId, userId, status -> {
            currentStatus = status;
            onWaitlist = "waiting".equals(status);
            if (binding != null) {
                updateWaitlistButton();
                updateInviteActions();
            }
        });
    }

    private void joinWaitlist() {
        if (!geoRequired) {
            // No geolocation needed for this event
            eventController.joinWaitlist(eventId, userId, null, null, success -> {
                if (success) {
                    Toast.makeText(getContext(), R.string.successfully_joined_waitlist, Toast.LENGTH_SHORT).show();
                    onWaitlist = true;
                    currentStatus = "waiting";
                    updateWaitlistButton();
                    fetchWaitlistCount();
                } else {
                    Toast.makeText(getContext(), R.string.waitlist_full, Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }

        // Geo-lock enabled: require a location sample
        if (getActivity() == null) return;

        if (!LocationHelper.hasLocationPermission(getActivity())) {
            LocationHelper.requestLocationPermission(getActivity());
            Toast.makeText(getContext(), "Please grant location permission and try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        LocationHelper.getCurrentLocation(getActivity(), new LocationHelper.LocationCallback() {
            @Override
            public void onLocationResult(Double latitude, Double longitude) {
                eventController.joinWaitlist(eventId, userId, latitude, longitude, success -> {
                    if (success) {
                        Toast.makeText(getContext(), R.string.successfully_joined_waitlist, Toast.LENGTH_SHORT).show();
                        onWaitlist = true;
                        currentStatus = "waiting";
                        updateWaitlistButton();
                        fetchWaitlistCount();
                    } else {
                        Toast.makeText(getContext(), R.string.waitlist_full, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getContext(), "Location required to join this event.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void leaveWaitlist() {
        eventController.leaveWaitlist(eventId, userId, success -> {
            if (success) {
                Toast.makeText(getContext(), R.string.successfully_left_waitlist, Toast.LENGTH_SHORT).show();
                onWaitlist = false;
                currentStatus = null;
                updateWaitlistButton();
                updateInviteActions();
                fetchWaitlistCount(); // Refresh count
            } else {
                Toast.makeText(getContext(), R.string.failed_to_leave_waitlist, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateWaitlistButton() {
        if (binding == null) return;
        if ("invited".equals(currentStatus) || "enrolled".equals(currentStatus)) {
            binding.btnJoinWaitlist.setEnabled(false);
            binding.btnJoinWaitlist.setText(getString(R.string.on_waitlist));
            binding.btnJoinWaitlist.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.button_purple_light));
        } else if (onWaitlist) {
            binding.btnJoinWaitlist.setText(R.string.leave_waitlist);
            binding.btnJoinWaitlist.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
        } else {
            binding.btnJoinWaitlist.setEnabled(true);
            binding.btnJoinWaitlist.setText(R.string.join_waitlist);
            binding.btnJoinWaitlist.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.button_purple_light));
        }
    }

    private void updateInviteActions() {
        if (binding == null) return;
        if ("invited".equals(currentStatus)) {
            binding.layoutInviteActions.setVisibility(View.VISIBLE);
        } else {
            binding.layoutInviteActions.setVisibility(View.GONE);
        }
    }

    private void respondToInvitation(boolean accept) {
        if (eventId == null || currentStatus == null || !"invited".equals(currentStatus)) return;
        String newStatus = accept ? "enrolled" : "cancelled";
        eventController.updateEntrantStatus(eventId, userId, newStatus, success -> {
            if (!success) return;
            currentStatus = newStatus;
            if (accept) {
                Toast.makeText(getContext(), "Invitation accepted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Invitation declined", Toast.LENGTH_SHORT).show();
            }
            updateWaitlistButton();
            updateInviteActions();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
