package com.example.eventlottery.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventlottery.R;
import com.example.eventlottery.activities.EventDetailActivity;
import com.example.eventlottery.adapters.AdminEventAdapter;
import com.example.eventlottery.adapters.AdminUserAdapter;
import com.example.eventlottery.models.Event;
import com.example.eventlottery.models.User;
import com.example.eventlottery.repositories.EventRepository;
import com.example.eventlottery.repositories.UserRepository;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboardFragment extends Fragment {

    private UserRepository userRepo;
    private EventRepository eventRepo;

    private AdminUserAdapter userAdapter;
    private AdminEventAdapter eventAdapter;

    private TextView tvStatUsers, tvStatEvents, tvStatImages;
    private RecyclerView rvAdmin;
    private View llEmpty;
    private ProgressBar progress;
    private TabLayout tabLayout;
    private EditText etSearch;

    private int currentTab = 0; // 0=users, 1=events, 2=images, 3=logs

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);

        userRepo = new UserRepository();
        eventRepo = new EventRepository();

        tvStatUsers  = view.findViewById(R.id.tv_stat_users);
        tvStatEvents = view.findViewById(R.id.tv_stat_events);
        tvStatImages = view.findViewById(R.id.tv_stat_images);
        rvAdmin      = view.findViewById(R.id.rv_admin);
        llEmpty      = view.findViewById(R.id.ll_empty);
        progress     = view.findViewById(R.id.progress);
        tabLayout    = view.findViewById(R.id.tab_layout);
        etSearch     = view.findViewById(R.id.et_search);

        setupAdapters();
        setupTabs();
        setupSearch();

        loadStats();
        loadUsers();

        return view;
    }

    private void setupAdapters() {
        userAdapter = new AdminUserAdapter();
        userAdapter.setDeleteListener(this::confirmDeleteUser);
        
        eventAdapter = new AdminEventAdapter();
        eventAdapter.setListener(new AdminEventAdapter.OnEventClickListener() {
            @Override
            public void onClick(Event event) {
                // Clicking an event in the normal Events tab opens details
                if (currentTab == 1) {
                    Intent i = new Intent(getContext(), EventDetailActivity.class);
                    i.putExtra("event_id", event.getId());
                    startActivity(i);
                }
            }

            @Override
            public void onDelete(Event event) {
                if (currentTab == 2) {
                    confirmDeletePoster(event);
                } else {
                    confirmDeleteEvent(event);
                }
            }
        });
        
        rvAdmin.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_users));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_events));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_images));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_logs));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                etSearch.setText("");
                switch (currentTab) {
                    case 0: loadUsers();  break;
                    case 1: loadEvents(); break;
                    case 2: showImagesTab(); break;
                    case 3: showLogsTab(); break;
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab t) {}
            @Override public void onTabReselected(TabLayout.Tab t) {}
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                String q = s.toString().trim();
                if (currentTab == 0) userAdapter.filter(q);
                else if (currentTab == 1 || currentTab == 2) eventAdapter.filter(q);
                updateEmpty();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadStats() {
        userRepo.getAllUsers().addOnSuccessListener(qs -> {
            if (isAdded()) tvStatUsers.setText(String.valueOf(qs.size()));
        });
        eventRepo.getAllEvents().addOnSuccessListener(qs -> {
            if (!isAdded()) return;
            tvStatEvents.setText(String.valueOf(qs.size()));
            long images = 0;
            for (DocumentSnapshot d : qs.getDocuments()) {
                Event e = d.toObject(Event.class);
                if (e != null && e.getPosterUrl() != null && !e.getPosterUrl().isEmpty()) images++;
            }
            tvStatImages.setText(String.valueOf(images));
        });
    }

    private void loadUsers() {
        progress.setVisibility(View.VISIBLE);
        rvAdmin.setAdapter(userAdapter);
        userRepo.getAllUsers().addOnSuccessListener(qs -> {
            if (!isAdded()) return;
            progress.setVisibility(View.GONE);
            List<User> users = new ArrayList<>();
            for (DocumentSnapshot doc : qs.getDocuments()) {
                User u = doc.toObject(User.class);
                if (u != null) { u.setId(doc.getId()); users.add(u); }
            }
            userAdapter.setUsers(users);
            updateEmpty();
        }).addOnFailureListener(e -> {
            if (isAdded()) progress.setVisibility(View.GONE);
        });
    }

    private void loadEvents() {
        progress.setVisibility(View.VISIBLE);
        rvAdmin.setAdapter(eventAdapter);
        eventRepo.getAllEvents().addOnSuccessListener(qs -> {
            if (!isAdded()) return;
            progress.setVisibility(View.GONE);
            List<Event> events = new ArrayList<>();
            for (DocumentSnapshot doc : qs.getDocuments()) {
                Event e = doc.toObject(Event.class);
                if (e != null) { e.setId(doc.getId()); events.add(e); }
            }
            eventAdapter.setEvents(events, false); // false = normal event view
            updateEmpty();
        }).addOnFailureListener(e -> {
            if (isAdded()) progress.setVisibility(View.GONE);
        });
    }

    private void showImagesTab() {
        progress.setVisibility(View.VISIBLE);
        rvAdmin.setAdapter(eventAdapter);
        eventRepo.getAllEvents().addOnSuccessListener(qs -> {
            if (!isAdded()) return;
            progress.setVisibility(View.GONE);
            List<Event> withImages = new ArrayList<>();
            for (DocumentSnapshot doc : qs.getDocuments()) {
                Event e = doc.toObject(Event.class);
                if (e != null && e.getPosterUrl() != null && !e.getPosterUrl().isEmpty()) {
                    e.setId(doc.getId());
                    withImages.add(e);
                }
            }
            eventAdapter.setEvents(withImages, true); // true = show image previews
            updateEmpty();
        });
    }

    private void showLogsTab() {
        progress.setVisibility(View.VISIBLE);
        new com.example.eventlottery.repositories.NotificationRepository()
                .getAllNotificationLogs()
                .addOnSuccessListener(qs -> {
                    if (!isAdded()) return;
                    progress.setVisibility(View.GONE);
                    List<String> logLines = new ArrayList<>();
                    for (DocumentSnapshot doc : qs.getDocuments()) {
                        com.example.eventlottery.models.AppNotification n =
                                doc.toObject(com.example.eventlottery.models.AppNotification.class);
                        if (n == null) continue;
                        logLines.add(String.format("[%s] %s \u2192 %s (%d recipients)",
                                com.example.eventlottery.utils.DateUtils.formatDateTime(n.getCreatedAt()),
                                n.getSenderName() != null ? n.getSenderName() : "System",
                                n.getTitle(),
                                n.getRecipientCount()));
                    }
                    if (logLines.isEmpty()) {
                        llEmpty.setVisibility(View.VISIBLE);
                        rvAdmin.setVisibility(View.GONE);
                    } else {
                        llEmpty.setVisibility(View.GONE);
                        rvAdmin.setVisibility(View.VISIBLE);
                        rvAdmin.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                            @NonNull
                            @Override public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
                                TextView tv = new TextView(getContext());
                                tv.setPadding(dp(16), dp(12), dp(16), dp(12));
                                tv.setTextColor(0xFF374151);
                                tv.setTextSize(13f);
                                return new RecyclerView.ViewHolder(tv) {};
                            }
                            @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int pos) {
                                ((TextView) h.itemView).setText(logLines.get(pos));
                            }
                            @Override public int getItemCount() { return logLines.size(); }
                        });
                    }
                });
    }

    private void confirmDeleteUser(User user) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_profile)
                .setMessage(R.string.delete_user_confirm)
                .setPositiveButton(R.string.btn_delete, (d, w) -> {
                    userRepo.deleteUser(user.getId()).addOnSuccessListener(v -> {
                        if (isAdded()) {
                            Toast.makeText(getContext(), "User deleted", Toast.LENGTH_SHORT).show();
                            loadUsers();
                            loadStats();
                        }
                    });
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void confirmDeleteEvent(Event event) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Event")
                .setMessage(R.string.delete_event_confirm)
                .setPositiveButton(R.string.btn_delete, (d, w) -> {
                    eventRepo.deleteEvent(event.getId()).addOnSuccessListener(v -> {
                        if (isAdded()) {
                            Toast.makeText(getContext(), "Event deleted", Toast.LENGTH_SHORT).show();
                            if (currentTab == 1) loadEvents();
                            else if (currentTab == 2) showImagesTab();
                            loadStats();
                        }
                    });
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void confirmDeletePoster(Event event) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Image")
                .setMessage("Delete this image?")
                .setPositiveButton(R.string.btn_delete, (d, w) -> {
                    event.setPosterUrl(null);
                    eventRepo.updateEvent(event).addOnSuccessListener(v -> {
                        if (isAdded()) {
                            Toast.makeText(getContext(), "Image removed", Toast.LENGTH_SHORT).show();
                            showImagesTab();
                            loadStats();
                        }
                    });
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void updateEmpty() {
        boolean empty = (currentTab == 0 && userAdapter.isEmpty()) ||
                        ((currentTab == 1 || currentTab == 2) && eventAdapter.isEmpty());
        llEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvAdmin.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private int dp(int val) {
        return Math.round(val * getResources().getDisplayMetrics().density);
    }
}
