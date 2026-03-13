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

/**
 * Fragment responsible for the lottery draw functionality.
 * Allows organizers to sample a specified number of attendees from the waiting list
 * or draw a replacement for a cancelled or declined invitation.
 * This class follows the View component of the MVC design pattern.
 *
 * Outstanding issues:
 * - None currently identified.
 */
public class OrganizerDrawsFragment extends Fragment {

    private FragmentOrganizerDrawsBinding binding;
    private String eventId;
    private EventController eventController;
    private String organizerId;

    /**
     * Retrieves the event ID from arguments and initializes the controller and organizer ID.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }
        eventController = new EventController();
        organizerId = DeviceIdManager.getDeviceId(requireContext());
    }

    /**
     * Inflates the layout for this fragment.
     *
     * @param inflater The LayoutInflater object that can be used to inflate views.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous state.
     * @return The View for the fragment's UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrganizerDrawsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Sets up click listeners for the lottery draw actions.
     *
     * @param view The View returned by onCreateView.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous state.
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnRunDraw.setOnClickListener(v -> {
            String numWinnersStr = binding.editNumWinners.getText().toString();
            if (numWinnersStr.isEmpty()) {
                Toast.makeText(getContext(), getString(R.string.please_fill_in_all_fields), Toast.LENGTH_SHORT).show();
                return;
            }
            int numWinners = Integer.parseInt(numWinnersStr);
            runRandomDraw(numWinners);
        });

        binding.btnDrawReplacement.setOnClickListener(v -> {
            drawReplacement();
        });
    }

    /**
     * Initiates a random lottery draw for a specified number of attendees.
     * Corresponds to US 02.05.02.
     *
     * @param numWinners The number of winners to draw from the waitlist.
     */
    private void runRandomDraw(int numWinners) {
        eventController.drawLottery(eventId, organizerId, numWinners, success -> {
            if (success) {
                Toast.makeText(getContext(), "Lottery draw completed successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to run lottery draw: Unauthorized or Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Initiates a draw for a single replacement applicant.
     * Corresponds to US 02.05.03.
     */
    private void drawReplacement() {
        eventController.drawReplacement(eventId, organizerId, success -> {
            if (success) {
                Toast.makeText(getContext(), "Replacement draw completed successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to draw replacement: Unauthorized or Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Cleans up the binding when the view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
