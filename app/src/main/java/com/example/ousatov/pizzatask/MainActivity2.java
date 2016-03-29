//package com.example.ousatov.pizzatask;
//
//import android.app.ListActivity;
//import android.content.Context;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AbsListView;
//import android.widget.ArrayAdapter;
//import android.widget.BaseAdapter;
//import android.widget.ListView;
//import android.widget.TextView;
//
//
//import com.example.ousatov.pizzatask.events.EventUpdateStorage;
//import com.example.ousatov.pizzatask.venue.FSVenue;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import de.greenrobot.event.EventBus;
//
//public class MainActivity2 extends ListActivity implements AbsListView.OnScrollListener {
//    private static final String TAG = "US";
//
//    private ArrayAdapter<String> myAdapter;
//    private EventBus mBus = EventBus.getDefault();
//    private ArrayList<FSVenue> mVenuesList;
//
//    private DataManager mDataManager;
//    private View mFooterView;
//
//    // we will need to take the latitude and the logntitude from a certain point
//    // this is the center of New York
//    final String latitude = "40.7463956";
//    final String longtitude = "-73.9852992";
//    final String v = "20160324";
//    final String query = "pizza";
//    final String ll = latitude + "," + longtitude;
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        mBus.unregister(this);
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        mBus.register(this);
//        Log.d(TAG, "Started !!!");
//
//        mDataManager = new DataManager(this);
//
//        mFooterView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.footer_view, null);
//        getListView().addFooterView(mFooterView, null, false);
//
//        mDataManager.refresh(v, ll, query, 10);
//
//        synchronized (mVenuesList) {
//            mVenuesList = mDataManager.getСachedVenue(10);
//            if (null != mVenuesList) {
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
//    }
//
//    public synchronized void showList() {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                synchronized (mVenuesList) {
//                    Log.d(TAG, "showList !!!!! tid = " + Thread.currentThread().getId());
//                    List<String> listTitle = new ArrayList<String>();
//
//                    for (int i = 0; i < mVenuesList.size(); i++) {
//                        // make a list of the venus that are loaded in the list.
//                        // show the name, the category and the city
//                        listTitle.add(i, mVenuesList.get(i).getName() + ", " + mVenuesList.get(i).getDistance());
//                    }
//                    myAdapter = new ArrayAdapter<String>(MainActivity2.this, R.layout.row_layout, R.id.listText, listTitle);
//                    ListView v = (ListView) findViewById(R.id.listView);
//                    v.setAdapter(myAdapter);
//                }
//            }
//        });
//    }
//
//    public void onEvent(EventUpdateStorage e) {
//        Log.d(TAG, "onEvent(EventUpdateStorage e) !!!!! tid = " + Thread.currentThread().getId());
//
//        synchronized (mVenuesList) {
//            mVenuesList = mDataManager.getNewPage(10);
//        }
//        showList();
//    }
//
//    private final int AUTOLOAD_THRESHOLD = 10;
//    private final int MAXIMUM_ITEMS = 50;
//
//    private boolean mIsLoading = false;
//    private boolean mMoreDataAvailable = true;
//    private boolean mWasLoading = false;
//    @Override
//    public void onScrollStateChanged(AbsListView view, int scrollState) {
//
//    }
//
//    @Override
//    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//        if (!mIsLoading && mMoreDataAvailable) {
//            if (totalItemCount >= MAXIMUM_ITEMS) {
//                mMoreDataAvailable = false;
//                getListView().removeFooterView(mFooterView);
//            } else if (totalItemCount - AUTOLOAD_THRESHOLD <= firstVisibleItem + visibleItemCount) {
//                mIsLoading = true;
//                mDataManager.refresh(v,ll,query, 10);
//            }
//        }
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//        if (mWasLoading) {
//            mWasLoading = false;
//            mIsLoading = true;
//        }
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        mWasLoading = mIsLoading;
//        mIsLoading = false;
//    }
//}
//
//
