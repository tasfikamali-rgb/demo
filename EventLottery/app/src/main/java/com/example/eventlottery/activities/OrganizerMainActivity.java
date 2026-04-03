package com.example.eventlottery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.eventlottery.R;
import com.example.eventlottery.fragments.DiscoverEventsFragment;
import com.example.eventlottery.fragments.NotificationsFragment;
import com.example.eventlottery.fragments.OrganizerDashboardFragment;
import com.example.eventlottery.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;

public class OrganizerMainActivity extends AppCompatActivity {

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_main);

        session = new SessionManager(this);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        Spinner spinnerRole = findViewById(R.id.spinner_role);

        // Load available roles for this user
        String organizerUserId = session.getUserId();
        if (organizerUserId == null) {
            // Should never happen — LoginActivity guards this — but be safe
            finish();
            return;
        }
        new com.example.eventlottery.repositories.UserRepository()
                .getUserById(organizerUserId)
                .addOnSuccessListener(doc -> {
                    com.example.eventlottery.models.User user =
                            doc.toObject(com.example.eventlottery.models.User.class);
                    if (user == null) return;
                    List<String> roles = user.getRoles();
                    List<String> displayRoles = new ArrayList<>();
                    for (String r : roles) displayRoles.add(capitalize(r));

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, displayRoles);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerRole.setAdapter(adapter);

                    // Select current role
                    String current = session.getActiveRole();
                    int idx = roles.indexOf(current);
                    if (idx >= 0) spinnerRole.setSelection(idx);

                    spinnerRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                            String chosen = roles.get(pos);
                            if (!chosen.equals(session.getActiveRole())) {
                                session.saveActiveRole(chosen);
                                switchRole(chosen);
                            }
                        }
                        @Override public void onNothingSelected(AdapterView<?> p) {}
                    });
                });

        loadFragment(new OrganizerDashboardFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) return loadFragment(new OrganizerDashboardFragment());
            if (id == R.id.nav_browse)    return loadFragment(new DiscoverEventsFragment());
            if (id == R.id.nav_alerts)    return loadFragment(new NotificationsFragment());
            if (id == R.id.nav_profile)   { startActivity(new Intent(this, ProfileActivity.class)); return true; }
            return false;
        });
    }

    private boolean loadFragment(Fragment f) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, f).commit();
        return true;
    }

    private void switchRole(String role) {
        Intent intent;
        switch (role) {
            case "admin":
                intent = new Intent(this, AdminMainActivity.class); break;
            case "entrant":
                intent = new Intent(this, EntrantMainActivity.class); break;
            default:
                return;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
