package com.example.ousatov.pizzatask;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.ousatov.pizzatask.events.EventNotUpdateStorage;
import com.example.ousatov.pizzatask.events.EventUpdateStorage;
import com.example.ousatov.pizzatask.venue.FSVenue;
import com.example.ousatov.pizzatask.networking.FSService;
import com.example.ousatov.pizzatask.storage.Storage;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import de.greenrobot.event.EventBus;

import java.util.ArrayList;
import java.util.Collections;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class DataManager {
    private static final String TAG = "US";

    final private String CLIENT_ID = "F3X4K30EJF0ES3LACQ2FRT4QWMJGGRNFRGWCCEWXR2O4HTRG";
    final private String CLIENT_SECRET = "UBVEJPUF0532QUMMBBVQTPNDA5MMUCKJFFHGHQPISUNBLABV";
    private final int MAX_LIMIT = 19;

    final private int SORT_BY_DISTANCE = 1;
    private Storage mStorage;
    private FSService mFsService;
    private int mCount;
    private boolean isFirstRefresh;
    private EventBus mBus = EventBus.getDefault();

    DataManager(Context c) {
        mStorage = new Storage(c);
        mFsService = createRetrofitService(FSService.class, FSService.FS_SERVER_BASE_URL);
        mCount = 0;
        isFirstRefresh = true;
    }

    public ArrayList<FSVenue> getСachedVenue(int number) {
        Log.d(TAG, "getСachedVenue!!!");
        ArrayList<FSVenue> list = mStorage.loadData(0, number);
        if (null != list) {
            Log.d(TAG, "getСachedVenue list is ok!!!");
            mCount = list.size();
        }
        Log.d(TAG, "getСachedVenue list is null!!!");
        return list;
    }

//    public ArrayList<FSVenue> getUpdatedVenue(int number) {
//        ArrayList<FSVenue> list = mStorage.loadData(0, number);
//        if (null != list) {
//            mCount = list.size();
//        }
//        return list;
//    }

    public synchronized ArrayList<FSVenue> getNewPage(int number) {
        Log.d(TAG, "getNewPage mCount = " + mCount);
        ArrayList<FSVenue> list = mStorage.loadData(mCount, number);
        if (null != list) {
            mCount += list.size();// - mCount;
            for (int i = 0;i < list.size(); i++) {
                Log.d(TAG, "getNewPage i = " + i + " dist = " + list.get(i).getDistance());
            }
        }
        Log.d(TAG, "getNewPage2 mCount = " + mCount);
        return list;
    }

    public synchronized void refresh(String version, String ll, String query, int number) {
        int tmp;
        if (isFirstRefresh) {
            tmp = 0;
        } else {
            tmp = mCount;
        }
        Log.d(TAG, "refresh() start offset = " + tmp + " number = " + number + " tid = " + Thread.currentThread().getId());
        mFsService.getVenues(CLIENT_ID, CLIENT_SECRET, version, ll, query, tmp, number, SORT_BY_DISTANCE)
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
                        mBus.post(new EventNotUpdateStorage());
                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {
                        Log.d(TAG, "onNext!!!!! tid = " + Thread.currentThread().getId());

                        ArrayList<FSVenue> fsVenues = parseFSresponse(jsonObject);


//                        Collections.sort(fsVenues, new FSDistanceComparator());
                        Log.d(TAG, "PRINT ARRAY !!!!!!!!!!!!! size = " + fsVenues.size());
                        for (int i = 0; i < fsVenues.size(); i++) {
                            Log.d(TAG, " i = " + i + " name = " + fsVenues.get(i).getName()
                                    + " distance = " + fsVenues.get(i).getDistance()
                                    + " phone = " + fsVenues.get(i).getBody().getPhone()
                                    + " url = " + fsVenues.get(i).getBody().getUrl());

                        }

                        if (isFirstRefresh) {
                            Log.d(TAG, "isFirstRefresh mCount = " + mCount);
                            mStorage.clearTable();
                            mCount = 0;
                        }
                        isFirstRefresh = false;

                        mStorage.saveData(fsVenues);
                        //TODO: push event
                    }
                });
        Log.d(TAG, "refresh() end" + " tid = " + Thread.currentThread().getId());
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
                                    if (jsonObj.has("phone")) {
                                        obj.getBody().setPhone(jsonObj.get("phone").getAsString());
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
