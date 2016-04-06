package com.example.ousatov.pizzatask.storage;



import android.content.Context;
import android.util.Log;

import com.example.ousatov.pizzatask.venue.FSVenue;

import java.util.ArrayList;

public class Storage {
    private static final String TAG = "US";

    private FSVenueReaderDbHelper mDb;
    private Context mContext;

    public Storage(Context c) {
        mContext = c;
        mDb = new FSVenueReaderDbHelper(mContext);
    }

    public synchronized void clearTable() {
        mDb.deleteOldTable();
    }

    public synchronized void saveData(ArrayList<FSVenue> data) {
        Log.d(TAG, "saveData() size = " + data.size() + " tid = " + Thread.currentThread().getId());
        for (int i = 0; i < data.size(); i++) {
            mDb.createVenue(data.get(i));
        }
    }

    public synchronized ArrayList<FSVenue> loadData(int offset, int number) {
        Log.d(TAG, "loadData() tid = " + Thread.currentThread().getId());

        return mDb.readVenues(offset, number);
    }

    public synchronized ArrayList<FSVenue> loadAllData() {
        Log.d(TAG, "loadAllData() tid = " + Thread.currentThread().getId());
        return mDb.readAllVenues();

    }
}
