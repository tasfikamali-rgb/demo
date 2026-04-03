package com.example.eventlottery.adapters;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.eventlottery.R;
import com.example.eventlottery.models.Event;
import com.example.eventlottery.utils.DateUtils;
import java.util.ArrayList;
import java.util.List;

public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.ViewHolder> {

    public interface OnEventClickListener {
        void onClick(Event event);
        void onDelete(Event event);
    }

    private List<Event> events = new ArrayList<>();
    private List<Event> allEvents = new ArrayList<>();
    private OnEventClickListener listener;
    private boolean showImages = false;

    public void setListener(OnEventClickListener l) { this.listener = l; }

    public void setEvents(List<Event> list, boolean showImages) {
        this.showImages = showImages;
        this.allEvents = new ArrayList<>(list);
        this.events    = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        if (query.isEmpty()) {
            events = new ArrayList<>(allEvents);
        } else {
            events = new ArrayList<>();
            String q = query.toLowerCase();
            for (Event e : allEvents) {
                if ((e.getTitle() != null && e.getTitle().toLowerCase().contains(q)) ||
                    (e.getOrganizerName() != null && e.getOrganizerName().toLowerCase().contains(q))) {
                    events.add(e);
                }
            }
        }
        notifyDataSetChanged();
    }

    public int getTotalCount() { return allEvents.size(); }
    public boolean isEmpty() { return events.isEmpty(); }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_event, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Event e = events.get(position);
        h.tvTitle.setText(e.getTitle());
        h.tvOrganizer.setText("by " + e.getOrganizerName());
        h.tvDate.setText(DateUtils.formatDateShort(e.getEventStartDate()));

        String status = e.getStatus();
        h.tvStatus.setText(status.toUpperCase());
        switch (status) {
            case Event.STATUS_OPEN:
                h.tvStatus.setBackgroundResource(R.drawable.bg_badge_green); break;
            case Event.STATUS_DRAWN:
                h.tvStatus.setBackgroundResource(R.drawable.bg_badge_accent); break;
            default:
                h.tvStatus.setBackgroundResource(R.drawable.bg_badge_grey);
        }

        if (showImages) {
            h.tvDeleteLabel.setVisibility(View.VISIBLE);
            if (e.getPosterUrl() != null && !e.getPosterUrl().isEmpty()) {
                h.ivPosterPreview.setVisibility(View.VISIBLE);
                loadPoster(h.ivPosterPreview, e.getPosterUrl());
                
                // Enlarge image on click
                h.ivPosterPreview.setOnClickListener(v -> showEnlargedImage(v.getContext(), e.getPosterUrl()));
            } else {
                h.ivPosterPreview.setVisibility(View.GONE);
            }
            h.itemView.setOnClickListener(null);
            
            // Allow deleting by clicking the text label as well
            h.tvDeleteLabel.setOnClickListener(v -> { if (listener != null) listener.onDelete(e); });
        } else {
            h.tvDeleteLabel.setVisibility(View.GONE);
            h.ivPosterPreview.setVisibility(View.GONE);
            h.itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(e); });
            h.tvDeleteLabel.setOnClickListener(null);
        }

        h.ivDelete.setOnClickListener(v -> { if (listener != null) listener.onDelete(e); });
    }

    private void loadPoster(ImageView imageView, String posterStr) {
        if (posterStr.startsWith("http")) {
            Glide.with(imageView.getContext()).load(posterStr).into(imageView);
        } else {
            try {
                byte[] decodedString = Base64.decode(posterStr, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                imageView.setImageBitmap(decodedByte);
            } catch (Exception ex) {
                imageView.setVisibility(View.GONE);
            }
        }
    }

    private void showEnlargedImage(android.content.Context context, String posterStr) {
        Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_full_image);
        
        ImageView ivLarge = dialog.findViewById(R.id.iv_full_image);
        loadPoster(ivLarge, posterStr);
        
        dialog.findViewById(R.id.btn_close_image).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override public int getItemCount() { return events.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvOrganizer, tvStatus, tvDate, tvDeleteLabel;
        ImageView ivDelete, ivPosterPreview;
        ViewHolder(View v) {
            super(v);
            tvTitle    = v.findViewById(R.id.tv_title);
            tvOrganizer = v.findViewById(R.id.tv_organizer);
            tvStatus   = v.findViewById(R.id.tv_status);
            tvDate     = v.findViewById(R.id.tv_date);
            ivDelete   = v.findViewById(R.id.iv_delete);
            ivPosterPreview = v.findViewById(R.id.iv_poster_preview);
            tvDeleteLabel = v.findViewById(R.id.tv_delete_label);
        }
    }
}
