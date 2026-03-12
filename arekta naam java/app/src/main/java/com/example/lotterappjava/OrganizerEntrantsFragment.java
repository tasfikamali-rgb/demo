package com.example.lotterappjava;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lotterappjava.databinding.FragmentOrganizerEntrantsBinding;

import java.util.ArrayList;
import java.util.List;

public class OrganizerEntrantsFragment extends Fragment {

    private FragmentOrganizerEntrantsBinding binding;
    private String eventId;
    private EventController eventController;
    private List<User> entrantList = new ArrayList<>();
    // private EntrantAdapter adapter; // Assuming you have an adapter for entrants

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
        binding = FragmentOrganizerEntrantsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.recyclerEntrants.setLayoutManager(new LinearLayoutManager(getContext()));
        // Initialize adapter here...

        binding.btnNotify.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Notify functionality coming soon", Toast.LENGTH_SHORT).show();
        });

        loadEntrants();
    }

    private void loadEntrants() {
        eventController.getEntrantsForEvent(eventId, entrants -> {
            if (binding == null) return;
            entrantList.clear();
            entrantList.addAll(entrants);
            // adapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
