package com.example.eventlottery.repositories;

import com.example.eventlottery.models.Comment;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class CommentRepository {
    private static final String COLLECTION = "comments";
    private final CollectionReference commentRef;

    public CommentRepository() {
        commentRef = FirebaseFirestore.getInstance().collection(COLLECTION);
    }

    public Task<Void> addComment(Comment comment) {
        return commentRef.document().set(comment);
    }

    public Task<QuerySnapshot> getCommentsForEvent(String eventId) {
        return commentRef.whereEqualTo("eventId", eventId).get();
    }

    public Task<Void> deleteComment(String commentId) {
        return commentRef.document(commentId).delete();
    }
}
