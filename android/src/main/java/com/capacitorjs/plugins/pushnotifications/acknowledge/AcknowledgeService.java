package com.capacitorjs.plugins.pushnotifications.acknowledge;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import com.capacitorjs.plugins.pushnotifications.NotificationLogItem;
import com.capacitorjs.plugins.pushnotifications.storage.NotificationStorage;
import com.getcapacitor.CapConfig;

import java.util.List;

public class AcknowledgeService {
    private static int MAX_LOGS_IN_STORAGE = 20;
    private static int MAX_ALLOWED_TIME_MS_POST_ACK = 3000;
    private NotificationStorage ackStorage;
    private String apiUrl;
    private Context context;
    private boolean isInitialize = false;

    public void initContent(Context context) {
        try {
            if (!this.isInitialize || this.context != context)
            {
                this.context = context;
                this.isInitialize = true;
                ackStorage = new NotificationStorage(context.getContentResolver());

                CapConfig capConfig = CapConfig.loadDefault(context.getApplicationContext());
                apiUrl = capConfig.getString("plugins.PushNotifications.apiUrl", "http://localhost:5000/api/");
            }
        } catch (Exception ex) {
            Log.e("AcknowledgeService", "ackNotification exception : " + ex.getMessage());
        }
    }

    public void newNotification(Intent intent) {
        if (this.isInitialize) {
            this.saveNotification(intent);
            this.keepMaxLogs();
            this.postNotificationAck();
        }
    }

    private void saveNotification(Intent intent) {
        try {
            DeviceInfoExtractor deviceInfoExtractor = new DeviceInfoExtractor(context);

            Bundle data = NotificationDataExtractor.getNotificationData(intent);
            if (data != null) {
                String notificationLogId = data.getString("notificationLogId");
                String interventionId = data.getString("interventionId");
                Log.i("AcknowledgeService", "receive notification id=" + notificationLogId);

                ackStorage.save(deviceInfoExtractor.getDeviceId(), notificationLogId, interventionId, deviceInfoExtractor.getOrigin());
                Log.i("AcknowledgeService", "save notification id=" + notificationLogId);
            }
        } catch (Exception ex) {
            Log.e("AcknowledgeService", "ackNotification exception : " + ex.getMessage());
        }
    }

    private void keepMaxLogs() {
        List<NotificationLogItem> logItems = ackStorage.getNotificationLogItems(MAX_LOGS_IN_STORAGE * 2);
        for (int i = MAX_LOGS_IN_STORAGE; i < logItems.size(); i++) {
            Log.i("AcknowledgeService", "Notification not sent " + logItems.get(i).notificationLogId);
            ackStorage.remove(logItems.get(i).timeStamp);
        }
    }

    public void postNotificationAck() {
        try {
            AcknowledgePublisher ackPublisher = new AcknowledgePublisher(apiUrl);
            List<NotificationLogItem> logItems = ackStorage.getNotificationLogItems(MAX_LOGS_IN_STORAGE);
            int allowedTimeMs = MAX_ALLOWED_TIME_MS_POST_ACK;
            long startTime = SystemClock.elapsedRealtime();
            for (NotificationLogItem logItem : logItems) {
                var result = ackPublisher.post(logItem, allowedTimeMs);
                if (result) {
                    ackStorage.remove(logItem.timeStamp);
                }
                long endTime = SystemClock.elapsedRealtime();
                long elapsedMillisec = endTime - startTime;
                allowedTimeMs -= elapsedMillisec;
                Log.i("AcknowledgeService", "post notification " + logItem.notificationLogId + " with result " + result + " in " + elapsedMillisec + " ms" + " remaining " + allowedTimeMs + " ms");
                if (allowedTimeMs <= 100) {
                    break;
                }
                startTime = endTime;
            }
        } catch (Exception ex) {
            Log.e("AcknowledgeService", "PostNotificationAck : " + ex.getMessage());
        }
    }
}
