package com.example.ousatov.pizzatask.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.ousatov.pizzatask.venue.FSVenue;

import java.util.ArrayList;

public class FSVenueReaderDbHelper extends SQLiteOpenHelper {
    private static final String TAG = "US";

    public FSVenueReaderDbHelper(Context context) {
        super(context, FSContract.VenueEntry.DATABASE_NAME, null, FSContract.VenueEntry.DATABASE_VERSION);
        Log.d(TAG, "FSVenueReaderDbHelper !!");
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";

    private static final String CREATE_TABLE = "CREATE TABLE " + FSContract.VenueEntry.TABLE_NAME + "("
            + FSContract.VenueEntry._ID + " INTEGER PRIMARY KEY, "
            + FSContract.VenueEntry.COLUMN_VENUE_NAME + TEXT_TYPE + COMMA_SEP
            + FSContract.VenueEntry.COLUMN_VENUE_DIST + INTEGER_TYPE + COMMA_SEP
            + FSContract.VenueEntry.COLUMN_VENUE_BODY + TEXT_TYPE + ")";
    private static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + FSContract.VenueEntry.TABLE_NAME;

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "DB onCreate !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! tid = " + Thread.currentThread().getId());
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "DB onUpgrade !!!!!!!!! tid = " + Thread.currentThread().getId());
        // drop books table if already exists
        db.execSQL(DELETE_TABLE);
        db.execSQL(CREATE_TABLE);
    }

    public void deleteOldTable() {
        Log.d(TAG, "deleteOldTable()  tid = " + Thread.currentThread().getId());
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL(DELETE_TABLE);
        db.execSQL(CREATE_TABLE);
    }

    public void createVenue(FSVenue venue) {
        // get reference of the FS database
        SQLiteDatabase db = this.getWritableDatabase();
        // make values to be inserted
        ContentValues values = new ContentValues();
        values.put(FSContract.VenueEntry.COLUMN_VENUE_NAME, venue.getName());
        values.put(FSContract.VenueEntry.COLUMN_VENUE_DIST, venue.getDistance());
        values.put(FSContract.VenueEntry.COLUMN_VENUE_BODY, venue.getBody().serializeFsBody());

        // insert venue
        db.insert(FSContract.VenueEntry.TABLE_NAME, null, values);

        // close database transaction
        db.close();
    }

    public ArrayList<FSVenue> readAllVenues() {
        Log.d(TAG, "DB readAllVenues !!!!!!!!!!!!!!!!!!!! tid = " + Thread.currentThread().getId());
        // get reference of the FS database
        return readFromDb(null);
    }

    public ArrayList<FSVenue> readVenues(int offset, int number) {
        Log.d(TAG, "DB readVenue !!!!!!!!!!!!!!!!!!!! tid = " + Thread.currentThread().getId());
        String limit = offset + COMMA_SEP + number;
        return readFromDb(limit);
    }

    private ArrayList<FSVenue> readFromDb(String limit) {
        // get reference of the FS database
        SQLiteDatabase db = this.getReadableDatabase();
        String sortOrder =
                FSContract.VenueEntry.COLUMN_VENUE_DIST + " ASC";
        // get book query
        Cursor cursor = db.query(FSContract.VenueEntry.TABLE_NAME,
                FSContract.VenueEntry.COLUMNS, null, null, null, null, sortOrder, limit);

        // if results !=null, parse the first one
        if (cursor != null) {
            cursor.moveToFirst();
        } else {
            return null;
        }
        int size = cursor.getCount();
        if (0 == size) {
            cursor.close();
            return null;
        }

        ArrayList<FSVenue> resultList = new ArrayList<>();
        do {
            FSVenue venue = new FSVenue();
            venue.setName(cursor.getString(1));
            venue.setDistance(Integer.parseInt(cursor.getString(2)));
            String jsonVenueBody = cursor.getString(3);
            venue.getBody().deserializeFsBody(jsonVenueBody);
            resultList.add(venue);
        } while (cursor.moveToNext());
        cursor.close();

        return resultList;
    }
}
