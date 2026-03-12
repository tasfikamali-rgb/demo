package com.example.lotterappjava;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OrganizerEventAdapter extends RecyclerView.Adapter<OrganizerEventAdapter.ViewHolder> {

    private List<Event> eventList;
    private OnEventClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public interface OnEventClickListener {
        void onItemClick(Event event);
        void onEditClick(Event event);
        void onDeleteClick(Event event);
    }

    public OrganizerEventAdapter(List<Event> eventList, OnEventClickListener listener) {
        this.eventList = eventList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_organizer_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.title.setText(event.getTitle());
        holder.date.setText(event.getEventDate() != null ? dateFormat.format(event.getEventDate()) : "No date");
        holder.cap.setText("Cap: " + event.getCapacity());
        
        if (event.getRegistrationEnd() != null) {
            holder.deadline.setText("Registration ends " + dateFormat.format(event.getRegistrationEnd()));
        } else {
            holder.deadline.setText("No registration deadline");
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(event));
        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(event));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(event));
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, cap, deadline;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.event_title);
            date = itemView.findViewById(R.id.event_date);
            cap = itemView.findViewById(R.id.event_cap);
            deadline = itemView.findViewById(R.id.registration_deadline);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
