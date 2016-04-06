package com.example.ousatov.pizzatask;


import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.ousatov.pizzatask.events.EventErrorGetLocation;
import com.example.ousatov.pizzatask.events.EventErrorUpdateStorage;
import com.example.ousatov.pizzatask.events.EventNotNeededUpdateStorage;
import com.example.ousatov.pizzatask.events.EventUpdateStorage;
import com.example.ousatov.pizzatask.venue.FSVenue;
import com.example.ousatov.pizzatask.networking.FSService;
import com.example.ousatov.pizzatask.storage.Storage;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import de.greenrobot.event.EventBus;

import java.util.ArrayList;
import java.util.Calendar;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class DataManager {
    private static final String TAG = "US";

    private static final String KEY_TO_LATITUDE = "KEY_TO_LATITUDE_US";
    private static final String KEY_TO_LONGITUDE = "KEY_TO_LONGITUDE_US";

    final private String CLIENT_ID = "F3X4K30EJF0ES3LACQ2FRT4QWMJGGRNFRGWCCEWXR2O4HTRG";
    final private String CLIENT_SECRET = "UBVEJPUF0532QUMMBBVQTPNDA5MMUCKJFFHGHQPISUNBLABV";
    private final int SORT_BY_DISTANCE = 1;
    private final String QUERY = "pizza";

    private final float DEVIATION = 0.1f;
    private float mLastLatitude;
    private float mLastLongitude;
    private float mCurrentLatitude;
    private float mCurrenLongitude;
    private final float NY_LATITUDE = 40.7463956f;
    private final float NY_LONGITUDE = -73.9852992f;
    private String mVersion;

    private Storage mStorage;
    private FSService mFsService;
    private int mCount;
    private GPSModule mGpsModule;
    private boolean isFirstRefresh;
    private EventBus mBus = EventBus.getDefault();
    private SharedPreferences mSharedPreferences;


    DataManager(Context c) {
        mStorage = new Storage(c);
        mFsService = createRetrofitService(FSService.class, FSService.FS_SERVER_BASE_URL);
        mCount = 0;
        isFirstRefresh = true;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(c);
        mGpsModule = new GPSModule(c);
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DATE);
        mVersion = year + "";
        if (month < 10) {
            mVersion += 0 + "" + month;
        } else {
            mVersion += "" + month;
        }
        if (day < 10) {
            mVersion += 0 + "" + day;
        } else {
            mVersion += "" + day;
        }
    }
    public void clearInformation() {
        mCount = 0;
        isFirstRefresh = true;
    }

    public synchronized void refresh(int number, boolean isNewYork) {
        mLastLatitude = mSharedPreferences.getFloat(KEY_TO_LATITUDE, 0.0f);
        mLastLongitude = mSharedPreferences.getFloat(KEY_TO_LONGITUDE, 0.0f);
        mCurrentLatitude = 0.0f;
        mCurrenLongitude = 0.0f;
        if (isNewYork) {
            mCurrenLongitude = NY_LONGITUDE;
            mCurrentLatitude = NY_LATITUDE;
        } else {
            Location currentLocation = mGpsModule.getLocation();
            if (null != currentLocation) {
                mCurrenLongitude = (float) currentLocation.getLongitude();
                mCurrentLatitude = (float) currentLocation.getLatitude();
            } else {
                mBus.post(new EventErrorGetLocation());
                return;
            }
        }
        String ll = mCurrentLatitude + "," + mCurrenLongitude;
        int offsetOnServer;
        if (isFirstRefresh) {
            offsetOnServer = 0;
        } else {
            offsetOnServer = mCount;
        }
        if (isFirstRefresh && !isNeedRefreshOfCoord(mLastLongitude, mLastLatitude, mCurrenLongitude, mCurrentLatitude)) {
            mBus.post(new EventNotNeededUpdateStorage());
            isFirstRefresh = false;
            return;
        }

        Log.d(TAG, "mCurrentLatitude = " + mCurrentLatitude);
        Log.d(TAG, "mCurrenLongitude = " + mCurrenLongitude);
        Log.d(TAG, "refresh() start offset = " + offsetOnServer + " number = " + number + " tid = " + Thread.currentThread().getId());
        Log.d(TAG, " Is Coordinate changed = " + isNeedRefreshOfCoord(mLastLongitude, mLastLatitude, mCurrenLongitude, mCurrentLatitude));
        mFsService.getVenues(CLIENT_ID, CLIENT_SECRET, mVersion, ll, QUERY, offsetOnServer, number, SORT_BY_DISTANCE)
                .subscribeOn(Schedulers.from(AsyncTask.THREAD_POOL_EXECUTOR))
                .observeOn(Schedulers.from(AsyncTask.THREAD_POOL_EXECUTOR))
                .subscribe(new Subscriber<JsonObject>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted() tid = " + Thread.currentThread().getId());
                        Log.d(TAG, "mBus.post(new EventUpdateStorage()) !!!!! tid = " + Thread.currentThread().getId());
                        mBus.post(new EventUpdateStorage());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError() called " + e.getMessage() + " tid = " + Thread.currentThread().getId());
                        mBus.post(new EventErrorUpdateStorage());
                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {
                        Log.d(TAG, "onNext!!!!! tid = " + Thread.currentThread().getId());

                        ArrayList<FSVenue> fsVenues = parseFSresponse(jsonObject);

                        //Collections.sort(fsVenues, new FSDistanceComparator());

                        if (isFirstRefresh) {
                            mStorage.clearTable();
                            mCount = 0;
                        }
                        isFirstRefresh = false;

                        mStorage.saveData(fsVenues);
                        Log.d(TAG, "SAVE mCurrentLatitude = " + mCurrentLatitude);
                        Log.d(TAG, "SAVE mCurrenLongitude = " + mCurrenLongitude);
                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                        editor.putFloat(KEY_TO_LATITUDE, mCurrentLatitude)
                                .putFloat(KEY_TO_LONGITUDE, mCurrenLongitude)
                                .apply();
                    }
                });
    }

    private <T> T createRetrofitService(final Class<T> clazz, final String endPoint) {
        Log.d(TAG, "Service interface: " + clazz + " Service end point: " + endPoint);
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(endPoint)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        return retrofit.create(clazz);
    }

    public ArrayList<FSVenue> getСachedVenue() {
        ArrayList<FSVenue> list = mStorage.loadAllData();
        if (null != list) {
            Log.d(TAG, "getСachedVenue list is ok!!! size = " + list.size());
            mCount = list.size();
        }
        return list;
    }

    public synchronized ArrayList<FSVenue> getNewPage(int number) {
        ArrayList<FSVenue> list = mStorage.loadData(mCount, number);
        if (null != list) {
            mCount += list.size();
            for (int i = 0; i < list.size(); i++) {
                Log.d(TAG, "getNewPage i = " + i + " dist = " + list.get(i).getDistance());
            }
        }
        return list;
    }

    private boolean isNeedRefreshOfCoord(float lastLong, float lastLat, float curLong, float curLat) {
        if (lastLat == 0.0f && lastLong == 0.0f) {
            return true;
        }
        if ((Math.abs(lastLat - curLat) > DEVIATION) || (Math.abs(lastLong - curLong) > DEVIATION)) {
            return true;
        }
        return false;
    }

    private ArrayList<FSVenue> parseFSresponse(JsonObject response) {
        ArrayList<FSVenue> temp = new ArrayList<FSVenue>();
        JsonObject jsonObj;
        JsonArray jsonArray;
        if (response.has("response")) {
            jsonObj = response.getAsJsonObject("response");
            if (jsonObj.has("groups")) {
                jsonArray = jsonObj.getAsJsonArray("groups");
                jsonObj = jsonArray.get(0).getAsJsonObject();
                if (jsonObj.has("items")) {
                    jsonArray = jsonObj.getAsJsonArray("items");
                    for (int i = 0; i < jsonArray.size(); i++) {
                        FSVenue obj = new FSVenue();
                        jsonObj = jsonArray.get(i).getAsJsonObject().getAsJsonObject("venue");
                        if (jsonObj.has("name")) {
                            obj.setName(jsonObj.get("name").getAsString());
                            if (jsonObj.has("location")) {
                                if (jsonObj.getAsJsonObject("location").has("distance")) {
                                    obj.setDistance(jsonObj.getAsJsonObject("location").get("distance").getAsInt());
                                    if (jsonObj.has("rating")) {
                                        obj.getBody().setRating(jsonObj.get("rating").getAsString());
                                    }
                                    if (jsonObj.has("url")) {
                                        obj.getBody().setUrl(jsonObj.get("url").getAsString());
                                    }
                                    temp.add(obj);
                                }
                            }
                        }
                    }
                }
            }
        }
        return temp;
    }
}
