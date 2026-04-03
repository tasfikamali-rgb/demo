package com.example.eventlottery.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventlottery.R;
import com.example.eventlottery.models.User;
import java.util.ArrayList;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {

    public interface OnDeleteListener { void onDelete(User user); }

    private List<User> users = new ArrayList<>();
    private List<User> allUsers = new ArrayList<>();
    private OnDeleteListener deleteListener;

    public void setDeleteListener(OnDeleteListener l) { this.deleteListener = l; }

    public void setUsers(List<User> list) {
        this.allUsers = new ArrayList<>(list);
        this.users    = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        if (query.isEmpty()) {
            users = new ArrayList<>(allUsers);
        } else {
            users = new ArrayList<>();
            String q = query.toLowerCase();
            for (User u : allUsers) {
                if ((u.getName() != null && u.getName().toLowerCase().contains(q)) ||
                    (u.getEmail() != null && u.getEmail().toLowerCase().contains(q))) {
                    users.add(u);
                }
            }
        }
        notifyDataSetChanged();
    }

    public int getTotalCount() { return allUsers.size(); }
    public boolean isEmpty() { return users.isEmpty(); }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        User u = users.get(position);
        h.tvAvatar.setText(u.getInitials());
        h.tvName.setText(u.getName());
        h.tvEmail.setText(u.getEmail());

        // Role badges
        h.llRoleBadges.removeAllViews();
        if (u.getRoles() != null) {
            for (String role : u.getRoles()) {
                TextView badge = new TextView(h.llRoleBadges.getContext());
                badge.setText(capitalize(role));
                badge.setTextSize(10f);
                badge.setTextColor(0xFF374151);
                badge.setPadding(dp(h, 8), dp(h, 3), dp(h, 8), dp(h, 3));
                badge.setBackgroundResource(R.drawable.bg_info_cell);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMarginEnd(dp(h, 4));
                badge.setLayoutParams(lp);
                h.llRoleBadges.addView(badge);
            }
        }

        h.ivDelete.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDelete(u);
        });
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private int dp(ViewHolder h, int val) {
        float d = h.itemView.getContext().getResources().getDisplayMetrics().density;
        return Math.round(val * d);
    }

    @Override public int getItemCount() { return users.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvName, tvEmail;
        LinearLayout llRoleBadges;
        ImageView ivDelete;
        ViewHolder(View v) {
            super(v);
            tvAvatar    = v.findViewById(R.id.tv_avatar);
            tvName      = v.findViewById(R.id.tv_name);
            tvEmail     = v.findViewById(R.id.tv_email);
            llRoleBadges = v.findViewById(R.id.ll_role_badges);
            ivDelete    = v.findViewById(R.id.iv_delete);
        }
    }
}
