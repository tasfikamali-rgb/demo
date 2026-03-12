package com.example.lotterappjava;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> implements Filterable {

    private List<User> userList;
    private List<User> userListFull;
    private OnDeleteClickListener onDeleteClickListener;
    private boolean showDeleteButton;

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    public UserAdapter(List<User> userList, boolean showDeleteButton) {
        this.userList = userList;
        this.userListFull = new ArrayList<>(userList);
        this.showDeleteButton = showDeleteButton;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view, onDeleteClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.name.setText(user.getName());
        holder.email.setText(user.getEmail());
        holder.deleteButton.setVisibility(showDeleteButton ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    @Override
    public Filter getFilter() {
        return userFilter;
    }

    private Filter userFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<User> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(userListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (User user : userListFull) {
                    if (user.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(user);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            userList.clear();
            if (results.values != null) {
                userList.addAll((List) results.values);
            }
            notifyDataSetChanged();
        }
    };

    public void updateList(List<User> newList) {
        this.userList = new ArrayList<>(newList);
        this.userListFull = new ArrayList<>(newList); // This syncs the search backup
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView email;
        ImageButton deleteButton;

        public UserViewHolder(@NonNull View itemView, OnDeleteClickListener listener) {
            super(itemView);
            name = itemView.findViewById(R.id.user_name);
            email = itemView.findViewById(R.id.user_email);
            deleteButton = itemView.findViewById(R.id.delete_user_button);

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onDeleteClick(position);
                    }
                }
            });
        }
    }
}
