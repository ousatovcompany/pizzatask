package com.example.ousatov.pizzatask.networking;

import com.google.gson.JsonObject;
import com.example.ousatov.pizzatask.BuildConfig;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;
//client_id={client_id}&client_server={client_server}&v={v}&ll={x},{y}&query={type_venues}")

public interface FSService {
    String FS_SERVER_BASE_URL = "https://api.foursquare.com/v2/venues/";

    @GET("explore")
    Observable<JsonObject> getVenues(@Query("client_id") String client_id,
                                     @Query("client_secret") String client_secret,
                                     @Query("v") String v,
                                     @Query("ll") String ll,
                                     @Query("query") String query,
                                     @Query("offset") int offset,
                                     @Query("limit") int limit,
                                     @Query("sortByDistance") int sortByDistance);
}
