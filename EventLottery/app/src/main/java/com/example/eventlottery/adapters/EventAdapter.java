package com.example.eventlottery.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.eventlottery.R;
import com.example.eventlottery.models.Event;
import com.example.eventlottery.utils.DateUtils;
import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    private List<Event> events   = new ArrayList<>();
    private List<Event> allEvents = new ArrayList<>();
    private final OnEventClickListener listener;

    private String currentQuery = "";
    private String currentStatusFilter = "All Status";
    private String currentSizeFilter = "Any Size";

    public EventAdapter(OnEventClickListener listener) {
        this.listener = listener;
    }

    public void setEvents(List<Event> events) {
        this.allEvents = new ArrayList<>(events);
        applyFilters();
    }

    public void filter(String query, String statusFilter, String sizeFilter) {
        this.currentQuery = query;
        this.currentStatusFilter = statusFilter;
        this.currentSizeFilter = sizeFilter;
        applyFilters();
    }

    private void applyFilters() {
        String lowerQuery = currentQuery == null ? "" : currentQuery.trim().toLowerCase();
        events = new ArrayList<>();

        for (Event e : allEvents) {
            String title    = e.getTitle()    != null ? e.getTitle().toLowerCase()    : "";
            String location = e.getLocation() != null ? e.getLocation().toLowerCase() : "";
            String status   = e.getStatus()   != null ? e.getStatus()                : Event.STATUS_OPEN;

            boolean matchesQuery = lowerQuery.isEmpty()
                    || title.contains(lowerQuery)
                    || location.contains(lowerQuery);

            boolean matchesStatus = "All Status".equals(currentStatusFilter)
                    || (currentStatusFilter.equalsIgnoreCase("Open Now") && status.equalsIgnoreCase(Event.STATUS_OPEN))
                    || (currentStatusFilter.equalsIgnoreCase("Closed") && status.equalsIgnoreCase(Event.STATUS_CLOSED))
                    || (currentStatusFilter.equalsIgnoreCase("Drawn") && status.equalsIgnoreCase(Event.STATUS_DRAWN))
                    || (currentStatusFilter.equalsIgnoreCase("Completed") && status.equalsIgnoreCase("completed"));

            boolean matchesSize = "Any Size".equals(currentSizeFilter);
            if (!matchesSize) {
                int cap = e.getCapacity();
                if (currentSizeFilter.contains("Small") && cap <= 20) matchesSize = true;
                else if (currentSizeFilter.contains("Medium") && cap > 20 && cap <= 50) matchesSize = true;
                else if (currentSizeFilter.contains("Large") && cap > 50) matchesSize = true;
            }

            if (matchesQuery && matchesStatus && matchesSize) {
                events.add(e);
            }
        }
        notifyDataSetChanged();
    }

    public boolean isEmpty() { return events.isEmpty(); }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Event e = events.get(position);

        h.tvTitle.setText(e.getTitle() != null ? e.getTitle() : "");
        h.tvLocation.setText(e.getLocation() != null ? e.getLocation() : "");
        
        // Combine date and time for the card
        String dateTime = DateUtils.formatDateShort(e.getEventStartDate()) + " • " + DateUtils.formatTime(e.getEventStartDate());
        h.tvDate.setText(dateTime);
        
        h.tvCapacity.setText(e.getWaitingListCount() + "/" + e.getCapacity() + " spots");
        h.tvPrice.setText(e.getFormattedPrice());

        // Handle Image Loading
        if (e.getPosterUrl() != null && !e.getPosterUrl().isEmpty()) {
            String posterStr = e.getPosterUrl();
            if (posterStr.startsWith("http")) {
                Glide.with(h.itemView.getContext()).load(posterStr).into(h.ivPoster);
            } else {
                try {
                    byte[] decodedString = Base64.decode(posterStr, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    h.ivPoster.setImageBitmap(decodedByte);
                } catch (Exception ex) {
                    h.ivPoster.setImageResource(R.color.primary_light);
                }
            }
        } else {
            h.ivPoster.setImageResource(R.color.primary_light);
        }

        String status = e.getStatus() != null ? e.getStatus() : Event.STATUS_OPEN;
        h.tvStatus.setText(status.toUpperCase());
        switch (status) {
            case Event.STATUS_OPEN:
                h.tvStatus.setBackgroundResource(R.drawable.bg_badge_green);  break;
            case Event.STATUS_CLOSED:
                h.tvStatus.setBackgroundResource(R.drawable.bg_badge_grey);   break;
            case Event.STATUS_DRAWN:
                h.tvStatus.setBackgroundResource(R.drawable.bg_badge_accent); break;
            default:
                h.tvStatus.setBackgroundResource(R.drawable.bg_badge_grey);
        }

        if (e.isRegistrationOpen()) {
            long days = e.getDaysLeftToRegister();
            h.llDaysLeft.setVisibility(View.VISIBLE);
            h.tvDaysLeft.setText(days + " days left");
        } else {
            h.llDaysLeft.setVisibility(View.GONE);
        }

        h.itemView.setOnClickListener(v -> listener.onEventClick(e));
    }

    @Override
    public int getItemCount() { return events.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvLocation, tvDate,
                 tvCapacity, tvPrice, tvStatus, tvDaysLeft;
        ImageView ivPoster;
        View llDaysLeft;

        ViewHolder(View v) {
            super(v);
            tvTitle       = v.findViewById(R.id.tv_title);
            tvLocation    = v.findViewById(R.id.tv_location);
            tvDate        = v.findViewById(R.id.tv_date);
            tvCapacity    = v.findViewById(R.id.tv_capacity);
            tvPrice       = v.findViewById(R.id.tv_price);
            tvStatus      = v.findViewById(R.id.tv_status);
            tvDaysLeft    = v.findViewById(R.id.tv_days_left);
            llDaysLeft    = v.findViewById(R.id.ll_days_left);
            ivPoster      = v.findViewById(R.id.iv_poster);
        }
    }
}
