package com.example.eventlottery.activities;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.eventlottery.R;
import com.example.eventlottery.models.Registration;
import com.example.eventlottery.repositories.RegistrationRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

public class EntrantMapActivity extends AppCompatActivity {

    private String eventId;
    private RegistrationRepository regRepo;
    private MapView map;
    private MyLocationNewOverlay myLocationOverlay;
    private boolean hasZoomedToUser = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Osmdroid configuration
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        setContentView(R.layout.activity_map_view);

        eventId = getIntent().getStringExtra("event_id");
        if (eventId == null) {
            finish();
            return;
        }

        regRepo = new RegistrationRepository();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        // Add "My Location" (GPS) overlay
        this.myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        this.myLocationOverlay.enableMyLocation();
        
        // Directly zoom to organizer's location once it is first determined
        this.myLocationOverlay.runOnFirstFix(() -> {
            runOnUiThread(() -> {
                if (myLocationOverlay != null && myLocationOverlay.getMyLocation() != null) {
                    map.getController().animateTo(myLocationOverlay.getMyLocation());
                    map.getController().setZoom(17.0);
                    hasZoomedToUser = true;
                }
            });
        });
        
        map.getOverlays().add(this.myLocationOverlay);

        // Set up FAB to zoom to my location
        FloatingActionButton fabMyLocation = findViewById(R.id.fab_my_location);
        fabMyLocation.setOnClickListener(v -> {
            GeoPoint myLocation = myLocationOverlay.getMyLocation();
            if (myLocation != null) {
                map.getController().animateTo(myLocation);
                map.getController().setZoom(17.0);
            } else {
                Toast.makeText(this, "Determining your location...", Toast.LENGTH_SHORT).show();
            }
        });

        loadEntrantLocations();
    }

    private void loadEntrantLocations() {
        regRepo.getRegistrationsForEvent(eventId).addOnSuccessListener(qs -> {
            List<GeoPoint> points = new ArrayList<>();

            for (DocumentSnapshot doc : qs.getDocuments()) {
                Registration reg = doc.toObject(Registration.class);
                if (reg != null && reg.isGeoVerified()) {
                    GeoPoint pos = new GeoPoint(reg.getLatitude(), reg.getLongitude());
                    
                    Marker marker = new Marker(map);
                    marker.setPosition(pos);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    marker.setTitle(reg.getUserName());
                    marker.setSubDescription("Status: " + reg.getStatus());
                    map.getOverlays().add(marker);
                    
                    points.add(pos);
                }
            }

            // Only auto-zoom to entrants if we haven't already zoomed to the user
            // or if there are many entrants to show.
            if (!points.isEmpty()) {
                map.post(() -> {
                    if (!hasZoomedToUser) {
                        if (points.size() == 1) {
                            map.getController().setZoom(15.0);
                            map.getController().setCenter(points.get(0));
                        } else {
                            BoundingBox bbox = BoundingBox.fromGeoPoints(points);
                            map.zoomToBoundingBox(bbox, true, 100);
                        }
                    }
                });
            } else if (!hasZoomedToUser) {
                Toast.makeText(this, "No geolocation data available for entrants", Toast.LENGTH_SHORT).show();
            }
            map.invalidate();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error loading locations", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
        if (myLocationOverlay != null) {
            myLocationOverlay.enableMyLocation();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
        if (myLocationOverlay != null) {
            myLocationOverlay.disableMyLocation();
        }
    }
}
