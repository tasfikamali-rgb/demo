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

/**
 * Adapter class for displaying a list of events specifically for an organizer in a RecyclerView.
 * This class follows the View component of the MVC design pattern, providing
 * functionality for viewing, editing, and deleting events owned by the organizer.
 *
 * Outstanding issues:
 * - None currently identified.
 */
public class OrganizerEventAdapter extends RecyclerView.Adapter<OrganizerEventAdapter.ViewHolder> {

    private List<Event> eventList;
    private OnEventClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final String currentOrganizerId;

    /**
     * Interface for handling click events on event items, edit buttons, and delete buttons.
     */
    public interface OnEventClickListener {
        /**
         * Called when an event item is clicked.
         * @param event The Event object that was clicked.
         */
        void onItemClick(Event event);

        /**
         * Called when the edit button for an event is clicked.
         * @param event The Event object to be edited.
         */
        void onEditClick(Event event);

        /**
         * Called when the delete button for an event is clicked.
         * @param event The Event object to be deleted.
         */
        void onDeleteClick(Event event);
    }

    /**
     * Constructor for OrganizerEventAdapter.
     * @param eventList The initial list of events to display.
     * @param currentOrganizerId The ID of the currently logged-in organizer.
     * @param listener The listener for click events.
     */
    public OrganizerEventAdapter(List<Event> eventList, String currentOrganizerId, OnEventClickListener listener) {
        this.eventList = eventList;
        this.listener = listener;
        this.currentOrganizerId = currentOrganizerId;
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

        boolean isOwner = event.getOrganizerId() != null
                && currentOrganizerId != null
                && event.getOrganizerId().equals(currentOrganizerId);

        if (isOwner) {
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnEdit.setOnClickListener(v -> listener.onEditClick(event));
            holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(event));
        } else {
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
            holder.btnEdit.setOnClickListener(null);
            holder.btnDelete.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * ViewHolder class for organizer event items.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, cap, deadline;
        ImageButton btnEdit, btnDelete;

        /**
         * Constructor for ViewHolder.
         * @param itemView The view for a single event item.
         */
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
