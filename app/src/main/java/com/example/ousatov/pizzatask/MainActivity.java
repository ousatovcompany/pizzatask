package com.example.ousatov.pizzatask;

import android.Manifest;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.example.ousatov.pizzatask.events.EventErrorGetLocation;
import com.example.ousatov.pizzatask.events.EventErrorUpdateStorage;
import com.example.ousatov.pizzatask.events.EventNotNeededUpdateStorage;
import com.example.ousatov.pizzatask.events.EventUpdateStorage;
import com.example.ousatov.pizzatask.venue.FSVenue;

import java.util.ArrayList;
import java.util.Calendar;

import de.greenrobot.event.EventBus;


public class MainActivity extends ListActivity implements AbsListView.OnScrollListener {
    private static final String TAG = "US";

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 1;
    private static final int MY_ACTIVITY_REQUEST_ACCESS_LOCATION = 12;
    private final int PAGE_SIZE = 10;
    private final int AUTOLOAD_THRESHOLD = 10;

    private boolean mIsLoading = false;
    private boolean mMoreDataAvailable = true;
    private boolean isUpdated = false;
    private boolean isNewYork = false;

    private FSVenueAdapter mAdapter;
    private DataManager mDataManager;

    private ArrayList<FSVenue> mVenuesList = new ArrayList<>();
    private ArrayList<FSVenue> mNewPage;

    private View mFooterView;
    private View mHeaderView;

    private ListView mMainListView;
    private Button mBtnRefresh;
    private CheckBox mIsNY;

    private EventBus mBus = EventBus.getDefault();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBus.unregister(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Log.d(TAG, "Started !!!");


        mIsNY = (CheckBox) findViewById(R.id.cb_is_ny);
        mBtnRefresh = (Button) findViewById(R.id.btn_refresh);
        mBtnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick refresh");
                if (!mIsLoading) {
                    if (checkRequiredPermissions()) {
                        refresh();
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Cannot refresh. try again.",
                            Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

        mBus.register(this);
        mMainListView = getListView();

        mAdapter = new FSVenueAdapter(this, R.layout.venue_item, mVenuesList);
        setListAdapter(mAdapter);

        mDataManager = new DataManager(this);

        if (checkRequiredPermissions()) {
            startWorking();
        }
    }

    private void refresh() {
        stopWorking();
        startWorking();
    }

    private void stopWorking() {
        Log.d(TAG, "stopWorking()");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.clear();
                if (null != mFooterView) {
                    mMainListView.removeFooterView(mFooterView);
                    mFooterView = null;
                }
                if (null != mHeaderView) {
                    mMainListView.removeHeaderView(mHeaderView);
                    mHeaderView = null;
                }
            }
        });
        isUpdated = false;
        mIsLoading = false;
        mMoreDataAvailable = true;
        mDataManager.clearInformation();
    }

    private void startWorking() {
        Log.d(TAG, "startWorking()");
        isNewYork = mIsNY.isChecked();
        mHeaderView = LayoutInflater.from(this).inflate(R.layout.header_view, null);
        mMainListView.addHeaderView(mHeaderView);
        mNewPage = mDataManager.getСachedVenue();
        if (null != mNewPage) {
            mAdapter.addAll(mNewPage);
        }

        mFooterView = LayoutInflater.from(this).inflate(R.layout.footer_view, null);
        mMainListView.addFooterView(mFooterView, null, false);

        mMainListView.setOnScrollListener(this);

        mIsLoading = true;
        mDataManager.refresh(PAGE_SIZE, isNewYork);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Log.d(TAG, "onListItemClick() position = " + position);
        super.onListItemClick(l, v, position, id);
        if (position > 0) {
            FSVenue clickedVenue = (FSVenue) getListAdapter().getItem(position - 1);
            Intent intent = new Intent(this, VenueActivity.class);
            intent.putExtra(VenueActivity.KEY_TO_RATING, clickedVenue.getBody().getRating());
            intent.putExtra(VenueActivity.KEY_TO_URL, clickedVenue.getBody().getUrl());
            startActivity(intent);
        }
    }

    private boolean checkRequiredPermissions() {
        Log.d(TAG, "checkRequiredPermissions()");
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                showNoStoragePermissionSnackbar();
                return false;
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult()");
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.length > 0)
                        && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Log.d(TAG, "ACCESS_LOCATION permission granted");

                    startWorking();
                } else {
                    Log.e(TAG, "ACCESS_LOCATION permission denied");
                }
                break;
            }
            default:
                break;
        }
    }

    public void showNoStoragePermissionSnackbar() {
        Log.d(TAG, "showNoStoragePermissionSnackbar()");
        Snackbar.make(this.getListView(), "Storage permission isn't granted", Snackbar.LENGTH_LONG)
                .setAction("SETTINGS", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openApplicationSettings();
                        Toast.makeText(getApplicationContext(),
                                "Open Permissions and grant the Storage permission",
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                })
                .show();
    }

    public void openApplicationSettings() {
        Log.d(TAG, "openApplicationSettings()");
        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(appSettingsIntent, MY_ACTIVITY_REQUEST_ACCESS_LOCATION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_ACTIVITY_REQUEST_ACCESS_LOCATION) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (!mIsLoading && mMoreDataAvailable && isUpdated) {
            if (totalItemCount - AUTOLOAD_THRESHOLD <= firstVisibleItem + visibleItemCount) {
                Log.d(TAG, "onScroll - refresh");
                mIsLoading = true;
                Log.d(TAG, "mIsLoading set true ====== tid = " + Thread.currentThread().getId());

                mDataManager.refresh(PAGE_SIZE, isNewYork);
            }
        }
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
        Log.d(TAG, "onEvent(EventNotNeededUpdateStorage e)");
        isUpdated = true;
        mIsLoading = false;
        Log.d(TAG, "mIsLoading set false ====== tid = " + Thread.currentThread().getId());
    }

    public void onEvent(EventErrorUpdateStorage e) {
        Log.d(TAG, "onEvent(EventErrorUpdateStorage e)");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplication(), "Error of connection to Foursquare server.", Toast.LENGTH_LONG).show();
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

    public void onEvent(EventErrorGetLocation e) {
        Log.d(TAG, "onEvent(EventErrorGetLocation e)");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplication(), "Error of get location.", Toast.LENGTH_SHORT).show();

                if (null != mFooterView) {
                    mMainListView.removeFooterView(mFooterView);
                    mFooterView = null;
                }
            }
        });
        mIsLoading = false;
    }
}


