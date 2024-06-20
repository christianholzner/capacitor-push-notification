package com.capacitorjs.plugins.pushnotifications;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import com.capacitorjs.plugins.pushnotifications.acknowledge.AcknowledgeService;

public class MessagingService extends FirebaseMessagingService {
    private final AcknowledgeService acknowledgeService = new AcknowledgeService();

    public void handleIntent(Intent intent) {
        Log.i("MessagingService", "intent received");
        super.handleIntent(intent);
        this.acknowledgeService.initContent(this);
        this.acknowledgeService.newNotification(intent);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        PushNotificationsPlugin.sendRemoteMessage(remoteMessage);
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        PushNotificationsPlugin.onNewToken(s);
    }
}
