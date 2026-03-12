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

/**
 * Adapter class for displaying a list of User objects in a RecyclerView.
 * This class follows the View component of the MVC design pattern, facilitating
 * the display and filtering of user data.
 *
 * Outstanding issues:
 * - None currently identified.
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> implements Filterable {

    private List<User> userList;
    private List<User> userListFull;
    private OnDeleteClickListener onDeleteClickListener;
    private boolean showDeleteButton;

    /**
     * Interface for handling click events on the delete button of a user item.
     */
    public interface OnDeleteClickListener {
        /**
         * Called when the delete button for a specific user is clicked.
         * @param position The position of the user in the list.
         */
        void onDeleteClick(int position);
    }

    /**
     * Sets the listener for delete click events.
     * @param listener The listener to be notified.
     */
    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    /**
     * Constructor for UserAdapter.
     * @param userList The initial list of users to display.
     * @param showDeleteButton Boolean flag to control the visibility of the delete button.
     */
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

    /**
     * Updates the data set and notifies the adapter of the change.
     * @param newList The new list of users.
     */
    public void updateList(List<User> newList) {
        this.userList = new ArrayList<>(newList);
        this.userListFull = new ArrayList<>(newList); // This syncs the search backup
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for user items.
     */
    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView email;
        ImageButton deleteButton;

        /**
         * Constructor for UserViewHolder.
         * @param itemView The view for a single user item.
         * @param listener The delete click listener.
         */
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
