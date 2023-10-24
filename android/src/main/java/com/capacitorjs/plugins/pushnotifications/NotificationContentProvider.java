package com.capacitorjs.plugins.pushnotifications;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class NotificationContentProvider extends ContentProvider {

    private static final UriMatcher uriMatcher;

    private static final int uriCode = 1;


    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(NotificationContentProviderDefinition.PROVIDER_NAME, "keys", uriCode);
        uriMatcher.addURI(NotificationContentProviderDefinition.PROVIDER_NAME, "keys/*", uriCode);
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case uriCode:
                return "vnd.android.cursor.dir/keys";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        Log.i("NotificationContentProvider", "onCreate");
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
        if (db != null) {
            return true;
        }
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Log.i("NotificationContentProvider", "query " + TABLE_NAME);

        if (sortOrder == null || sortOrder == "") {
            sortOrder = NotificationContentProviderDefinition.COLUMN_KEY;
        }

        Cursor c = db.query(TABLE_NAME, projection, selection, selectionArgs, null,
                null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.i("NotificationContentProvider", "insert " + TABLE_NAME);
        long rowID = db.replace(TABLE_NAME, "", values);
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(NotificationContentProviderDefinition.CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new SQLiteException("Failed to add a record into " + uri);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        Log.i("NotificationContentProvider", "update " + TABLE_NAME);
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case uriCode:
                count = db.update(TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.i("NotificationContentProvider", "delete " + TABLE_NAME);
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case uriCode:
                count = db.delete(TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    private SQLiteDatabase db;

    static final String DATABASE_NAME = "ContentProviderDB";
    static final String TABLE_NAME = "keys";
    static final int DATABASE_VERSION = 3;
    static final String CREATE_DB_TABLE = " CREATE TABLE " + TABLE_NAME
            + " (" + NotificationContentProviderDefinition.COLUMN_KEY + " TEXT PRIMARY KEY, "
            + " " + NotificationContentProviderDefinition.COLUMN_DEVICEID + " TEXT,"
            + " " + NotificationContentProviderDefinition.COLUMN_NOTIFICATIONLOGID + " TEXT,"
            + " " + NotificationContentProviderDefinition.COLUMN_INTERVENTIONID + " TEXT,"
            + " " + NotificationContentProviderDefinition.COLUMN_ORIGIN + " TEXT);";

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            Log.i("NotificationContentProvider", "DatabaseHelper constructor");
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.i("NotificationContentProvider", "DatabaseHelper onCreate " + CREATE_DB_TABLE);
            db.execSQL(CREATE_DB_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i("NotificationContentProvider", "DatabaseHelper onUpgrade " + "DROP TABLE IF EXISTS " + TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}
