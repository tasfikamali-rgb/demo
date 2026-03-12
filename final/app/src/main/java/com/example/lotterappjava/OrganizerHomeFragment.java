package com.example.lotterappjava;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lotterappjava.databinding.FragmentOrganizerHomeBinding;

import java.util.ArrayList;
import java.util.List;

public class OrganizerHomeFragment extends Fragment {

    private FragmentOrganizerHomeBinding binding;
    private EventController eventController;
    private OrganizerEventAdapter eventAdapter;
    private final AuthManager authManager = new AuthManager();
    private List<Event> eventList = new ArrayList<>();
    private String organizerId;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentOrganizerHomeBinding.inflate(inflater, container, false);
        eventController = new EventController();
        organizerId = DeviceIdManager.getDeviceId(requireContext());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        loadEvents();

        binding.btnCreateNewEvent.setOnClickListener(v ->
                NavHostFragment.findNavController(OrganizerHomeFragment.this)
                        .navigate(R.id.action_organizerHomeFragment_to_organizerCreateEventFragment)
        );

        binding.btnFacilityProfile.setOnClickListener(v ->
                NavHostFragment.findNavController(OrganizerHomeFragment.this)
                        .navigate(R.id.action_organizerHomeFragment_to_organizerProfileFragment)
        );

        binding.btnLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        new AlertDialog.Builder(getContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    authManager.signOut();
                    NavOptions options = new NavOptions.Builder()
                            .setPopUpTo(R.id.nav_graph, true)
                            .build();
                    NavHostFragment.findNavController(OrganizerHomeFragment.this)
                            .navigate(R.id.loginFragment, null, options);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupRecyclerView() {
        eventAdapter = new OrganizerEventAdapter(eventList, organizerId, new OrganizerEventAdapter.OnEventClickListener() {
            @Override
            public void onItemClick(Event event) {
                Bundle bundle = new Bundle();
                bundle.putString("eventId", event.getEventId());
                NavHostFragment.findNavController(OrganizerHomeFragment.this)
                        .navigate(R.id.action_organizerHomeFragment_to_organizerEventDetailsFragment, bundle);
            }

            @Override
            public void onEditClick(Event event) {
                Bundle bundle = new Bundle();
                bundle.putString("eventId", event.getEventId());
                NavHostFragment.findNavController(OrganizerHomeFragment.this)
                        .navigate(R.id.action_organizerHomeFragment_to_organizerCreateEventFragment, bundle);
            }

            @Override
            public void onDeleteClick(Event event) {
                showDeleteConfirmation(event);
            }
        });
        binding.recyclerMyEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerMyEvents.setAdapter(eventAdapter);
    }

    private void loadEvents() {
        eventController.getAllEvents(events -> {
            if (binding == null) return;
            eventList.clear();
            eventList.addAll(events);
            eventAdapter.notifyDataSetChanged();
        });
    }

    private void showDeleteConfirmation(Event event) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    eventController.deleteEvent(event.getEventId(), organizerId, success -> {
                        if (success) {
                            Toast.makeText(getContext(), "Event deleted", Toast.LENGTH_SHORT).show();
                            loadEvents();
                        } else {
                            Toast.makeText(getContext(), "Failed to delete event or permission denied", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
