package com.example.lotterappjava;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter class for displaying a list of Notification objects in a RecyclerView.
 * This class follows the View component of the MVC design pattern, facilitating
 * the display of notification messages and timestamps for both users and administrators.
 *
 * Outstanding issues:
 * - Date formatting could be moved to a utility class for consistency.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Notification> notificationList;

    /**
     * Constructor for NotificationAdapter.
     * @param notificationList The initial list of notifications to display.
     */
    public NotificationAdapter(List<Notification> notificationList) {
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        
        // If it's a personal notification (userId is not null), we can show different info if needed
        // but for now let's follow the requested log-style or consistent style
        
        if (notification.getUserId() == null) {
            // This is a group log for Admin
            holder.eventId.setText("Target Group: " + notification.getTargetGroup().toUpperCase());
            holder.message.setText("Group Message: " + notification.getMessage());
        } else {
            // This is a personal notification for Entrant
            holder.eventId.setText("Event ID: " + notification.getEventId());
            holder.message.setText(notification.getMessage());
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.US);
        holder.timestamp.setText(sdf.format(notification.getTimestamp()));
        
        // Optionally show who sent it if we have organizer names, 
        // but for now, the message contains the content.
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    /**
     * Updates the data set and notifies the adapter of the change.
     * @param newList The new list of notifications.
     */
    public void updateList(List<Notification> newList) {
        this.notificationList = newList;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for notification items.
     */
    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView eventId;
        TextView message;
        TextView timestamp;

        /**
         * Constructor for NotificationViewHolder.
         * @param itemView The view for a single notification item.
         */
        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            eventId = itemView.findViewById(R.id.notification_event_id);
            message = itemView.findViewById(R.id.notification_message);
            timestamp = itemView.findViewById(R.id.notification_timestamp);
        }
    }
}
