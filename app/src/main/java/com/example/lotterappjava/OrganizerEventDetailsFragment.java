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

/**
 * Fragment that displays the details of an event for an organizer.
 * It provides a tabbed interface to manage entrants, draws, and see entrant locations on a map.
 * This class follows the View component of the MVC design pattern.
 *
 * Outstanding issues:
 * - None currently identified.
 */
public class OrganizerEventDetailsFragment extends Fragment {

    private FragmentOrganizerEventDetailsBinding binding;
    private String eventId;
    private EventController eventController;
    private String organizerId;

    /**
     * Retrieves the event ID from arguments and initializes the controller.
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
        binding = FragmentOrganizerEventDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Initializes UI components and sets up click listeners.
     *
     * @param view The View returned by onCreateView.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous state.
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        
        binding.btnEdit.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("eventId", eventId);
            NavHostFragment.findNavController(this).navigate(R.id.action_organizerEventDetailsFragment_to_organizerCreateEventFragment, args);
        });

        checkOwnershipAndLoad();
    }

    /**
     * Verifies if the current user is the owner of the event and loads the UI accordingly.
     */
    private void checkOwnershipAndLoad() {
        eventController.getEvent(eventId, event -> {
            if (binding == null) return;
            if (event != null) {
                boolean isOwner = organizerId != null
                        && event.getOrganizerId() != null
                        && organizerId.equals(event.getOrganizerId());

                binding.textEventTitle.setText(event.getTitle());
                if (!isOwner) {
                    Toast.makeText(getContext(), getString(R.string.viewing_view_only), Toast.LENGTH_SHORT).show();
                    binding.btnEdit.setVisibility(View.GONE);
                } else {
                    binding.btnEdit.setVisibility(View.VISIBLE);
                }
                setupTabs();
            } else {
                Toast.makeText(getContext(), getString(R.string.event_not_found), Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(this).navigateUp();
            }
        });
    }

    /**
     * Configures the TabLayout and sets up the listener for switching between sub-fragments.
     */
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

    /**
     * Replaces the fragment in the tab container with a new one.
     *
     * @param fragment The fragment to display in the tab container.
     */
    private void replaceTabFragment(Fragment fragment) {
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        fragment.setArguments(args);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.tab_container, fragment)
                .commit();
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
