package com.example.eventlottery.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventlottery.R;
import com.example.eventlottery.models.User;
import java.util.ArrayList;
import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {

    public interface OnProfileClickListener { void onClick(User user); }

    private List<User> profiles = new ArrayList<>();
    private final OnProfileClickListener listener;

    public ProfileAdapter(OnProfileClickListener listener) { this.listener = listener; }

    public void setProfiles(List<User> list) {
        this.profiles = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    public boolean isEmpty() { return profiles.isEmpty(); }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_profile, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        User u = profiles.get(position);
        h.tvAvatar.setText(u.getInitials());
        h.tvName.setText(u.getName());

        // Role badges
        h.llRoles.removeAllViews();
        if (u.getRoles() != null) {
            for (String role : u.getRoles()) {
                TextView badge = new TextView(h.llRoles.getContext());
                badge.setText(capitalize(role));
                badge.setTextSize(10f);
                badge.setTextColor(0xFFFFFFFF);
                badge.setPadding(dp(h, 8), dp(h, 3), dp(h, 8), dp(h, 3));
                badge.setBackgroundResource(getBadgeRes(role));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMarginEnd(dp(h, 4));
                badge.setLayoutParams(lp);
                h.llRoles.addView(badge);
            }
        }

        h.itemView.setOnClickListener(v -> listener.onClick(u));
    }

    private int getBadgeRes(String role) {
        switch (role) {
            case "organizer": return R.drawable.bg_badge_accent;
            case "admin":     return R.drawable.bg_badge_red;
            default:          return R.drawable.bg_badge_green;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private int dp(ViewHolder h, int val) {
        float density = h.itemView.getContext().getResources().getDisplayMetrics().density;
        return Math.round(val * density);
    }

    @Override public int getItemCount() { return profiles.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvName;
        LinearLayout llRoles;
        ViewHolder(View v) {
            super(v);
            tvAvatar = v.findViewById(R.id.tv_avatar);
            tvName   = v.findViewById(R.id.tv_name);
            llRoles  = v.findViewById(R.id.ll_roles);
        }
    }
}
