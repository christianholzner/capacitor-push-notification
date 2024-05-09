package com.capacitorjs.plugins.pushnotifications.ack;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import com.capacitorjs.plugins.pushnotifications.ack.storage.NotificationAckStorage;
import com.getcapacitor.CapConfig;

import java.util.List;

public class NotificationAckService {

    private static int MAX_LOGS_IN_STORAGE = 10;
    private static int MAX_ALLOWED_TIME_MS_POST_ACK = 3000;
    private NotificationAckStorage ackStorage;
    private String apiUrl;
    private Context context;
    private boolean isInit;

    public void initContext(Context context) {
        try {
            if (!isInit || this.context != context)
            {
                this.context = context;
                ackStorage = new NotificationAckStorage(context.getContentResolver());

                CapConfig capConfig = CapConfig.loadDefault(context.getApplicationContext());
                apiUrl = capConfig.getString("plugins.PushNotifications.apiUrl", "http://localhost:5000/api/");
                isInit = true;
            }
        } catch (Exception ex) {
            Log.e("NotificationAckService", "ackNotification exception : " + ex.getMessage());
        }
    }

    public void saveNotification(Intent intent) {
        try {
            if (!isInit) {
                return;
            }
            NotificationExtractor NotificationExtractor = new NotificationExtractor(intent);
            DeviceInfoExtractor deviceInfoExtractor = new DeviceInfoExtractor(context);

            Bundle data = NotificationExtractor.getNotificationData();
            if (data != null) {
                String notificationLogId = data.getString("notificationLogId");
                String interventionId = data.getString("interventionId");
                Log.i("NotificationAckService", "receive notification id=" + notificationLogId);

                ackStorage.save(deviceInfoExtractor.getDeviceId(), notificationLogId, interventionId, deviceInfoExtractor.getOrigin());
                Log.i("NotificationAckService", "save notification id=" + notificationLogId);
                KeepMaxLogs();
            }
        } catch (Exception ex) {
            Log.e("NotificationAckService", "ackNotification exception : " + ex.getMessage());
        }
    }

    private void KeepMaxLogs() {
        List<NotificationLogItem> logItems = ackStorage.getNotificationLogItems(MAX_LOGS_IN_STORAGE * 2);
        for (int i = MAX_LOGS_IN_STORAGE; i < logItems.size(); i++) {
            Log.i("NotificationAckService", "Notification not sent " + logItems.get(i).notificationLogId);
            ackStorage.remove(logItems.get(i).timeStamp);
        }
    }

    public void PostNotificationAck() {
        try {
            if (!isInit) {
                return;
            }
            NotificationAckPublisher ackPublisher = new NotificationAckPublisher(apiUrl);
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
                Log.i("NotificationAckService", "post notification " + logItem.notificationLogId + " with result " + result + " in " + elapsedMillisec + " ms" + " remaining " + allowedTimeMs + " ms");
                if (allowedTimeMs <= 100) {
                    break;
                }
                startTime = endTime;
            }
        } catch (Exception ex) {
            Log.e("NotificationAckService", "PostNotificationAck : " + ex.getMessage());
        }
    }
}
