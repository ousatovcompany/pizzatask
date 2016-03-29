package com.example.ousatov.pizzatask;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.ousatov.pizzatask.venue.FSVenue;

import org.w3c.dom.Text;

import java.util.List;

public class FSVenueAdapter extends ArrayAdapter<FSVenue> {
    private LayoutInflater mInflater;
    public FSVenueAdapter(Context context, int resource, List<FSVenue> objects) {
        super(context, resource, objects);
        mInflater = (LayoutInflater.from(context));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = mInflater.inflate(R.layout.venue_item, parent, false);
        }
        FSVenue item = getItem(position);
        if (null != item) {
            TextView tvName = (TextView) view.findViewById(R.id.tvName);
            TextView tvDistance = (TextView) view.findViewById(R.id.tvDistance);
            if (null != tvName) {
                tvName.setText(item.getName());
            }
            if (null != tvDistance) {
                tvDistance.setText(item.getDistance() + "m");
            }
        }
        return view;
    }
}
