package com.example.eventlottery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.eventlottery.R;
import com.example.eventlottery.fragments.CalendarFragment;
import com.example.eventlottery.fragments.DiscoverEventsFragment;
import com.example.eventlottery.fragments.MyEventsFragment;
import com.example.eventlottery.fragments.NotificationsFragment;
import com.example.eventlottery.models.User;
import com.example.eventlottery.repositories.NotificationRepository;
import com.example.eventlottery.repositories.UserRepository;
import com.example.eventlottery.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class EntrantMainActivity extends AppCompatActivity {

    private SessionManager session;
    private UserRepository userRepo;
    private TextView tvNotifBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_main);

        session  = new SessionManager(this);
        userRepo = new UserRepository();
        tvNotifBadge = findViewById(R.id.tv_notif_badge);
        View ivBell  = findViewById(R.id.iv_bell);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        setupRoleSwitcher();

        // Default fragment
        loadFragment(new DiscoverEventsFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_events)    return loadFragment(new DiscoverEventsFragment());
            if (id == R.id.nav_my_events) return loadFragment(new MyEventsFragment());
            if (id == R.id.nav_calendar)  return loadFragment(new CalendarFragment());
            if (id == R.id.nav_alerts)    return loadFragment(new NotificationsFragment());
            if (id == R.id.nav_profile)   {
                startActivity(new Intent(this, ProfileActivity.class)); return true;
            }
            return false;
        });

        ivBell.setOnClickListener(v -> {
            bottomNav.setSelectedItemId(R.id.nav_alerts);
        });

        // Handle deep link event open
        String openEventId = getIntent().getStringExtra("open_event_id");
        if (openEventId != null) openEventDetail(openEventId);

        loadUnreadBadge();
    }

    private void setupRoleSwitcher() {
        Spinner spinner = findViewById(R.id.spinner_role);
        String userId = session.getUserId();
        if (userId == null) return;

        userRepo.getUserById(userId).addOnSuccessListener(doc -> {
            User user = doc.toObject(User.class);
            if (user == null) return;
            List<String> roles = user.getRoles();
            List<String> display = new ArrayList<>();
            for (String r : roles) display.add(capitalize(r));

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, display);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            // Set selection to current role
            int idx = roles.indexOf("entrant");
            if (idx >= 0) spinner.setSelection(idx);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
    }

    private void switchRole(String role) {
        Intent intent;
        switch (role) {
            case "admin":
                intent = new Intent(this, AdminMainActivity.class); break;
            case "organizer":
                intent = new Intent(this, OrganizerMainActivity.class); break;
            default:
                return;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private boolean loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
        return true;
    }

    private void openEventDetail(String eventId) {
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra("event_id", eventId);
        startActivity(intent);
    }

    private void loadUnreadBadge() {
        String userId = session.getUserId();
        if (userId == null || tvNotifBadge == null) return;
        new NotificationRepository().getUnreadNotifications(userId)
                .addOnSuccessListener(qs -> {
                    int count = qs.size();
                    if (count > 0) {
                        tvNotifBadge.setVisibility(View.VISIBLE);
                        tvNotifBadge.setText(count > 9 ? "9+" : String.valueOf(count));
                    } else {
                        tvNotifBadge.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUnreadBadge();
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
