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

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private List<String> imageUrlList;
    private OnDeleteClickListener onDeleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    public ImageAdapter(List<String> imageUrlList) {
        this.imageUrlList = imageUrlList;
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
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return imageUrlList.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        ImageButton deleteButton;

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