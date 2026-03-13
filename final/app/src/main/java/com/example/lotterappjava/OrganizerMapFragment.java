package com.example.lotterappjava;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lotterappjava.databinding.FragmentOrganizerMapBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

/**
 * Fragment that displays a Google Map with markers for entrant locations.
 * This class follows the View component of the MVC design pattern, providing
 * organizers with a geographical visualization of event participants.
 *
 * Outstanding issues:
 * - Map bounds calculation might fail if participants are very far apart.
 */
public class OrganizerMapFragment extends Fragment implements OnMapReadyCallback {

    private FragmentOrganizerMapBinding binding;
    private String eventId;
    private EventController eventController;
    private GoogleMap googleMap;

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
        binding = FragmentOrganizerMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        loadEntrantLocations();
    }

    /**
     * Fetches and displays locations for entrants with various statuses (waiting, invited, enrolled).
     */
    private void loadEntrantLocations() {
        if (eventId == null || googleMap == null) return;

        eventController.getEntrantsWithStatus(eventId, "waiting", this::addMarkersForParticipants);
        eventController.getEntrantsWithStatus(eventId, "invited", this::addMarkersForParticipants);
        eventController.getEntrantsWithStatus(eventId, "enrolled", this::addMarkersForParticipants);
    }

    /**
     * Adds markers to the Google Map for a list of event participants.
     * @param participants The list of participants with location data.
     */
    private void addMarkersForParticipants(List<EventController.Participant> participants) {
        if (googleMap == null || participants.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boolean hasAny = false;

        for (EventController.Participant p : participants) {
            Double lat = p.getLatitude();
            Double lon = p.getLongitude();
            if (lat == null || lon == null) continue;
            hasAny = true;
            LatLng pos = new LatLng(lat, lon);
            String title = p.getUser() != null ? p.getUser().getName() : "Entrant";
            String snippet = "Status: " + p.getStatus();
            float color;
            switch (p.getStatus()) {
                case "invited":
                    color = BitmapDescriptorFactory.HUE_ORANGE;
                    break;
                case "enrolled":
                    color = BitmapDescriptorFactory.HUE_GREEN;
                    break;
                case "cancelled":
                    color = BitmapDescriptorFactory.HUE_RED;
                    break;
                default:
                    color = BitmapDescriptorFactory.HUE_AZURE;
                    break;
            }
            googleMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(title)
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(color)));
            boundsBuilder.include(pos);
        }

        if (hasAny) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 120));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
