package com.kc.travelbuddy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "SearchActivity";
    LatLng startLocation,endLocation;
    String travelTime,travelDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        travelTime=getIntent().getStringExtra("travelTime");
        travelDate=getIntent().getStringExtra("travelDate");
        Bundle bundle = getIntent().getParcelableExtra("locations");
        startLocation=bundle.getParcelable("startLocation");
        endLocation=bundle.getParcelable("endLocation");
        Log.i(TAG, "travelTime"+travelTime);
        Log.i(TAG, "travelDate"+travelDate);
        Log.i(TAG, "startLocation"+startLocation);
        Log.i(TAG, "endLocation"+endLocation);

    }
}
