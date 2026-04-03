package com.example.eventlottery.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import com.example.eventlottery.R;
import com.example.eventlottery.activities.EntrantMainActivity;
import com.example.eventlottery.repositories.UserRepository;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID   = "event_lottery_channel";
    private static final String CHANNEL_NAME = "EventLottery Notifications";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        // Save updated FCM token to Firestore for this device's user
        SessionManager session = new SessionManager(this);
        String userId = session.getUserId();
        if (userId != null) {
            new UserRepository().updateFcmToken(userId, token);
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String title   = "EventLottery";
        String body    = "";
        String eventId = null;

        // Data payload takes priority
        if (!remoteMessage.getData().isEmpty()) {
            title   = remoteMessage.getData().getOrDefault("title", title);
            body    = remoteMessage.getData().getOrDefault("body", body);
            eventId = remoteMessage.getData().get("eventId");
        }

        // Notification payload fallback
        if (remoteMessage.getNotification() != null) {
            if (remoteMessage.getNotification().getTitle() != null)
                title = remoteMessage.getNotification().getTitle();
            if (remoteMessage.getNotification().getBody() != null)
                body  = remoteMessage.getNotification().getBody();
        }

        showNotification(title, body, eventId);
    }

    private void showNotification(String title, String body, String eventId) {
        createNotificationChannel();

        Intent intent = new Intent(this, EntrantMainActivity.class);
        if (eventId != null) intent.putExtra("open_event_id", eventId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Lottery results, invitations, and event updates");
        channel.enableVibration(true);
        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) manager.createNotificationChannel(channel);
    }
}
