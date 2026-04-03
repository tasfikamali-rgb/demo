package com.example.eventlottery.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventlottery.R;
import com.example.eventlottery.models.Event;
import java.util.ArrayList;
import java.util.List;

public class OrganizerEventAdapter extends RecyclerView.Adapter<OrganizerEventAdapter.ViewHolder> {

    public interface OnEventClickListener { 
        void onClick(Event event); 
        void onEditClick(Event event);
    }

    private List<Event> events = new ArrayList<>();
    private OnEventClickListener listener;

    public void setListener(OnEventClickListener l) { this.listener = l; }

    public void setEvents(List<Event> list) {
        this.events = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    public boolean isEmpty() { return events.isEmpty(); }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_organizer_event, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Event e = events.get(position);
        h.tvTitle.setText(e.getTitle());
        h.tvEntrantCount.setText(e.getWaitingListCount() + " entrants");
        h.tvAcceptedCount.setText(e.getAcceptedCount() + "/" + e.getCapacity() + " accepted");

        String status = e.getStatus();
        String label;
        int badgeRes;
        switch (status) {
            case Event.STATUS_OPEN:
                label = "Registration Open"; badgeRes = R.drawable.bg_badge_accent; break;
            case Event.STATUS_CLOSED:
                label = "Registration Closed"; badgeRes = R.drawable.bg_badge_grey; break;
            case Event.STATUS_DRAWN:
                label = "Lottery Drawn"; badgeRes = R.drawable.bg_badge_green; break;
            case Event.STATUS_COMPLETED:
                label = "Completed"; badgeRes = R.drawable.bg_badge_grey; break;
            default:
                label = capitalize(status); badgeRes = R.drawable.bg_badge_grey;
        }
        h.tvStatus.setText(label);
        h.tvStatus.setBackgroundResource(badgeRes);

        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(e); });
        h.btnEdit.setOnClickListener(v -> { if (listener != null) listener.onEditClick(e); });
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    @Override public int getItemCount() { return events.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvEntrantCount, tvAcceptedCount, tvStatus;
        ImageView btnEdit;
        ViewHolder(View v) {
            super(v);
            tvTitle        = v.findViewById(R.id.tv_title);
            tvEntrantCount = v.findViewById(R.id.tv_entrant_count);
            tvAcceptedCount = v.findViewById(R.id.tv_accepted_count);
            tvStatus       = v.findViewById(R.id.tv_status);
            btnEdit        = v.findViewById(R.id.btn_edit_event);
        }
    }
}
