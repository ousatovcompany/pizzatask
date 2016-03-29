package com.example.ousatov.pizzatask;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;

import com.example.ousatov.pizzatask.events.EventNotUpdateStorage;
import com.example.ousatov.pizzatask.events.EventUpdateStorage;
import com.example.ousatov.pizzatask.venue.FSVenue;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;


public class MainActivity extends ListActivity implements AbsListView.OnScrollListener {
    private static final String TAG = "US";
    private static final String TAG2 = "USA";
    //    ArrayAdapter<String> myAdapter;
//    ArrayAdapter<String> myAdapter;
    private FSVenueAdapter mAdapter;
    private EventBus mBus = EventBus.getDefault();
    private ArrayList<FSVenue> mVenuesList = new ArrayList<>();

    private final List<String> mListTitle = new ArrayList<String>();

    private DataManager mDataManager;
    private View mFooterView;

    private boolean isUpdated = false;
    // we will need to take the latitude and the logntitude from a certain point
    // this is the center of New York
    private final String latitude = "40.7463956";
    private final String longtitude = "-73.9852992";
    private final String v = "20160324";
    private final String query = "pizza";
    private final String ll = latitude + "," + longtitude;
    private final int PAG_SIZE = 10;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBus.unregister(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        mBus.register(this);
        Log.d(TAG2, "Started !!!");

        mDataManager = new DataManager(this);
        mFooterView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.loading_view, null);
        getListView().addFooterView(mFooterView, null, false);

        mAdapter = new FSVenueAdapter(MainActivity.this, R.layout.venue_item, mVenuesList);
        setListAdapter(mAdapter);

        mVenuesList.addAll(mDataManager.getСachedVenue(PAG_SIZE));
        mAdapter.notifyDataSetChanged();
//        showList();
        mDataManager.refresh(v, ll, query, PAG_SIZE);
        mIsLoading = true;
//        if (null != mVenuesList) {
//            synchronized (mVenuesList) {
//
//                showList();
//                Log.d(TAG, "PRINT ARRAY MAIN ACTIVITY !!!!!!!!!!!!! size = " + mVenuesList.size());
//                for (int i = 0; i < mVenuesList.size(); i++) {
//                    Log.d(TAG, " i = " + i + " name = " + mVenuesList.get(i).getName()
//                            + " distance = " + mVenuesList.get(i).getDistance()
//                            + " phone = " + mVenuesList.get(i).getBody().getPhone()
//                            + " url = " + mVenuesList.get(i).getBody().getUrl());
//                }
//            }
//        }
//
        getListView().setOnScrollListener(this);
    }

    public void onEvent(EventUpdateStorage e) {
        Log.d(TAG2, "onEvent(EventUpdateStorage e) !!!!! tid = " + Thread.currentThread().getId());
        synchronized (mVenuesList) {
            Log.d(TAG2, " ++++++++++++++++++++++++++++++++++++++ 1 tid = " + Thread.currentThread().getId());
            ArrayList<FSVenue> newPage = mDataManager.getNewPage(PAG_SIZE);
            if (null != newPage) {
                Log.d(TAG2, " ++++++++++++++++++++++++++++++++++++++ 2 tid = " + Thread.currentThread().getId());
                Log.d(TAG2, " mVenuesList.addAll(newPage) size = " + newPage.size());
                if (!isUpdated) {
                    mVenuesList.clear();
                    isUpdated = true;
                }
                mVenuesList.addAll(newPage);
            } else {

                Log.d(TAG2, " ++++++++++++++++++++++++++++++++++++++ 3 tid = " + Thread.currentThread().getId());
                Log.d(TAG2, "newPage is null");
                mMoreDataAvailable = false;
                getListView().removeFooterView(mFooterView);
            }
            Log.d(TAG2, " ++++++++++++++++++++++++++++++++++++++ 4 tid = " + Thread.currentThread().getId());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyDataSetChanged();
                }
            });
            Log.d(TAG2, " ++++++++++++++++++++++++++++++++++++++ 5 tid = " + Thread.currentThread().getId());
            mIsLoading = false;
            Log.d(TAG2, "mIsLoading set false ====== tid = " + Thread.currentThread().getId());
