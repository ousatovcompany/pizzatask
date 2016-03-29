package com.example.ousatov.pizzatask.venue;


public class FSVenue {
    private String mName;
    private int mDistance;

    private FSVenueBody mBody;

    public FSVenue() {
        mName = null;
        mDistance = -1;
        mBody = new FSVenueBody();
    }

    public void setName(String n) {
        mName = n;
    }

    public String getName() {
        return mName;
    }

    public int getDistance() {
        return mDistance;
    }

    public void setDistance(int d) {
        mDistance = d;
    }

    public FSVenueBody getBody() {
        return mBody;
    }

}
