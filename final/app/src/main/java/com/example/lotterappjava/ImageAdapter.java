package com.example.lotterappjava;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Adapter class for displaying a list of image URLs in a RecyclerView.
 * This class follows the View component of the MVC design pattern,
 * using Glide to load images into the UI.
 *
 * Outstanding issues:
 * - None currently identified.
 */
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private List<String> imageUrlList;
    private OnDeleteClickListener onDeleteClickListener;

    /**
     * Interface for handling click events on the delete button of an image item.
     */
    public interface OnDeleteClickListener {
        /**
         * Called when the delete button for a specific image is clicked.
         * @param position The position of the image URL in the list.
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
     * Constructor for ImageAdapter.
     * @param imageUrlList The initial list of image URLs to display.
     */
    public ImageAdapter(List<String> imageUrlList) {
        this.imageUrlList = imageUrlList;
    }

    /**
     * Updates the data set and notifies the adapter of the change.
     * @param newList The new list of image URLs.
     */
    public void updateList(List<String> newList) {
        this.imageUrlList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view, onDeleteClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrlList.get(position);
        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .centerCrop()
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return imageUrlList != null ? imageUrlList.size() : 0;
    }

    /**
     * ViewHolder class for image items.
     */
    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        ImageButton deleteButton;

        /**
         * Constructor for ImageViewHolder.
         * @param itemView The view for a single image item.
         * @param listener The delete click listener.
         */
        public ImageViewHolder(@NonNull View itemView, OnDeleteClickListener listener) {
            super(itemView);
            image = itemView.findViewById(R.id.event_image);
            deleteButton = itemView.findViewById(R.id.delete_image_button);

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
