package com.kc.travelbuddy;

import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ExpandableListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SearchResultActivity extends AppCompatActivity {
    private static final String TAG = "SearchResultActivity";
    LatLng startLocation,endLocation;
    String travelTime,travelDate;
    private RequestQueue requestQueue ;
    private String ROUTEURL,WEATHERURL;
    private ExpandableListView expandableListView;
    private ExpandableListAdapter expandableListAdapter;
    private List<String> lisDataHeader;
    private HashMap<String,List<String>> listHashMap;
    private ArrayList<Double> latitudeList;
    private ArrayList<Double> longitudeList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        DecodeAdress decodeAdress=new DecodeAdress();
        latitudeList=new ArrayList<>();
        longitudeList=new ArrayList<>();

    /*    travelTime=getIntent().getStringExtra("travelTime");
        travelDate=getIntent().getStringExtra("travelDate");
        Bundle bundle = getIntent().getParcelableExtra("locations");
        startLocation=bundle.getParcelable("startLocation");
        endLocation=bundle.getParcelable("endLocation");
        Log.i(TAG, "travelTime"+travelTime);
        Log.i(TAG, "travelDate"+travelDate);
        Log.i(TAG, "startLocation"+startLocation);
        Log.i(TAG, "endLocation"+endLocation);


        String cityName=decodeAdress.getDecodedCityName(startLocation.latitude,startLocation.longitude,geocoder);
        Log.i(TAG, "####cityName"+cityName);

        //Find the main cities*/
        findCities(startLocation,endLocation);
        getWeather();

        expandableListView=findViewById(R.id.lvExpandable);
        expandableListAdapter=new ExpandableListAdapter(this,lisDataHeader,listHashMap);
        expandableListView.setAdapter(expandableListAdapter);

    }

    private void findCities(LatLng startLocation, LatLng endLocation) {
        /*ROUTEURL="https://route.api.here.com/routing/7.2/calculateroute.json?" +
                "app_id=cMNYP4N3NoQ43gRxNx0c&app_code=DLhtnJkXtQMH02R0uUr3Eg&waypoint0" +
                "=geo!"+startLocation.latitude+","+startLocation.longitude+"&waypoint1" +
                "=geo!"+endLocation.latitude+","+endLocation.longitude+"&mode=fastest;car;traffic:disabled";*/
        final Double latitude=7.604613;
        Double stLong=80.072521;
        Double endLat=7.2946291;
        Double endLong=80.5907617;
        ROUTEURL="https://route.api.here.com/routing/7.2/calculateroute.json?" +
                "app_id=cMNYP4N3NoQ43gRxNx0c&app_code=DLhtnJkXtQMH02R0uUr3Eg&waypoint0" +
                "=geo!"+latitude+","+stLong+"&waypoint1" +
                "=geo!"+endLat+","+endLong+"&mode=fastest;car;traffic:disabled";
        requestQueue= Volley.newRequestQueue(SearchResultActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                ROUTEURL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "JSON ResponseBody :" + response);
                        // Process the JSON
                        try{
                            // Get the JSON array
                            JSONObject jsonResponse = response.getJSONObject("response");
                            //Log.d(TAG, "jsonResponse : " + jsonResponse);
                            JSONArray route=jsonResponse.getJSONArray("route");
                            //Log.d(TAG, "route : " + route);
                            for(int i=0;i<route.length();i++) {
                                JSONObject routeObject=route.getJSONObject(i);
                                JSONArray leg=routeObject.getJSONArray("leg");
                                //Log.d(TAG, "leg : " + leg);
                                for(int j=0;j<leg.length();j++) {
                                    JSONObject legObject=leg.getJSONObject(j);
                                    //Log.d(TAG, "legObject : " + legObject);
                                    JSONArray maneuver=legObject.getJSONArray("maneuver");
                                    Log.d(TAG, "maneuver : " + maneuver);
                                    for(int k=0;k<maneuver.length();k++) {
                                        JSONObject positionObject=maneuver.getJSONObject(k);
                                       // Log.d(TAG, "position : " + positionObject);
                                        JSONObject position=positionObject.getJSONObject("position");
                                        //JSONArray latitude=position.getJSONArray("position");
                                        Double tempLat=Double.parseDouble(position.getString("latitude"));
                                        Double tempLongi=Double.parseDouble(position.getString("longitude"));
                                        latitudeList.add(tempLat);
                                        longitudeList.add(tempLongi);
                                        Log.d(TAG, "latitude : " + tempLat);
                                    }
                                }

                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        // Do something when error occurred
                    }
                }
        );

        // Add JsonObjectRequest to the RequestQueue
        requestQueue.add(jsonObjectRequest);


    }

    private void getWeather() {
        for(int i=0;i<latitudeList.size();i++){
            WEATHERURL="https://api.darksky.net/forecast/91127ba2ca9e159767ac3aa34425dffd" +
                    "/"+latitudeList.get(i)+","+longitudeList.get(i)+",2019-09-25T08:30:55?exclude=daily,hourly";
        }

    }

    private void initData() {
        lisDataHeader =new ArrayList<>();
        listHashMap=new HashMap<>();

        lisDataHeader.add("Test1");
        lisDataHeader.add("Test2");
        lisDataHeader.add("Test3");


        List<String> details=new ArrayList<>();
        details.add("Details Test");

        List<String> details2=new ArrayList<>();
        details2.add("Details Test 2");
        details2.add("Details Test  2 22");

        listHashMap.put(lisDataHeader.get(0),details);
        listHashMap.put(lisDataHeader.get(1),details2);
    }
}