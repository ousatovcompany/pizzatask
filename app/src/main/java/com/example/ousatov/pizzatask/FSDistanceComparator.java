package com.example.ousatov.pizzatask;

import com.example.ousatov.pizzatask.venue.FSVenue;

import java.util.Comparator;

public class FSDistanceComparator implements Comparator<FSVenue> {

    @Override
    public int compare(FSVenue lhs, FSVenue rhs) {
        return lhs.getDistance() - rhs.getDistance();
    }
}
