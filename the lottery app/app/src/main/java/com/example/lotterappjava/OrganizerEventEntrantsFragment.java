package com.example.lotterappjava;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lotterappjava.databinding.FragmentOrganizerEventEntrantsBinding;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class OrganizerEventEntrantsFragment extends Fragment {

    private FragmentOrganizerEventEntrantsBinding binding;
    private String eventId;
    private EventController eventController;
    private String currentStatus = "waiting";
    private final List<EventController.Participant> participantList = new ArrayList<>();
    // private ParticipantAdapter adapter; 

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }
        eventController = new EventController();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrganizerEventEntrantsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        binding.recyclerEntrants.setLayoutManager(new LinearLayoutManager(getContext()));
        // adapter = new ParticipantAdapter(participantList);
        // binding.recyclerEntrants.setAdapter(adapter);

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: currentStatus = "waiting"; break;
                    case 1: currentStatus = "invited"; break;
                    case 2: currentStatus = "enrolled"; break;
                    case 3: currentStatus = "cancelled"; break;
                }
                loadParticipants();
                updateUIForStatus();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        binding.btnDrawLottery.setOnClickListener(v -> showDrawDialog());
        binding.btnSendNotification.setOnClickListener(v -> showNotificationDialog());
        binding.btnViewMap.setOnClickListener(v -> {
            // US 02.02.02: Navigate to map view
            Bundle args = new Bundle();
            args.putString("eventId", eventId);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.organizerMapFragment, args);
        });
        binding.btnExportCsv.setOnClickListener(v -> exportToCSV());

        loadParticipants();
        updateUIForStatus();
    }

    private void updateUIForStatus() {
        if ("waiting".equals(currentStatus)) {
            binding.btnDrawLottery.setVisibility(View.VISIBLE);
            binding.btnDrawLottery.setText("Draw Lottery");
        } else if ("invited".equals(currentStatus)) {
            binding.btnDrawLottery.setVisibility(View.VISIBLE);
            binding.btnDrawLottery.setText("Draw Replacement");
        } else {
            binding.btnDrawLottery.setVisibility(View.GONE);
        }
    }

    private void loadParticipants() {
        eventController.getEntrantsWithStatus(eventId, currentStatus, participants -> {
            if (binding == null) return;
            participantList.clear();
            participantList.addAll(participants);
            // adapter.notifyDataSetChanged();
            
            // For now, let's just toast the count to confirm it works
            Toast.makeText(getContext(), "Loaded " + participants.size() + " participants", Toast.LENGTH_SHORT).show();
        });
    }

    private void showDrawDialog() {
        // US 02.05.02: Input number of attendees to sample
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle(currentStatus.equals("waiting") ? "Draw Lottery" : "Draw Replacement");
        
        final android.widget.EditText input = new android.widget.EditText(requireContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint("Number of entrants to select");
        builder.setView(input);

        builder.setPositiveButton("Draw", (dialog, which) -> {
            String val = input.getText().toString();
            if (!val.isEmpty()) {
                int num = Integer.parseInt(val);
                String organizerId = DeviceIdManager.getDeviceId(requireContext());
                if (currentStatus.equals("waiting")) {
                    eventController.drawLottery(eventId, organizerId, num, success -> {
                        if (success) {
                            Toast.makeText(getContext(), "Lottery drawn!", Toast.LENGTH_SHORT).show();
                            loadParticipants();
                        } else {
                            Toast.makeText(getContext(), "Failed to draw lottery", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // Draw replacement
                    eventController.drawReplacement(eventId, organizerId, success -> {
                        if (success) {
                            Toast.makeText(getContext(), "Replacement drawn!", Toast.LENGTH_SHORT).show();
                            loadParticipants();
                        } else {
                            Toast.makeText(getContext(), "Failed to draw replacement", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showNotificationDialog() {
        // US 02.07.01-03: Send notification to current group
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Send Notification to " + currentStatus + " list");
        
        final android.widget.EditText input = new android.widget.EditText(requireContext());
        input.setHint("Enter message here...");
        builder.setView(input);

        builder.setPositiveButton("Send", (dialog, which) -> {
            String message = input.getText().toString();
            if (!message.isEmpty()) {
                eventController.sendNotificationToGroup(eventId, currentStatus, message, success -> {
                    Toast.makeText(getContext(), "Notification sent!", Toast.LENGTH_SHORT).show();
                });
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void exportToCSV() {
        // US 02.06.05: Export final list
        if (!"enrolled".equals(currentStatus)) {
            Toast.makeText(getContext(), "Switch to 'Enrolled' tab to export final list", Toast.LENGTH_SHORT).show();
            return;
        }
        
        StringBuilder csv = new StringBuilder("Name,Email,Status\n");
        for (EventController.Participant p : participantList) {
            User u = p.getUser();
            csv.append(u.getName()).append(",").append(u.getEmail()).append(",").append(p.getStatus()).append("\n");
        }
        
        // In a real app, save this to a file or share it
        Log.d("CSV_EXPORT", csv.toString());
        Toast.makeText(getContext(), "CSV exported to logs (Mock)", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
