package com.example.lotterappjava;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Adapter class for displaying a list of Event objects in a RecyclerView.
 * This class follows the View component of the MVC design pattern, facilitating
 * the display, filtering, and interaction with event data in the UI.
 *
 * Outstanding issues:
 * - Date formatting could be moved to a utility class for consistency.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> implements Filterable {

    private List<Event> eventList;
    private List<Event> eventListFull;
    private Map<String, String> eventStatuses = new HashMap<>();
    private OnDeleteClickListener onDeleteClickListener;
    private OnItemClickListener onItemClickListener;
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private static final int DESCRIPTION_CHAR_LIMIT = 100;
    private boolean showDeleteButton;

    /**
     * Interface for handling click events on the delete button of an event item.
     */
    public interface OnDeleteClickListener {
        /**
         * Called when the delete button for a specific event is clicked.
         * @param position The position of the event in the list.
         */
        void onDeleteClick(int position);
    }

    /**
     * Interface for handling click events on an event item.
     */
    public interface OnItemClickListener {
        /**
         * Called when an event item is clicked.
         * @param event The Event object that was clicked.
         */
        void onItemClick(Event event);
    }

    /**
     * Sets the listener for delete click events.
     * @param listener The listener to be notified.
     */
    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    /**
     * Sets the listener for item click events.
     * @param listener The listener to be notified.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    /**
     * Constructor for EventAdapter with default delete button visibility (hidden).
     * @param eventList The initial list of events to display.
     */
    public EventAdapter(List<Event> eventList) {
        this(eventList, false);
    }

    /**
     * Constructor for EventAdapter.
     * @param eventList The initial list of events to display.
     * @param showDeleteButton Boolean flag to control the visibility of the delete button.
     */
    public EventAdapter(List<Event> eventList, boolean showDeleteButton) {
        this.eventList = eventList;
        this.eventListFull = new ArrayList<>(eventList);
        this.showDeleteButton = showDeleteButton;
    }

    /**
     * Updates the data set and notifies the adapter of the change.
     * @param newList The new list of events.
     */
    public void updateList(List<Event> newList) {
        this.eventList = new ArrayList<>(newList);
        this.eventListFull = new ArrayList<>(newList); // This syncs the search backup
        notifyDataSetChanged();
    }

    /**
     * Sets the status mapping for events to display status labels.
     * @param statuses A map where the key is the event ID and the value is the status string.
     */
    public void setEventStatuses(Map<String, String> statuses) {
        this.eventStatuses = statuses;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view, onDeleteClickListener, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.title.setText(event.getTitle());
        
        // Character limit for description followed by "..."
        String desc = event.getDescription() != null ? event.getDescription() : "";
        if (desc.length() > DESCRIPTION_CHAR_LIMIT) {
            desc = desc.substring(0, DESCRIPTION_CHAR_LIMIT) + "...";
        }
        holder.description.setText(desc);

        // Show detailed start time and end time (e.g. 2025-06-17 18:30 - 20:30)
        if (event.getEventDate() != null) {
            SimpleDateFormat fullSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            StringBuilder timeBuilder = new StringBuilder(fullSdf.format(event.getEventDate()));
            if (event.getEventStartTime() != null) {
                SimpleDateFormat timeOnly = new SimpleDateFormat(" HH:mm", Locale.getDefault());
                timeBuilder.append(timeOnly.format(event.getEventStartTime()));
                if (event.getEventEndTime() != null) {
                    timeBuilder.append(" - ").append(timeOnly.format(event.getEventEndTime()));
                }
            }
            holder.time.setText(timeBuilder.toString());
            holder.time.setVisibility(View.VISIBLE);
        } else {
            holder.time.setVisibility(View.GONE);
        }

        // Show registration deadline
        if (event.getRegistrationEnd() != null) {
            holder.regDeadline.setText("Reg ends: " + dateFormat.format(event.getRegistrationEnd()));
            holder.regDeadline.setVisibility(View.VISIBLE);
            
            if (event.getRegistrationEnd().before(new Date())) {
                holder.regDeadline.setTextColor(Color.parseColor("#991B1B")); 
                holder.regDeadline.setText("Registration Closed");
            } else {
                holder.regDeadline.setTextColor(Color.parseColor("#EF4444"));
            }
        } else {
            holder.regDeadline.setVisibility(View.GONE);
        }

        if (event.getPosterUrl() != null && !event.getPosterUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(event.getPosterUrl())
                    .into(holder.poster);
        } else {
            holder.poster.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.deleteButton.setVisibility(showDeleteButton ? View.VISIBLE : View.GONE);

        String status = eventStatuses.get(event.getEventId());
        if (status != null) {
            holder.status.setText(status.toUpperCase());
            holder.status.setVisibility(View.VISIBLE);
            
            // Set status color
            switch (status.toLowerCase()) {
                case "waiting":
                    holder.status.setBackgroundColor(Color.parseColor("#FEF3C7"));
                    holder.status.setTextColor(Color.parseColor("#92400E"));
                    break;
                case "invited":
                    holder.status.setBackgroundColor(Color.parseColor("#DBEAFE"));
                    holder.status.setTextColor(Color.parseColor("#1E40AF"));
                    break;
                case "enrolled":
                    holder.status.setBackgroundColor(Color.parseColor("#D1FAE5"));
                    holder.status.setTextColor(Color.parseColor("#065F46"));
                    break;
                case "cancelled":
                    holder.status.setBackgroundColor(Color.parseColor("#FEE2E2"));
                    holder.status.setTextColor(Color.parseColor("#991B1B"));
                    break;
                default:
                    holder.status.setBackgroundColor(Color.parseColor("#F3F4F6"));
                    holder.status.setTextColor(Color.parseColor("#374151"));
            }
        } else {
            holder.status.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    @Override
    public Filter getFilter() {
        return eventFilter;
    }

    private Filter eventFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Event> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(eventListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Event event : eventListFull) {
                    if (event.getTitle().toLowerCase().contains(filterPattern)) {
                        filteredList.add(event);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            eventList.clear();
            if (results.values != null) {
                eventList.addAll((List) results.values);
            }
            notifyDataSetChanged();
        }
    };

    /**
     * ViewHolder class for event items.
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title;
        TextView description;
        TextView time;
        TextView regDeadline;
        TextView status;
        ImageButton deleteButton;

        /**
         * Constructor for EventViewHolder.
         * @param itemView The view for a single event item.
         * @param deleteListener The delete click listener.
         * @param itemListener The item click listener.
         */
        public EventViewHolder(@NonNull View itemView, OnDeleteClickListener deleteListener, OnItemClickListener itemListener) {
            super(itemView);
            poster = itemView.findViewById(R.id.event_poster);
            title = itemView.findViewById(R.id.event_title);
            description = itemView.findViewById(R.id.event_description);
            time = itemView.findViewById(R.id.event_time);
            regDeadline = itemView.findViewById(R.id.event_reg_deadline);
            status = itemView.findViewById(R.id.event_status);
            deleteButton = itemView.findViewById(R.id.delete_event_button);

            deleteButton.setOnClickListener(v -> {
                if (deleteListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        deleteListener.onDeleteClick(position);
                    }
                }
            });
        }
    }
}
