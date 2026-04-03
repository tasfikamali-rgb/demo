package com.example.eventlottery.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventlottery.R;
import com.example.eventlottery.models.AppNotification;
import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    public interface OnNotificationActionListener {
        void onAccept(AppNotification notification);
        void onDecline(AppNotification notification);
        void onDelete(AppNotification notification);
    }

    private List<AppNotification> notifications = new ArrayList<>();
    private OnNotificationActionListener listener;

    public void setActionListener(OnNotificationActionListener listener) {
        this.listener = listener;
    }

    public void setNotifications(List<AppNotification> list) {
        this.notifications = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    public boolean isEmpty() {
        return notifications.isEmpty();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        AppNotification n = notifications.get(position);
        h.tvTitle.setText(n.getTitle());
        h.tvMessage.setText(n.getMessage());
        h.tvTime.setText(n.getRelativeTime());
        h.vUnread.setVisibility(n.isRead() ? View.GONE : View.VISIBLE);

        if (n.isActionRequired() || AppNotification.TYPE_CO_ORGANIZER.equals(n.getType())) {
            h.llActions.setVisibility(View.VISIBLE);
            h.btnAccept.setOnClickListener(v -> {
                if (listener != null) listener.onAccept(n);
            });
            h.btnDecline.setOnClickListener(v -> {
                if (listener != null) listener.onDecline(n);
            });
        } else {
            h.llActions.setVisibility(View.GONE);
        }

        h.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onDelete(n);
            return true;
        });
    }

    @Override public int getItemCount() { return notifications.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTime;
        View vUnread, llActions;
        View btnAccept, btnDecline;

        ViewHolder(View v) {
            super(v);
            tvTitle   = v.findViewById(R.id.tv_notif_title);
            tvMessage = v.findViewById(R.id.tv_notif_message);
            tvTime    = v.findViewById(R.id.tv_notif_time);
            vUnread   = v.findViewById(R.id.v_unread_dot);
            llActions = v.findViewById(R.id.ll_actions);
            btnAccept = v.findViewById(R.id.btn_accept);
            btnDecline = v.findViewById(R.id.btn_decline);
        }
    }
}
