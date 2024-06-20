package com.capacitorjs.plugins.pushnotifications;

import org.json.JSONException;
import org.json.JSONObject;

public class NotificationLogItem {
    public String timeStamp;
    public String deviceId;
    public String notificationLogId;
    public String interventionId;
    public String origin;

    public NotificationLogItem(String timeStamp, String deviceId, String notificationLogId, String interventionId, String origin) {
        this.timeStamp = timeStamp;
        this.deviceId = deviceId;
        this.notificationLogId = notificationLogId;
        this.interventionId = interventionId;
        this.origin = origin;
    }

    public String toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("timeStamp", timeStamp);
        json.put("deviceId", deviceId);
        json.put("notificationLogId", notificationLogId);
        json.put("interventionId", interventionId);
        json.put("origin", origin);
        return json.toString();
    }
}