//        mIsLoading = false;
//        Log.d(TAG2, "mIsLoading set false ====== tid = " + Thread.currentThread().getId());
//        Log.d(TAG2, "onEvent(EventUpdateStorage e) !!!!! tid = " + Thread.currentThread().getId());
//
//        synchronized (mVenuesList) {
//            mVenuesList = mDataManager.getNewPage(PAG_SIZE);
//        }
//        isUpdated = true;
//        showList();
        }
    }

    public void onEvent(EventNotUpdateStorage e) {
        Log.d(TAG2, "onEvent(EventNotUpdateStorage e) !!!!! tid = " + Thread.currentThread().getId());
        synchronized (mVenuesList) {
            Log.d(TAG2, " ++++++++++++++++++++++++++++++++++++++ 1 tid = " + Thread.currentThread().getId());
            ArrayList<FSVenue> newPage = mDataManager.getNewPage(PAG_SIZE);
            if (null != newPage) {
                Log.d(TAG2, " ++++++++++++++++++++++++++++++++++++++ 2 tid = " + Thread.currentThread().getId());
                Log.d(TAG2, " mVenuesList.addAll(newPage) size = " + newPage.size());
                mVenuesList.addAll(newPage);
            } else {

                Log.d(TAG2, " ++++++++++++++++++++++++++++++++++++++ 3 tid = " + Thread.currentThread().getId());
                Log.d(TAG2, "newPage is null");
                mMoreDataAvailable = false;
                getListView().removeFooterView(mFooterView);
            }
            Log.d(TAG2, " ++++++++++++++++++++++++++++++++++++++ 4 tid = " + Thread.currentThread().getId());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyDataSetChanged();
                }
            });
            Log.d(TAG2, " ++++++++++++++++++++++++++++++++++++++ 5 tid = " + Thread.currentThread().getId());
            mIsLoading = false;
            Log.d(TAG2, "mIsLoading set false ====== tid = " + Thread.currentThread().getId());
        }
    }


    private final int AUTOLOAD_THRESHOLD = 10;

    private boolean mIsLoading = false;
    private boolean mMoreDataAvailable = true;
    private boolean mWasLoading = false;

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
//        Log.d(TAG, "onScrollStateChanged scrollState = " + scrollState);

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//        Log.d(TAG, "onScroll firstVisibleItem = " + firstVisibleItem + " visibleItemCount = " + visibleItemCount + " totalItemCount = " + totalItemCount);
        Log.d(TAG2, "111 mIsLoading = " + mIsLoading + " mMoreDataAvailable = " + mMoreDataAvailable + "====== tid = " + Thread.currentThread().getId());
        if (!mIsLoading && mMoreDataAvailable) {
//            if (totalItemCount >= MAXIMUM_ITEMS) {
//                Log.d(TAG2, "onScroll - totalItemCount >= MAXIMUM_ITEMS");
//                mMoreDataAvailable = false;
//                getListView().removeFooterView(mFooterView);
//            } else
            Log.d(TAG2, "111 totalItemCount = " + totalItemCount
                    + " AUTOLOAD_THRESHOLD = " + AUTOLOAD_THRESHOLD
                    + " firstVisibleItem = " + firstVisibleItem
                    + " visibleItemCount = " + visibleItemCount
                    + " totalItemCount - AUTOLOAD_THRESHOLD = " + (totalItemCount - AUTOLOAD_THRESHOLD)
                    + " firstVisibleItem + visibleItemCoun = " + (firstVisibleItem + visibleItemCount));

            if (totalItemCount - AUTOLOAD_THRESHOLD <= firstVisibleItem + visibleItemCount) {

                Log.d(TAG2, "onScroll - refresh");
                mIsLoading = true;
                Log.d(TAG2, "mIsLoading set true ====== tid = " + Thread.currentThread().getId());
                mDataManager.refresh(v, ll, query, PAG_SIZE);
            } else {

            }
        }
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        if (mWasLoading) {
//            mWasLoading = false;
//            mIsLoading = true;
//            Log.d(TAG2, "mIsLoading set true11 ====== tid = " + Thread.currentThread().getId());
//        }
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        mWasLoading = mIsLoading;
//        mIsLoading = false;
//        Log.d(TAG2, "mIsLoading set false11 ====== tid = " + Thread.currentThread().getId());
//    }
}


