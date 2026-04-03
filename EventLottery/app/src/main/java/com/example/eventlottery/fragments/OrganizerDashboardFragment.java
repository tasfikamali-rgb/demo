package com.example.eventlottery.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.eventlottery.R;
import com.example.eventlottery.activities.CreateEventActivity;
import com.example.eventlottery.activities.OrganizerEventManagementActivity;
import com.example.eventlottery.adapters.OrganizerEventAdapter;
import com.example.eventlottery.models.Event;
import com.example.eventlottery.repositories.EventRepository;
import com.example.eventlottery.repositories.RegistrationRepository;
import com.example.eventlottery.utils.SessionManager;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class OrganizerDashboardFragment extends Fragment {

    private SessionManager session;
    private EventRepository eventRepo;
    private RegistrationRepository regRepo;
    private OrganizerEventAdapter adapter;

    private TextView tvStatEvents, tvStatOpen, tvStatEntrants, tvStatAccepted;
    private View llEmpty;
    private ProgressBar progress;
    private SwipeRefreshLayout swipeRefresh;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_organizer_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        session   = new SessionManager(requireContext());
        eventRepo = new EventRepository();
        regRepo   = new RegistrationRepository();

        tvStatEvents   = view.findViewById(R.id.tv_stat_events);
        tvStatOpen     = view.findViewById(R.id.tv_stat_open);
        tvStatEntrants = view.findViewById(R.id.tv_stat_entrants);
        tvStatAccepted = view.findViewById(R.id.tv_stat_accepted);
        llEmpty        = view.findViewById(R.id.ll_empty);
        progress       = view.findViewById(R.id.progress);
        swipeRefresh   = view.findViewById(R.id.swipe_refresh);

        RecyclerView rv = view.findViewById(R.id.rv_events);
        adapter = new OrganizerEventAdapter();
        adapter.setListener(new OrganizerEventAdapter.OnEventClickListener() {
            @Override
            public void onClick(Event event) {
                Intent i = new Intent(getContext(), OrganizerEventManagementActivity.class);
                i.putExtra("event_id", event.getId());
                startActivity(i);
            }

            @Override
            public void onEditClick(Event event) {
                Intent i = new Intent(getContext(), CreateEventActivity.class);
                i.putExtra("event_id", event.getId());
                startActivity(i);
            }
        });
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        view.findViewById(R.id.btn_new_event).setOnClickListener(v ->
                startActivity(new Intent(getContext(), CreateEventActivity.class)));

        swipeRefresh.setOnRefreshListener(this::loadDashboard);
        swipeRefresh.setColorSchemeResources(R.color.primary);

        loadDashboard();
    }

    private void loadDashboard() {
        String userId = session.getUserId();
        if (userId == null) return;
        progress.setVisibility(View.VISIBLE);

        eventRepo.getEventsByOrganizer(userId).addOnSuccessListener(qs -> {
            if (!isAdded()) return;
            List<Event> events = new ArrayList<>();
            int openCount = 0, totalEntrants = 0, totalAccepted = 0;

            for (DocumentSnapshot doc : qs.getDocuments()) {
                Event e = doc.toObject(Event.class);
                if (e == null) continue;
                e.setId(doc.getId());
                events.add(e);
                if (Event.STATUS_OPEN.equals(e.getStatus())) openCount++;
                totalEntrants += e.getWaitingListCount();
                totalAccepted += e.getAcceptedCount();
            }

            // Client-side sort by createdAt descending since we removed it from the repository query
            events.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));

            tvStatEvents.setText(String.valueOf(events.size()));
            tvStatOpen.setText(String.valueOf(openCount));
            tvStatEntrants.setText(String.valueOf(totalEntrants));
            tvStatAccepted.setText(String.valueOf(totalAccepted));

            adapter.setEvents(events);
            progress.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(false);
            llEmpty.setVisibility(adapter.isEmpty() ? View.VISIBLE : View.GONE);
        }).addOnFailureListener(e -> {
            if (!isAdded()) return;
            progress.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(false);
            Toast.makeText(getContext(), "Error loading events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override public void onResume() { super.onResume(); loadDashboard(); }
}
