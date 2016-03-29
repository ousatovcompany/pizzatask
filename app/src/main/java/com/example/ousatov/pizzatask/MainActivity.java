package com.example.ousatov.pizzatask;

import android.app.ListActivity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.TextView;

import com.example.ousatov.pizzatask.events.EventNotUpdateStorage;
import com.example.ousatov.pizzatask.events.EventUpdateStorage;
import com.example.ousatov.pizzatask.venue.FSVenue;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;


public class MainActivity extends ListActivity implements AbsListView.OnScrollListener {
    private static final String TAG = "US";
    private FSVenueAdapter mAdapter;
    private EventBus mBus = EventBus.getDefault();
    private ArrayList<FSVenue> mVenuesList = new ArrayList<>();
    private ArrayList<FSVenue> mNewPage;
    private DataManager mDataManager;
    private View mFooterView;
    private View mHeaderView;

    private boolean isUpdated = false;
    // we will need to take the latitude and the logntitude from a certain point
    // this is the center of New York
    private final String latitude = "40.7463956";
    private final String longtitude = "-73.9852992";
    private final String v = "20160324";
    private final String query = "pizza";
    private final String ll = latitude + "," + longtitude;
    private final int PAGE_SIZE = 10;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBus.unregister(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mBus.register(this);
        Log.d(TAG, "Started !!!");

        mDataManager = new DataManager(this);
        mFooterView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.footer_view, null);
        getListView().addFooterView(mFooterView, null, false);

        mHeaderView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.header_view, null);
        getListView().addHeaderView(mHeaderView);

        mAdapter = new FSVenueAdapter(MainActivity.this, R.layout.venue_item, mVenuesList);
        setListAdapter(mAdapter);

        mAdapter.addAll(mDataManager.getСachedVenue(PAGE_SIZE));
//        mAdapter.notifyDataSetChanged();
        mDataManager.refresh(v, ll, query, PAGE_SIZE);
        mIsLoading = true;
        getListView().setOnScrollListener(this);
    }

    public void onEvent(EventUpdateStorage e) {
        Log.d(TAG, "onEvent(EventUpdateStorage e) !!!!! tid = " + Thread.currentThread().getId());
        synchronized (mAdapter) {
//            ArrayList<FSVenue> newPage = mDataManager.getNewPage(PAGE_SIZE);
            mNewPage = mDataManager.getNewPage(PAGE_SIZE);
            if (null != mNewPage) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isUpdated) {
                            mAdapter.clear();
                            isUpdated = true;
                        }
                        if (null != mNewPage) {
                            mAdapter.addAll(mNewPage);
                        }
                    }
                });
                Log.d(TAG, " mAdapter.addAll(newPage) size = " + mNewPage.size());
            } else {
                Log.d(TAG, "newPage is null");

                mMoreDataAvailable = false;
                getListView().removeFooterView(mFooterView);
            }

            mIsLoading = false;
            Log.d(TAG, "mIsLoading set false ====== tid = " + Thread.currentThread().getId());
        }
    }

    public void onEvent(EventNotUpdateStorage e) {
        Log.d(TAG, "onEvent(EventNotUpdateStorage e) !!!!! tid = " + Thread.currentThread().getId());
        synchronized (mAdapter) {
//            ArrayList<FSVenue> newPage = mDataManager.getNewPage(PAGE_SIZE);
            mNewPage = mDataManager.getNewPage(PAGE_SIZE);
            if (null != mNewPage) {
                Log.d(TAG, " mVenuesList.addAll(newPage) size = " + mNewPage.size());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (null != mNewPage) {
                            mAdapter.addAll(mNewPage);
                        }
                    }
                });
            } else {
                Log.d(TAG, "newPage is null");
                mMoreDataAvailable = false;
                getListView().removeFooterView(mFooterView);
            }
            Log.d(TAG, " ++++++++++++++++++++++++++++++++++++++ 4 tid = " + Thread.currentThread().getId());

            mIsLoading = false;
            Log.d(TAG, "mIsLoading set false ====== tid = " + Thread.currentThread().getId());
        }
    }


    private final int AUTOLOAD_THRESHOLD = 10;

    private boolean mIsLoading = false;
    private boolean mMoreDataAvailable = true;
    private boolean mWasLoading = false;

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//        Log.d(TAG, "onScroll firstVisibleItem = " + firstVisibleItem + " visibleItemCount = " + visibleItemCount + " totalItemCount = " + totalItemCount);
//        Log.d(TAG, "111 mIsLoading = " + mIsLoading + " mMoreDataAvailable = " + mMoreDataAvailable + "====== tid = " + Thread.currentThread().getId());
        if (!mIsLoading && mMoreDataAvailable) {
//            Log.d(TAG, "111 totalItemCount = " + totalItemCount
//                    + " AUTOLOAD_THRESHOLD = " + AUTOLOAD_THRESHOLD
//                    + " firstVisibleItem = " + firstVisibleItem
//                    + " visibleItemCount = " + visibleItemCount
//                    + " totalItemCount - AUTOLOAD_THRESHOLD = " + (totalItemCount - AUTOLOAD_THRESHOLD)
//                    + " firstVisibleItem + visibleItemCoun = " + (firstVisibleItem + visibleItemCount));

            if (totalItemCount - AUTOLOAD_THRESHOLD <= firstVisibleItem + visibleItemCount) {
                Log.d(TAG, "onScroll - refresh");
                mIsLoading = true;
                Log.d(TAG, "mIsLoading set true ====== tid = " + Thread.currentThread().getId());
                mDataManager.refresh(v, ll, query, PAGE_SIZE);
            }
        }
    }

}


