package com.example.eventlottery.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventlottery.R;
import com.example.eventlottery.models.Registration;
import java.util.ArrayList;
import java.util.List;

public class RegistrationAdapter extends RecyclerView.Adapter<RegistrationAdapter.ViewHolder> {

    public interface OnCancelListener {
        void onCancel(Registration registration);
    }

    private List<Registration> registrations = new ArrayList<>();
    private final boolean showCancelButton;
    private OnCancelListener cancelListener;

    public RegistrationAdapter(boolean showCancelButton) {
        this.showCancelButton = showCancelButton;
    }

    public void setCancelListener(OnCancelListener l) { this.cancelListener = l; }

    public void setRegistrations(List<Registration> list) {
        this.registrations = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    public boolean isEmpty() { return registrations.isEmpty(); }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_registration, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Registration r = registrations.get(position);
        String initials = r.getUserName() != null && !r.getUserName().isEmpty()
                ? String.valueOf(r.getUserName().charAt(0)).toUpperCase() : "?";
        h.tvAvatar.setText(initials);
        h.tvName.setText(r.getUserName());
        h.tvEmail.setText(r.getUserEmail());

        // Status badge
        h.tvStatusBadge.setText(r.getStatus().toUpperCase());
        switch (r.getStatus()) {
            case Registration.STATUS_WAITING:
                h.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_accent); break;
            case Registration.STATUS_SELECTED:
                h.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_green); break;
            case Registration.STATUS_ACCEPTED:
                h.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_green); break;
            case Registration.STATUS_DECLINED:
                h.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_red); break;
            case Registration.STATUS_CANCELLED:
                h.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_grey); break;
        }

        if (showCancelButton && cancelListener != null &&
                !Registration.STATUS_CANCELLED.equals(r.getStatus())) {
            h.ivCancel.setVisibility(View.VISIBLE);
            h.ivCancel.setOnClickListener(v -> cancelListener.onCancel(r));
        } else {
            h.ivCancel.setVisibility(View.GONE);
        }
    }

    @Override public int getItemCount() { return registrations.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvName, tvEmail, tvStatusBadge;
        ImageView ivCancel;

        ViewHolder(View v) {
            super(v);
            tvAvatar     = v.findViewById(R.id.tv_avatar);
            tvName       = v.findViewById(R.id.tv_name);
            tvEmail      = v.findViewById(R.id.tv_email);
            tvStatusBadge = v.findViewById(R.id.tv_status_badge);
            ivCancel     = v.findViewById(R.id.iv_cancel);
        }
    }
}
