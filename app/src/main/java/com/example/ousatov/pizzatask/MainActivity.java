package com.example.ousatov.pizzatask;

import android.Manifest;
import android.app.ListActivity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.ousatov.pizzatask.events.EventErrorUpdateStorage;
import com.example.ousatov.pizzatask.events.EventNotNeededUpdateStorage;
import com.example.ousatov.pizzatask.events.EventUpdateStorage;
import com.example.ousatov.pizzatask.venue.FSVenue;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;


public class MainActivity extends ListActivity implements AbsListView.OnScrollListener {
    private static final String TAG = "US";
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 1;
    private final int PAGE_SIZE = 10;
    private FSVenueAdapter mAdapter;
    private EventBus mBus = EventBus.getDefault();
    private ArrayList<FSVenue> mVenuesList = new ArrayList<>();
    private ArrayList<FSVenue> mNewPage;
    private DataManager mDataManager;
    private View mFooterView;
    private View mHeaderView;
    private ListView mMainListView;

    private boolean isUpdated = false;
    // we will need to take the latitude and the logntitude from a certain point
    // this is the center of New York


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

        Log.d(TAG, "Started !!!");

        if (checkRequiredPermissions()) {
            Log.d(TAG, "startWorking 2 ");
            startWorking();
        }
        Log.d(TAG, "startWorking 3 ");
    }

    private boolean checkRequiredPermissions() {
        Log.d(TAG, "checkRequiredPermissions 1 ");
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "checkRequiredPermissions 2 ");

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Log.d(TAG, "checkRequiredPermissions 3 ");

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                return false;
            } else {
                Log.d(TAG, "checkRequiredPermissions 4 ");

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
                return false;
            }
        } else {
            return true;
        }
    }

    private void startWorking() {
        mMainListView = getListView();
        mDataManager = new DataManager(this);
        mHeaderView = LayoutInflater.from(this).inflate(R.layout.header_view, null);
        mMainListView.addHeaderView(mHeaderView);
        mAdapter = new FSVenueAdapter(this, R.layout.venue_item, mVenuesList);
        setListAdapter(mAdapter);

        mBus.register(this);
        mNewPage = mDataManager.getСachedVenue();
        if (null != mNewPage) {
            mAdapter.addAll(mNewPage);
        }
        mFooterView = LayoutInflater.from(this).inflate(R.layout.footer_view, null);
        mMainListView.addFooterView(mFooterView, null, false);
        mMainListView.setOnScrollListener(this);


        mIsLoading = true;
        mDataManager.refresh(PAGE_SIZE);
    }

    public void onEvent(EventUpdateStorage e) {
        Log.d(TAG, "onEvent(EventUpdateStorage e) !!!!! tid = " + Thread.currentThread().getId());
        synchronized (mAdapter) {
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (null != mFooterView) {
                            mMainListView.removeFooterView(mFooterView);
                            mFooterView = null;
                        }
                    }
                });
            }

            mIsLoading = false;
            Log.d(TAG, "mIsLoading set false ====== tid = " + Thread.currentThread().getId());
        }
    }
    public void onEvent(EventNotNeededUpdateStorage e) {
        Log.d(TAG, "onEvent(EventNotNeededUpdateStorage e) !!!!! tid = " + Thread.currentThread().getId());
        isUpdated = true;
        mIsLoading = false;
        Log.d(TAG, "mIsLoading set false ====== tid = " + Thread.currentThread().getId());
    }
    public void onEvent(EventErrorUpdateStorage e) {
        Log.d(TAG, "onEvent(EventErrorUpdateStorage e) !!!!! tid = " + Thread.currentThread().getId());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplication(), "Error of connection to Foursquare server.", Toast.LENGTH_SHORT).show();
            }
        });
        mMoreDataAvailable = false;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (null != mFooterView) {
                    mMainListView.removeFooterView(mFooterView);
                    mFooterView = null;
                }
            }
        });
        mIsLoading = false;
        Log.d(TAG, "mIsLoading set false ====== tid = " + Thread.currentThread().getId());
    }



    private final int AUTOLOAD_THRESHOLD = 10;

    private boolean mIsLoading = false;
    private boolean mMoreDataAvailable = true;

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//        Log.d(TAG, "onScroll firstVisibleItem = " + firstVisibleItem + " visibleItemCount = " + visibleItemCount + " totalItemCount = " + totalItemCount);
        Log.d(TAG, "111 mIsLoading = " + mIsLoading + " mMoreDataAvailable = " + mMoreDataAvailable + " isUpdated = " + isUpdated);
        if (!mIsLoading && mMoreDataAvailable && isUpdated) {
//            Log.d(TAG, "111 totalItemCount = " + totalItemCount
//                    + " AUTOLOAD_THRESHOLD = " + AUTOLOAD_THRESHOLD
//                    + " firstVisibleItem = " + firstVisibleItem
//                    + " visibleItemCount = " + visibleItemCount
//                    + " totalItemCount - AUTOLOAD_THRESHOLD = " + (totalItemCount - AUTOLOAD_THRESHOLD)
//                    + " firstVisibleItem + visibleItemCoun = " + (firstVisibleItem + visibleItemCount));

            Log.d(TAG, "onScroll1111111111 - refresh");
            if (totalItemCount - AUTOLOAD_THRESHOLD <= firstVisibleItem + visibleItemCount) {
                Log.d(TAG, "onScroll - refresh");
                mIsLoading = true;
                Log.d(TAG, "mIsLoading set true ====== tid = " + Thread.currentThread().getId());
                mDataManager.refresh(PAGE_SIZE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult 1 ");
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.length > 0)
                        && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Log.d(TAG, "onRequestPermissionsResult 2 ");

                    Log.d(TAG, "startWorking 1 ");
                    startWorking();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.d(TAG, "ACCESS_LOCATION permission granted");

                } else {
                    Log.e(TAG, "ACCESS_LOCATION permission denied");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;
            }
            // other 'case' lines to check for other
            // permissions this app might request
            default:
                break;
        }
    }
}


