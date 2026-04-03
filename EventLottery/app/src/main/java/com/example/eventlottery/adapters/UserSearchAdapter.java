package com.example.eventlottery.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventlottery.R;
import com.example.eventlottery.models.User;
import java.util.ArrayList;
import java.util.List;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.ViewHolder> {

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    private List<User> users = new ArrayList<>();
    private final OnUserClickListener listener;

    public UserSearchAdapter(OnUserClickListener listener) {
        this.listener = listener;
    }

    public void setUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        holder.tvInitials.setText(user.getInitials());
        holder.tvName.setText(user.getName());
        holder.tvEmail.setText(user.getEmail());
        holder.itemView.setOnClickListener(v -> listener.onUserClick(user));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInitials, tvName, tvEmail;
        ViewHolder(View v) {
            super(v);
            tvInitials = v.findViewById(R.id.tv_avatar);
            tvName = v.findViewById(R.id.tv_name);
            tvEmail = v.findViewById(R.id.tv_email);
        }
    }
}
