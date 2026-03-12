package com.example.lotterappjava;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lotterappjava.databinding.FragmentOrganizerDrawsBinding;

public class OrganizerDrawsFragment extends Fragment {

    private FragmentOrganizerDrawsBinding binding;
    private String eventId;
    private EventController eventController;
    private String organizerId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }
        eventController = new EventController();
        organizerId = DeviceIdManager.getDeviceId(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrganizerDrawsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnRunDraw.setOnClickListener(v -> {
            String numWinnersStr = binding.editNumWinners.getText().toString();
            if (numWinnersStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter number of winners", Toast.LENGTH_SHORT).show();
                return;
            }
            int numWinners = Integer.parseInt(numWinnersStr);
            runRandomDraw(numWinners);
        });

        binding.btnDrawReplacement.setOnClickListener(v -> {
            drawReplacement();
        });
    }

    private void runRandomDraw(int numWinners) {
        // US 02.05.02: As an organizer, I want to set the system to sample a specified number of attendees
        eventController.drawLottery(eventId, organizerId, numWinners, success -> {
            if (success) {
                Toast.makeText(getContext(), "Lottery draw completed successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to run lottery draw: Unauthorized or Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void drawReplacement() {
        // US 02.05.03: As an organizer I want to be able to draw a replacement applicant
        eventController.drawReplacement(eventId, organizerId, success -> {
            if (success) {
                Toast.makeText(getContext(), "Replacement draw completed successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to draw replacement: Unauthorized or Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
