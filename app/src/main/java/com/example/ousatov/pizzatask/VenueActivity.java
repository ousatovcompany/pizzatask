package com.example.ousatov.pizzatask;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;

public class VenueActivity extends Activity {
    private static final String TAG = "US";

    public static final String KEY_TO_RATING = "KEY_RATING";
    public static final String KEY_TO_URL = "KEY_URL";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_venue);
        Intent intent = getIntent();
        TextView tvPhone = (TextView) findViewById(R.id.tvRating);
        tvPhone.setText(intent.getStringExtra(KEY_TO_RATING));

        Log.d(TAG, "tvUrl   = " + intent.getStringExtra(KEY_TO_URL));
        TextView tvUrl = (TextView) findViewById(R.id.tvUrl);

        tvUrl.setMovementMethod(LinkMovementMethod.getInstance());
//        tvUrl.setText(intent.getStringExtra(KEY_TO_URL));
        tvUrl.setText(Html.fromHtml("<a href=" + intent.getStringExtra(KEY_TO_URL) + ">" + intent.getStringExtra(KEY_TO_URL) + "</a>"));
    }

}
