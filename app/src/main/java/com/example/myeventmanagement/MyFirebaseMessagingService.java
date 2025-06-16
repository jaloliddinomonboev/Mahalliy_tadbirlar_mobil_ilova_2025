package com.example.myeventmanagement;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        saveTokenToDatabase(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String title = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();
        saveNotificationToDatabase(title, body);
    }

    private void saveTokenToDatabase(String token) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        ref.child("fcmToken").setValue(token);
    }

    private void saveNotificationToDatabase(String title, String body) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference notifRef = FirebaseDatabase.getInstance().getReference("Notifications").child(userId);
        String notifId = notifRef.push().getKey();
        notifRef.child(notifId).child("title").setValue(title);
        notifRef.child(notifId).child("body").setValue(body);
        notifRef.child(notifId).child("timestamp").setValue(System.currentTimeMillis());
    }
}