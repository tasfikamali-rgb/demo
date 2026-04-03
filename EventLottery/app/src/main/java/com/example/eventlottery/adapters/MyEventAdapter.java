package com.example.eventlottery.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventlottery.R;
import com.example.eventlottery.models.Registration;
import com.example.eventlottery.utils.DateUtils;
import java.util.ArrayList;
import java.util.List;

public class MyEventAdapter extends RecyclerView.Adapter<MyEventAdapter.ViewHolder> {

    public interface OnItemClickListener { void onClick(Registration registration); }

    public static class RegistrationWithTitle {
        public final Registration registration;
        public final String eventTitle;
        public final long eventStartDate;
        public RegistrationWithTitle(Registration r, String title, long date) {
            this.registration   = r;
            this.eventTitle     = title;
            this.eventStartDate = date;
        }
    }

    private List<RegistrationWithTitle> allItems = new ArrayList<>();
    private List<RegistrationWithTitle> filteredItems = new ArrayList<>();
    private OnItemClickListener listener;

    public void setListener(OnItemClickListener l) { this.listener = l; }

    public void setItems(List<RegistrationWithTitle> list) {
        this.allItems = new ArrayList<>(list);
        applyFilter("All Status");
    }

    public void filter(String statusFilter) {
        applyFilter(statusFilter);
    }

    private void applyFilter(String statusFilter) {
        if ("All Status".equals(statusFilter)) {
            filteredItems = new ArrayList<>(allItems);
        } else {
            filteredItems = new ArrayList<>();
            for (RegistrationWithTitle item : allItems) {
                String regStatus = item.registration.getStatus();
                if (matchesFilter(regStatus, statusFilter)) {
                    filteredItems.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    private boolean matchesFilter(String regStatus, String filter) {
        if (regStatus == null) return false;
        switch (filter) {
            case "Invited": return Registration.STATUS_INVITED.equalsIgnoreCase(regStatus);
            case "Waiting": return Registration.STATUS_WAITING.equalsIgnoreCase(regStatus);
            case "Selected": return Registration.STATUS_SELECTED.equalsIgnoreCase(regStatus);
            case "Accepted": return Registration.STATUS_ACCEPTED.equalsIgnoreCase(regStatus);
            case "Declined": return Registration.STATUS_DECLINED.equalsIgnoreCase(regStatus);
            case "Cancelled": return Registration.STATUS_CANCELLED.equalsIgnoreCase(regStatus);
            default: return false;
        }
    }

    public boolean isEmpty() { return filteredItems.isEmpty(); }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_event, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        RegistrationWithTitle item = filteredItems.get(position);
        Registration r = item.registration;

        h.tvEventTitle.setText(item.eventTitle);
        h.tvEventDate.setText(DateUtils.formatDateShort(item.eventStartDate));
        h.tvJoinedDate.setText("Joined " + DateUtils.formatDateShort(r.getJoinedAt()));

        String icon;
        int badgeRes;
        switch (r.getStatus()) {
            case Registration.STATUS_INVITED:
                icon = "📩"; badgeRes = R.drawable.bg_badge_accent; break;
            case Registration.STATUS_WAITING:
                icon = "⏳"; badgeRes = R.drawable.bg_badge_accent; break;
            case Registration.STATUS_SELECTED:
                icon = "🎉"; badgeRes = R.drawable.bg_badge_green; break;
            case Registration.STATUS_ACCEPTED:
                icon = "✅"; badgeRes = R.drawable.bg_badge_green; break;
            case Registration.STATUS_DECLINED:
                icon = "❌"; badgeRes = R.drawable.bg_badge_red; break;
            case Registration.STATUS_CANCELLED:
                icon = "🚫"; badgeRes = R.drawable.bg_badge_grey; break;
            default:
                icon = "?"; badgeRes = R.drawable.bg_badge_grey;
        }
        h.tvStatusIcon.setText(icon);
        h.tvStatusBadge.setText(capitalize(r.getStatus()));
        h.tvStatusBadge.setBackgroundResource(badgeRes);

        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(r); });
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    @Override public int getItemCount() { return filteredItems.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStatusIcon, tvEventTitle, tvEventDate, tvJoinedDate, tvStatusBadge;
        ViewHolder(View v) {
            super(v);
            tvStatusIcon  = v.findViewById(R.id.tv_status_icon);
            tvEventTitle  = v.findViewById(R.id.tv_event_title);
            tvEventDate   = v.findViewById(R.id.tv_event_date);
            tvJoinedDate  = v.findViewById(R.id.tv_joined_date);
            tvStatusBadge = v.findViewById(R.id.tv_status_badge);
        }
    }
}
