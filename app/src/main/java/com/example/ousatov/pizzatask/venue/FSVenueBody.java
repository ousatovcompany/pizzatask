package com.example.ousatov.pizzatask.venue;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class FSVenueBody {
    private static final String TAG = "US";

    private String mPhone;
    private String mUrl;

    public FSVenueBody() {
        mPhone = null;
        mUrl = null;
    }

    public FSVenueBody(String json) {
        this.deserializeFsBody(json);
    }

    public String getPhone() {
        return mPhone;
    }

    public void setPhone(String p) {
        mPhone = p;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String u) {
        mUrl = u;
    }

    public String serializeFsBody() {
        Gson gs = new Gson();

        return gs.toJson(this);
    }

    public void deserializeFsBody(String json) {
        Gson gs = new Gson();
        Type type = new TypeToken<FSVenueBody>() {}.getType();
        FSVenueBody body = gs.fromJson(json, type);
        this.mPhone = body.getPhone();
        this.mUrl = body.getUrl();
    }
}