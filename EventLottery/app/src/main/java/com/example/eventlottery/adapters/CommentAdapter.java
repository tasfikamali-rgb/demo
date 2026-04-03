package com.example.eventlottery.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventlottery.R;
import com.example.eventlottery.models.Comment;
import com.example.eventlottery.utils.DateUtils;
import java.util.ArrayList;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    public interface OnDeleteListener { void onDelete(Comment comment); }

    private List<Comment> comments = new ArrayList<>();
    private String currentUserId;
    private boolean isOrganizer;
    private OnDeleteListener deleteListener;

    public CommentAdapter(String currentUserId, boolean isOrganizer) {
        this.currentUserId = currentUserId;
        this.isOrganizer   = isOrganizer;
    }

    public void setDeleteListener(OnDeleteListener l) { this.deleteListener = l; }

    public void setComments(List<Comment> list) {
        this.comments = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    public boolean isEmpty() { return comments.isEmpty(); }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Comment c = comments.get(position);
        h.tvAvatar.setText(c.getInitials());
        h.tvName.setText(c.getUserName());
        h.tvText.setText(c.getText());
        h.tvTime.setText(DateUtils.formatRelative(c.getCreatedAt()));

        boolean canDelete = isOrganizer || c.getUserId().equals(currentUserId);
        h.ivDelete.setVisibility(canDelete ? View.VISIBLE : View.GONE);
        if (canDelete) {
            h.ivDelete.setOnClickListener(v -> {
                if (deleteListener != null) deleteListener.onDelete(c);
            });
        }
    }

    @Override public int getItemCount() { return comments.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvName, tvText, tvTime;
        ImageView ivDelete;
        ViewHolder(View v) {
            super(v);
            tvAvatar = v.findViewById(R.id.tv_avatar);
            tvName   = v.findViewById(R.id.tv_name);
            tvText   = v.findViewById(R.id.tv_text);
            tvTime   = v.findViewById(R.id.tv_time);
            ivDelete = v.findViewById(R.id.iv_delete);
        }
    }
}
