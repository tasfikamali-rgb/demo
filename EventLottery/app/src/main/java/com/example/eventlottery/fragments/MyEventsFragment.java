package com.example.eventlottery.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventlottery.R;
import com.example.eventlottery.activities.EventDetailActivity;
import com.example.eventlottery.adapters.MyEventAdapter;
import com.example.eventlottery.models.Event;
import com.example.eventlottery.models.Registration;
import com.example.eventlottery.repositories.EventRepository;
import com.example.eventlottery.repositories.RegistrationRepository;
import com.example.eventlottery.utils.SessionManager;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class MyEventsFragment extends Fragment {

    private SessionManager session;
    private RegistrationRepository regRepo;
    private EventRepository eventRepo;
    private MyEventAdapter adapter;
    private View llEmpty;
    private ProgressBar progress;
    private AutoCompleteTextView atvStatusFilter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        session   = new SessionManager(requireContext());
        regRepo   = new RegistrationRepository();
        eventRepo = new EventRepository();

        RecyclerView rv = view.findViewById(R.id.rv_my_events);
        llEmpty  = view.findViewById(R.id.ll_empty);
        progress = view.findViewById(R.id.progress);
        atvStatusFilter = view.findViewById(R.id.atv_my_status_filter);

        adapter = new MyEventAdapter();
        adapter.setListener(reg -> {
            if (getContext() == null || reg.getEventId() == null) return;
            Intent i = new Intent(getContext(), EventDetailActivity.class);
            i.putExtra("event_id", reg.getEventId());
            startActivity(i);
        });
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        setupFilter();
        loadMyEvents();
    }

    private void setupFilter() {
        String[] statuses = {"All Status", "Waiting", "Selected", "Accepted", "Declined", "Cancelled"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(), 
                R.layout.item_dropdown_filter, statuses);
        atvStatusFilter.setAdapter(statusAdapter);
        atvStatusFilter.setOnItemClickListener((parent, view, position, id) -> {
            adapter.filter(statuses[position]);
            updateEmpty();
        });
    }

    private void loadMyEvents() {
        String userId = session.getUserId();
        if (userId == null) return;
        if (!isAdded()) return;

        progress.setVisibility(View.VISIBLE);
        llEmpty.setVisibility(View.GONE);

        regRepo.getRegistrationsForUser(userId).addOnSuccessListener(qs -> {
            if (!isAdded()) return;

            List<Registration> regs = new ArrayList<>();
            for (DocumentSnapshot doc : qs.getDocuments()) {
                try {
                    Registration r = doc.toObject(Registration.class);
                    if (r != null) {
                        r.setId(doc.getId());
                        regs.add(r);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (regs.isEmpty()) {
                progress.setVisibility(View.GONE);
                llEmpty.setVisibility(View.VISIBLE);
                adapter.setItems(new ArrayList<>());
                return;
            }

            List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
            for (Registration r : regs) {
                if (r.getEventId() != null) {
                    tasks.add(eventRepo.getEventById(r.getEventId()));
                }
            }

            if (tasks.isEmpty()) {
                progress.setVisibility(View.GONE);
                llEmpty.setVisibility(View.VISIBLE);
                return;
            }

            Tasks.whenAllComplete(tasks).addOnCompleteListener(allTask -> {
                if (!isAdded()) return;

                List<MyEventAdapter.RegistrationWithTitle> items = new ArrayList<>();
                for (Registration r : regs) {
                    String title = "Unknown Event";
                    long date = 0;

                    for (Task<DocumentSnapshot> t : tasks) {
                        if (t.isSuccessful() && t.getResult() != null) {
                            DocumentSnapshot doc = t.getResult();
                            if (doc.getId().equals(r.getEventId())) {
                                Event e = doc.toObject(Event.class);
                                if (e != null) {
                                    title = e.getTitle() != null ? e.getTitle() : "Event";
                                    date = e.getEventStartDate();
                                }
                                break;
                            }
                        }
                    }
                    items.add(new MyEventAdapter.RegistrationWithTitle(r, title, date));
                }

                items.sort((a, b) -> Long.compare(b.registration.getJoinedAt(), a.registration.getJoinedAt()));

                progress.setVisibility(View.GONE);
                adapter.setItems(items);
                // After loading, ensure current filter is applied
                adapter.filter(atvStatusFilter.getText().toString());
                updateEmpty();
            });

        }).addOnFailureListener(e -> {
            if (!isAdded()) return;
            progress.setVisibility(View.GONE);
            llEmpty.setVisibility(View.VISIBLE);
            Toast.makeText(getContext(), "Registration load failed", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateEmpty() {
        boolean empty = adapter.isEmpty();
        llEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMyEvents();
    }
}
