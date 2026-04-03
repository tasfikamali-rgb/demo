package com.example.eventlottery.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventlottery.R;
import com.example.eventlottery.activities.EventDetailActivity;
import com.example.eventlottery.adapters.EventAdapter;
import com.example.eventlottery.models.Event;
import com.example.eventlottery.repositories.EventRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarFragment extends Fragment {

    private final Calendar displayMonth = Calendar.getInstance();
    private Calendar selectedDate = Calendar.getInstance();
    private Map<String, List<Event>> eventsByDate = new HashMap<>();
    private final SimpleDateFormat keyFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat headerFmt = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private final SimpleDateFormat dayHeaderFmt = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());

    private TextView tvMonthYear, tvSelectedDate, tvNoEventsDay;
    private GridLayout gridCalendar;
    private RecyclerView rvDayEvents;
    private EventAdapter dayAdapter;
    private EventRepository eventRepo;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        eventRepo = new EventRepository();

        tvMonthYear   = view.findViewById(R.id.tv_month_year);
        tvSelectedDate = view.findViewById(R.id.tv_selected_date);
        tvNoEventsDay = view.findViewById(R.id.tv_no_events_day);
        gridCalendar  = view.findViewById(R.id.grid_calendar);
        rvDayEvents   = view.findViewById(R.id.rv_day_events);

        dayAdapter = new EventAdapter(event -> {
            Intent i = new Intent(getContext(), EventDetailActivity.class);
            i.putExtra("event_id", event.getId());
            startActivity(i);
        });
        rvDayEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDayEvents.setAdapter(dayAdapter);

        view.findViewById(R.id.iv_prev_month).setOnClickListener(v -> {
            displayMonth.add(Calendar.MONTH, -1);
            buildCalendarGrid();
        });
        view.findViewById(R.id.iv_next_month).setOnClickListener(v -> {
            displayMonth.add(Calendar.MONTH, 1);
            buildCalendarGrid();
        });

        loadEvents();
    }

    private void loadEvents() {
        eventRepo.getAllPublicEvents().addOnSuccessListener(qs -> {
            if (!isAdded()) return;  // fragment may have been detached
            eventsByDate.clear();
            for (DocumentSnapshot doc : qs.getDocuments()) {
                Event e = doc.toObject(Event.class);
                if (e == null) continue;
                e.setId(doc.getId());
                String key = keyFmt.format(new Date(e.getEventStartDate()));
                eventsByDate.computeIfAbsent(key, k -> new ArrayList<>()).add(e);
            }
            buildCalendarGrid();
            showDayEvents(selectedDate);
        });
    }

    private void buildCalendarGrid() {
        tvMonthYear.setText(headerFmt.format(displayMonth.getTime()));
        gridCalendar.removeAllViews();

        Calendar cal = (Calendar) displayMonth.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDow = cal.get(Calendar.DAY_OF_WEEK) - 1; // 0=Sun
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        Calendar today = Calendar.getInstance();

        // Empty cells before first day
        for (int i = 0; i < firstDow; i++) {
            gridCalendar.addView(makeDayCell(null, false, false, false));
        }

        for (int day = 1; day <= daysInMonth; day++) {
            cal.set(Calendar.DAY_OF_MONTH, day);
            boolean isToday = sameDay(cal, today);
            boolean isSelected = sameDay(cal, selectedDate);
            String key = keyFmt.format(cal.getTime());
            boolean hasEvents = eventsByDate.containsKey(key);

            final int d = day;
            final Calendar dayCal = (Calendar) cal.clone();
            View cell = makeDayCell(String.valueOf(day), isToday, isSelected, hasEvents);
            cell.setOnClickListener(v -> {
                selectedDate = dayCal;
                buildCalendarGrid();
                showDayEvents(dayCal);
            });
            gridCalendar.addView(cell);
        }
    }

    private View makeDayCell(String label, boolean isToday, boolean isSelected, boolean hasEvents) {
        LinearLayout cell = new LinearLayout(getContext());
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER);

        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.width = 0;
        lp.height = dp(44);
        cell.setLayoutParams(lp);

        if (label == null) return cell;

        TextView tv = new TextView(getContext());
        tv.setText(label);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(14f);

        if (isToday) {
            tv.setBackgroundResource(R.drawable.bg_calendar_today);
            tv.setTextColor(Color.WHITE);
            tv.setTypeface(null, Typeface.BOLD);
        } else if (isSelected) {
            tv.setBackgroundResource(R.drawable.bg_calendar_selected);
            tv.setTextColor(0xFF1A7A5E);
        } else {
            tv.setTextColor(0xFF374151);
        }

        LinearLayout.LayoutParams tvLp = new LinearLayout.LayoutParams(dp(32), dp(32));
        tvLp.gravity = Gravity.CENTER;
        tv.setLayoutParams(tvLp);
        cell.addView(tv);

        if (hasEvents) {
            View dot = new View(getContext());
            dot.setBackgroundResource(R.drawable.bg_event_dot);
            LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(dp(5), dp(5));
            dotLp.gravity = Gravity.CENTER_HORIZONTAL;
            dotLp.topMargin = dp(2);
            dot.setLayoutParams(dotLp);
            cell.addView(dot);
        }

        return cell;
    }

    private void showDayEvents(Calendar cal) {
        tvSelectedDate.setText("Events on " + dayHeaderFmt.format(cal.getTime()));
        String key = keyFmt.format(cal.getTime());
        List<Event> events = eventsByDate.getOrDefault(key, new ArrayList<>());
        dayAdapter.setEvents(events);
        tvNoEventsDay.setVisibility(events.isEmpty() ? View.VISIBLE : View.GONE);
        rvDayEvents.setVisibility(events.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private boolean sameDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
               a.get(Calendar.MONTH) == b.get(Calendar.MONTH) &&
               a.get(Calendar.DAY_OF_MONTH) == b.get(Calendar.DAY_OF_MONTH);
    }

    private int dp(int val) {
        return Math.round(val * getResources().getDisplayMetrics().density);
    }
}
