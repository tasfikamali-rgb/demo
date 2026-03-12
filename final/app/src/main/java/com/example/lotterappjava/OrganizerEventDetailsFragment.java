package com.example.lotterappjava;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lotterappjava.databinding.FragmentOrganizerEventDetailsBinding;
import com.google.android.material.tabs.TabLayout;

public class OrganizerEventDetailsFragment extends Fragment {

    private FragmentOrganizerEventDetailsBinding binding;
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
        binding = FragmentOrganizerEventDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        checkOwnershipAndLoad();
    }

    private void checkOwnershipAndLoad() {
        eventController.getEvent(eventId, event -> {
            if (binding == null) return;
            if (event != null) {
                boolean isOwner = organizerId != null
                        && event.getOrganizerId() != null
                        && organizerId.equals(event.getOrganizerId());

                binding.textEventTitle.setText(event.getTitle());
                if (!isOwner) {
                    Toast.makeText(getContext(), "Viewing event created by another organizer (view-only)", Toast.LENGTH_SHORT).show();
                }
                setupTabs();
            } else {
                Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(this).navigateUp();
            }
        });
    }

    private void setupTabs() {
        // Initial fragment
        replaceTabFragment(new OrganizerEntrantsFragment());

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        replaceTabFragment(new OrganizerEntrantsFragment());
                        break;
                    case 1:
                        replaceTabFragment(new OrganizerDrawsFragment());
                        break;
                    case 2:
                        replaceTabFragment(new OrganizerMapFragment());
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void replaceTabFragment(Fragment fragment) {
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        fragment.setArguments(args);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.tab_container, fragment)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
