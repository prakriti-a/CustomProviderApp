package com.prakriti.customprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;

public class StudentDataProvider extends ContentProvider {

    private static final String TAG = "StudentDataProvider";

    static final String PROVIDER_NAME = "com.prakriti.customprovider.DataProvider";
    static final String URL = "content://" + PROVIDER_NAME + "/students";
    static final Uri CONTENT_URI = Uri.parse(URL);

    static final String _ID = "_id";
    static final String NAME = "name";
    static final String CLASS = "class";

    private static HashMap<String, String> STUDENTS_PROJECTION_MAP;

    static final int STUDENTS = 1;
    static final int STUDENT_ID = 2;

    static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "students", STUDENTS);
        uriMatcher.addURI(PROVIDER_NAME, "students/#", STUDENT_ID);
    }

    // Database specific constant declarations
    private SQLiteDatabase db;
    static final String DATABASE_NAME = "SchoolDB";
    static final String STUDENTS_TABLE = "students";
    static final int DATABASE_VERSION = 1;
    static final String CREATE_DB_TABLE = " CREATE TABLE " + STUDENTS_TABLE + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            " name TEXT NOT NULL, " + " grade TEXT NOT NULL);";





    public StudentDataProvider() { }

    @Override
    public boolean onCreate() { // initialize your content provider on startup.
        Log.i(TAG, "onCreate: ");
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        // Create a write able database which will trigger its creation if it doesn't already exist
        db = dbHelper.getWritableDatabase();
        return db != null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) { // handle requests to delete one or more rows.
        int count = 0;
        switch (uriMatcher.match(uri)){
            case STUDENTS:
                count = db.delete(STUDENTS_TABLE, selection, selectionArgs);
                break;
            case STUDENT_ID:
                String id = uri.getPathSegments().get(1);
                count = db.delete(STUDENTS_TABLE, _ID +  " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) { // handle requests to insert a new row
        // Add a new student record
        long rowID = db.insert(STUDENTS_TABLE, null, values);
        // If record is added successfully
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new SQLException("Failed to add a record into " + uri);
//        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) { // handle query requests from clients.
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(STUDENTS_TABLE);

        switch (uriMatcher.match(uri)) {
            case STUDENTS:
                qb.setProjectionMap(STUDENTS_PROJECTION_MAP);
                break;
            case STUDENT_ID:
                qb.appendWhere( _ID + "=" + uri.getPathSegments().get(1));
                break;
            default:
        }
        if (sortOrder == null || sortOrder == ""){
            // By default sort on student names
            sortOrder = NAME;
        }
        Cursor cursor = qb.query(db, projection, selection, selectionArgs,null, null, sortOrder);
        // register to watch a content URI for changes
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) { // handle requests to update one or more rows.
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case STUDENTS:
                count = db.update(STUDENTS_TABLE, values, selection, selectionArgs);
                break;
            case STUDENT_ID:
                count = db.update(STUDENTS_TABLE, values, _ID + " = " + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? " AND (" +selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }


    @Override
    public String getType(Uri uri) { // handle requests for the MIME type of the data at the given URI.
        switch (uriMatcher.match(uri)){
            case STUDENTS:
                // Get all student records
                return "vnd.android.cursor.dir/vnd.example.students";
            case STUDENT_ID:
                // Get a particular student
                return "vnd.android.cursor.item/vnd.example.students";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    // Helper class that creates and manages the provider's underlying data repository
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_DB_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + STUDENTS_TABLE);
            onCreate(db);
        }
    }
}