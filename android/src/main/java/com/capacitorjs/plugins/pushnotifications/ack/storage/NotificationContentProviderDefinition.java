package com.capacitorjs.plugins.pushnotifications.ack.storage;

import android.net.Uri;

public final class NotificationContentProviderDefinition {
    public static final String PROVIDER_NAME = "com.capacitorjs.plugins.pushnotifications";
    public static final String URL = "content://" + PROVIDER_NAME + "/keys";
    public static final Uri CONTENT_URI = Uri.parse(URL);
    public static final String COLUMN_KEY = "id";
    public static final String COLUMN_DEVICEID = "deviceId";
    public static final String COLUMN_NOTIFICATIONLOGID = "notificationLogId";
    public static final String COLUMN_INTERVENTIONID = "interventionId";
    public static final String COLUMN_ORIGIN = "origin";

    public static final String[] DEFAULT_PROJECTION = {
            COLUMN_KEY,
            COLUMN_DEVICEID,
            COLUMN_NOTIFICATIONLOGID,
            COLUMN_INTERVENTIONID,
            COLUMN_ORIGIN
    };
}
