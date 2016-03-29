package com.example.ousatov.pizzatask.storage;


import android.provider.BaseColumns;

public final class FSContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public FSContract() {}

    /* Inner class that defines the table contents */
    public static abstract class VenueEntry implements BaseColumns {
        public static final String TABLE_NAME = "venues";
        public static final String COLUMN_VENUE_NAME = "name";
        public static final String COLUMN_VENUE_DIST = "distance";
        public static final String COLUMN_VENUE_BODY = "body";
        // database version
        public static final int DATABASE_VERSION = 3;
        // database name
        public static final String DATABASE_NAME = "PizzaTask.db";
        static final String[] COLUMNS = { _ID, COLUMN_VENUE_NAME, COLUMN_VENUE_DIST, COLUMN_VENUE_BODY};
    }
}