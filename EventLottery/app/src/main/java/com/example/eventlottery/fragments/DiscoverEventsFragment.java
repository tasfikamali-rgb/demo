package com.example.eventlottery.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.eventlottery.R;
import com.example.eventlottery.activities.EventDetailActivity;
import com.example.eventlottery.activities.QRScanActivity;
import com.example.eventlottery.adapters.EventAdapter;
import com.example.eventlottery.models.Event;
import com.example.eventlottery.repositories.EventRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class DiscoverEventsFragment extends Fragment {

    private EventRepository eventRepo;
    private EventAdapter adapter;
    private RecyclerView rvEvents;
    private View llEmpty;
    private ProgressBar progress;
    private SwipeRefreshLayout swipeRefresh;
    
    private EditText etSearch;
    private AutoCompleteTextView atvStatus, atvSize;
    private View llFilterOptions;

    private final ActivityResultLauncher<Intent> qrLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    String eventId = result.getData().getStringExtra("event_id");
                    if (eventId != null) openEventDetail(eventId);
                }
            });

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discover_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        eventRepo = new EventRepository();

        rvEvents     = view.findViewById(R.id.rv_events);
        llEmpty      = view.findViewById(R.id.ll_empty);
        progress     = view.findViewById(R.id.progress);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        etSearch     = view.findViewById(R.id.et_search);
        
        View cvFilterToggle = view.findViewById(R.id.cv_filter_toggle);
        llFilterOptions     = view.findViewById(R.id.ll_filter_options);
        atvStatus           = view.findViewById(R.id.atv_status);
        atvSize             = view.findViewById(R.id.atv_size);
        
        FloatingActionButton fabScan = view.findViewById(R.id.fab_scan);

        adapter = new EventAdapter(event -> openEventDetail(event.getId()));
        rvEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        rvEvents.setAdapter(adapter);

        setupFilters();

        swipeRefresh.setOnRefreshListener(this::loadEvents);
        swipeRefresh.setColorSchemeResources(R.color.primary);

        fabScan.setOnClickListener(v -> qrLauncher.launch(
                new Intent(getContext(), QRScanActivity.class)));

        cvFilterToggle.setOnClickListener(v -> {
            boolean isVisible = llFilterOptions.getVisibility() == View.VISIBLE;
            llFilterOptions.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        });

        loadEvents();
    }

    private void setupFilters() {
        // Status options
        String[] statuses = {"All Status", "Open Now", "Closed", "Drawn", "Completed"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(), 
                R.layout.item_dropdown_filter, statuses);
        atvStatus.setAdapter(statusAdapter);
        atvStatus.setOnItemClickListener((parent, view, position, id) -> applyFilters());

        // Size options
        String[] sizes = {"Any Size", "Small (\u226420)", "Medium (21-50)", "Large (50+)"};
        ArrayAdapter<String> sizeAdapter = new ArrayAdapter<>(requireContext(), 
                R.layout.item_dropdown_filter, sizes);
        atvSize.setAdapter(sizeAdapter);
        atvSize.setOnItemClickListener((parent, view, position, id) -> applyFilters());

        // Search text
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void applyFilters() {
        String query = etSearch.getText().toString();
        String status = atvStatus.getText().toString();
        String size = atvSize.getText().toString();
        
        adapter.filter(query, status, size);
        updateEmpty();
    }

    private void loadEvents() {
        if (!isAdded()) return;
        progress.setVisibility(View.VISIBLE);
        eventRepo.getAllPublicEvents().addOnSuccessListener(qs -> {
            if (!isAdded()) return;
            List<Event> events = new ArrayList<>();
            for (DocumentSnapshot doc : qs.getDocuments()) {
                Event e = doc.toObject(Event.class);
                if (e != null) {
                    e.setId(doc.getId());
                    events.add(e);
                }
            }
            events.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));

            adapter.setEvents(events);
            applyFilters();
            progress.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(false);
        }).addOnFailureListener(e -> {
            if (!isAdded()) return;
            progress.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(false);
            Toast.makeText(getContext(), "Error loading events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void openEventDetail(String eventId) {
        Intent intent = new Intent(getContext(), EventDetailActivity.class);
        intent.putExtra("event_id", eventId);
        startActivity(intent);
    }

    private void updateEmpty() {
        boolean empty = adapter.isEmpty();
        llEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvEvents.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override public void onResume() { super.onResume(); loadEvents(); }
}
