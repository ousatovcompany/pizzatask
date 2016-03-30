package com.example.ousatov.pizzatask;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;


public class GPSModule implements LocationListener {
    private static final String TAG = "US";
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    private final Context mContext;
    private LocationManager mLocManager;

    private boolean mIsGpsEnabled = false;
    private boolean mIsNetworkEnabled = false;

    public GPSModule(Context c) {
        mContext = c;
    }

    public Location getLocation() {
        Location resultLocation = null;
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "getLocation() permission denied. return null");
            return null;
        }

        mLocManager = (LocationManager) mContext.getSystemService(Service.LOCATION_SERVICE);
        mIsNetworkEnabled = mLocManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (mIsNetworkEnabled) {
            Log.d(TAG, "NETWORK_PROVIDER is enabled.");
            mLocManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
            resultLocation = mLocManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        mIsGpsEnabled = mLocManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (mIsGpsEnabled) {
            if (null == resultLocation) {
                mLocManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                resultLocation = mLocManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        }

        return resultLocation;
    }



    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
