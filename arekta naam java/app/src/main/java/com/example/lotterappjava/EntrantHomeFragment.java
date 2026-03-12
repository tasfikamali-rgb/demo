package com.example.lotterappjava;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lotterappjava.databinding.FragmentEntrantHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntrantHomeFragment extends Fragment {

    private FragmentEntrantHomeBinding binding;
    private EventAdapter adapter;
    private List<Event> allEvents = new ArrayList<>();
    private Map<String, String> userEventStatuses = new HashMap<>();
    private EventController eventController;
    private final AuthManager authManager = new AuthManager();
    private String currentUserId;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentEntrantHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fbUser != null) {
            currentUserId = fbUser.getUid();
        } else {
            currentUserId = DeviceIdManager.getDeviceId(requireContext());
        }

        eventController = new EventController();
        
        binding.recyclerEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        
        setupFilters();
        setupSearch();

        binding.btnNotifications.setOnClickListener(v -> {
            NavHostFragment.findNavController(EntrantHomeFragment.this)
                    .navigate(R.id.action_entrantHomeFragment_to_notificationFragment);
        });

        binding.btnProfile.setOnClickListener(v -> {
            NavHostFragment.findNavController(EntrantHomeFragment.this)
                    .navigate(R.id.action_entrantHomeFragment_to_entrantProfileFragment);
        });

        binding.btnLogout.setOnClickListener(v -> logout());

        fetchInitialData();
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
                    NavHostFragment.findNavController(EntrantHomeFragment.this)
                            .navigate(R.id.loginFragment, null, options);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupFilters() {
        binding.filterAll.setOnClickListener(v -> {
            updateFilterUI(binding.filterAll);
            displayEvents(allEvents, userEventStatuses);
        });

        binding.filterMyApplications.setOnClickListener(v -> {
            updateFilterUI(binding.filterMyApplications);
            eventController.getEventsWithStatusForUser(currentUserId, (events, statuses) -> {
                displayEvents(events, statuses);
            });
        });

        // Initial state
        updateFilterUI(binding.filterAll);
    }

    private void updateFilterUI(TextView selectedFilter) {
        // Reset all
        resetFilterStyle(binding.filterAll);
        resetFilterStyle(binding.filterMyApplications);

        // Set selected
        selectedFilter.setTextColor(Color.parseColor("#4F46E5"));
    }

    private void resetFilterStyle(TextView textView) {
        textView.setTextColor(Color.parseColor("#6B7280"));
    }

    private void setupSearch() {
        binding.searchViewEvents.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) {
                    adapter.getFilter().filter(newText);
                }
                return false;
            }
        });
    }

    private void fetchInitialData() {
        // First get user's statuses so they can be shown even in "All Events" view
        eventController.getEventsWithStatusForUser(currentUserId, (appliedEvents, statuses) -> {
            userEventStatuses.clear();
            userEventStatuses.putAll(statuses);
            
            // Then fetch all events
            eventController.getAllEvents(events -> {
                allEvents.clear();
                allEvents.addAll(events);
                displayEvents(allEvents, userEventStatuses);
            });
        });
    }

    private void displayEvents(List<Event> events, Map<String, String> statuses) {
        adapter = new EventAdapter(new ArrayList<>(events));
        adapter.setEventStatuses(statuses);
        adapter.setOnItemClickListener(event -> {
            Bundle bundle = new Bundle();
            bundle.putString("eventId", event.getEventId());
            NavHostFragment.findNavController(EntrantHomeFragment.this)
                    .navigate(R.id.action_entrantHomeFragment_to_entrantEventDetailsFragment, bundle);
        });
        binding.recyclerEvents.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}