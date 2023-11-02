package com.capacitorjs.plugins.pushnotifications.ack.storage;

import static java.lang.System.currentTimeMillis;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.capacitorjs.plugins.pushnotifications.ack.NotificationLogItem;

import java.util.ArrayList;
import java.util.List;

public class NotificationAckStorage {
    private ContentResolver contentResolver;

    public NotificationAckStorage(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public void save(String deviceId, String notificationLogId, String interventionId, String origin) {
        try {
            String key = Long.toString(currentTimeMillis());

            contentResolver.insert(NotificationContentProviderDefinition.CONTENT_URI, createContentValues(key, new NotificationLogItem(key, deviceId, notificationLogId, interventionId, origin)));
        } catch (Exception ex) {
            Log.e("NotificationAckStorage", "logNotification : " + ex.getMessage());
        }
    }

    public void remove(String timeStamp) {
        String[] selectionArgs = {timeStamp};
        contentResolver.delete(NotificationContentProviderDefinition.CONTENT_URI, NotificationContentProviderDefinition.COLUMN_KEY + " = ?", selectionArgs);
    }

    public List<NotificationLogItem> getNotificationLogItems(int maxValues) {
        List<NotificationLogItem> values = new ArrayList<>();
        String[] projection = NotificationContentProviderDefinition.DEFAULT_PROJECTION;
        Cursor cursor = contentResolver.query(NotificationContentProviderDefinition.CONTENT_URI, projection, null, null, NotificationContentProviderDefinition.COLUMN_KEY + " DESC");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    NotificationLogItem logItem = new NotificationLogItem(
                            getValueOrDefault(cursor, NotificationContentProviderDefinition.COLUMN_KEY),
                            getValueOrDefault(cursor, NotificationContentProviderDefinition.COLUMN_DEVICEID),
                            getValueOrDefault(cursor, NotificationContentProviderDefinition.COLUMN_NOTIFICATIONLOGID),
                            getValueOrDefault(cursor, NotificationContentProviderDefinition.COLUMN_INTERVENTIONID),
                            getValueOrDefault(cursor, NotificationContentProviderDefinition.COLUMN_ORIGIN));
                    values.add(logItem);
                    if (values.size() >= maxValues) {
                        break;
                    }
                    cursor.moveToNext();
                }
            }
            cursor.close();
        }
        return values;
    }

    private static String getValueOrDefault(Cursor cursor, String columnName)
    {
        int index = cursor.getColumnIndex(columnName);
        return index >= 0 ? cursor.getString(index) : null;
    }

    private static ContentValues createContentValues(String key, NotificationLogItem logItem) {
        ContentValues values = new ContentValues();
        values.put(NotificationContentProviderDefinition.COLUMN_KEY, key);
        values.put(NotificationContentProviderDefinition.COLUMN_DEVICEID, logItem.deviceId);
        values.put(NotificationContentProviderDefinition.COLUMN_NOTIFICATIONLOGID, logItem.notificationLogId);
        values.put(NotificationContentProviderDefinition.COLUMN_INTERVENTIONID, logItem.interventionId);
        values.put(NotificationContentProviderDefinition.COLUMN_ORIGIN, logItem.origin);
        return values;
    }
}
