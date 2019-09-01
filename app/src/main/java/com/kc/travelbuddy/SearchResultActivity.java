package com.kc.travelbuddy;

import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.ybq.android.spinkit.style.Circle;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

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
    private ArrayList<String> summaryList;
    private ArrayList<String> predictionList;
    final WeatherDataStore weatherDataStore=new WeatherDataStore();
    Button btnViewMap;
    ProgressBar progressBar;
    FrameLayout frame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        frame = findViewById(R.id.frame);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame,new WeatherMapFragment()).commit();


        travelTime=getIntent().getStringExtra("travelTime");
        travelDate=getIntent().getStringExtra("travelDate");
        final Bundle bundle = getIntent().getParcelableExtra("locations");
        startLocation=bundle.getParcelable("startLocation");
        endLocation=bundle.getParcelable("endLocation");
        Log.i(TAG, "travelTime: "+travelTime);
        Log.i(TAG, "travelDate: "+travelDate);
        Log.i(TAG, "startLocation: "+startLocation);
        Log.i(TAG, "endLocation: "+endLocation);
        progressBar = findViewById(R.id.spin_kit);
        Circle circle=new Circle();
        progressBar.setIndeterminateDrawable(circle);

        //Find the main cities
        LoadData loadData=new LoadData();
        loadData.execute(startLocation,endLocation);
        //findCitiesUsingGMAPS(startLocation,endLocation);
        //findCities(startLocation,endLocation);
        expandableListView=findViewById(R.id.lvExpandable);
        //expandableListAdapter=new ExpandableListAdapter(this,lisDataHeader,listHashMap);
        //expandableListView.setAdapter(expandableListAdapter);


    }
    private class LoadData extends AsyncTask<LatLng, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }
        @Override
        protected String doInBackground(LatLng... latLngs) {
            String s="sucess";
            findCitiesUsingGMAPS(latLngs[0],latLngs[1]);
            return s;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "Striiing : " + s);
            //progressBar.setVisibility(View.INVISIBLE);

        }
    }

    private void findCities(LatLng startLocation, LatLng endLocation) {
        //final WeatherDataStore weatherDataStore=new WeatherDataStore();
        weatherDataStore.clearArrayLists();
        ROUTEURL="https://route.api.here.com/routing/7.2/calculateroute.json?" +
                "app_id=cMNYP4N3NoQ43gRxNx0c&app_code=DLhtnJkXtQMH02R0uUr3Eg&waypoint0" +
                "=geo!"+startLocation.latitude+","+startLocation.longitude+"&waypoint1" +
                "=geo!"+endLocation.latitude+","+endLocation.longitude+"&mode=fastest;car;traffic:disabled";
        /*Double latitude=7.604613;
        Double stLong=80.072521;
        Double endLat=7.2946291;
        Double endLong=80.5907617;
        ROUTEURL="https://route.api.here.com/routing/7.2/calculateroute.json?" +
                "app_id=cMNYP4N3NoQ43gRxNx0c&app_code=DLhtnJkXtQMH02R0uUr3Eg&waypoint0" +
                "=geo!"+latitude+","+stLong+"&waypoint1" +
                "=geo!"+endLat+","+endLong+"&mode=fastest;car;traffic:disabled";*/
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
                                        //latitudeList.add(tempLat);
                                       // longitudeList.add(tempLongi);
                                        weatherDataStore.setLatitudeList(tempLat);
                                        weatherDataStore.setLongitudeList(tempLongi);
                                        Log.d(TAG, "latitude : " + tempLat);
                                    }
                                }
                                getWeather();
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

    private void findCitiesUsingGMAPS(LatLng startLocation, LatLng endLocation){
        weatherDataStore.clearArrayLists();
        final String APIKEY="AIzaSyAvj6UR1FtWxJwjBiKOyrvdBPK5tnFFuDs";
        ROUTEURL="https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" +startLocation.latitude+","+startLocation.longitude+
                "&destination="+endLocation.latitude+","+endLocation.longitude+
                "&key="+APIKEY;
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
                            JSONArray stepsArray=response.getJSONArray("routes").getJSONObject(0).getJSONArray("legs")
                                    .getJSONObject(0).getJSONArray("steps");
                            JSONObject stepsObject;
                            for (int i=0;i<stepsArray.length();i++)
                            {
                                stepsObject=stepsArray.getJSONObject(i);
                                Double tempLat=Double.parseDouble(stepsObject.getJSONObject("end_location").getString("lat"));
                                Double tempLongi=Double.parseDouble(stepsObject.getJSONObject("end_location").getString("lng"));
                                weatherDataStore.setLatitudeList(tempLat);
                                weatherDataStore.setLongitudeList(tempLongi);
                                Log.d(TAG, "latitude : " + tempLat);
                                Log.d(TAG, "Long : " + tempLongi);
                            }



                            getWeather();

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


        for(int i=0;i<weatherDataStore.getLatitudeList().size();i++){
            WEATHERURL="https://api.darksky.net/forecast/91127ba2ca9e159767ac3aa34425dffd" +
                    "/"+weatherDataStore.getLatitudeList().get(i)+","+weatherDataStore.getLongitudeList().get(i)+
                    ","+travelDate+"T"+travelTime+":00?exclude=daily,hourly";
            summaryList=new ArrayList<>();
            predictionList=new ArrayList<>();
            requestQueue= Volley.newRequestQueue(SearchResultActivity.this);
            final AtomicInteger requestsCounter = new AtomicInteger(0);
            //Log.d(TAG, "JSON Weather :" );
            final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    WEATHERURL,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            //Log.d(TAG, "JSON ResponseBody :" + response);
                            // Process the JSON
                            try{
                                // Get the JSON array
                                JSONObject jsonResponse = response.getJSONObject("currently");
                                String summary=jsonResponse.getString("summary");
                                String prediction=jsonResponse.getString("icon");
                                Log.d(TAG, "summary : " + summary +"| prediction: "+prediction);
                                summaryList.add(summary);
                                predictionList.add(prediction);
                                if (summaryList.size()==weatherDataStore.getLatitudeList().size()){
                                    initData();
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

    }

    private void initData() {
        listHashMap=new HashMap<>();
        ArrayList<String> cityNames=new ArrayList<>();
        ArrayList<String> filteredCityNames=new ArrayList<>();
        DecodeAdress decodeAdress=new DecodeAdress();
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        for(int i=0;i<weatherDataStore.getLatitudeList().size();i++){
            cityNames.add(decodeAdress.getDecodedCityName(weatherDataStore.getLatitudeList().get(i),
                    weatherDataStore.getLongitudeList().get(i),geocoder));
        }
        /**
         * lisDataHeader=citynames
         * details=summaryList,predictionList
         * */

        List<String> details=new ArrayList<>();
        details.add("Details Test");

        List<String> details2=new ArrayList<>();
        details2.add("Details Test 2");
        details2.add("Details Test  2 22");

        for (int i = 0; i < summaryList.size(); i++) {
            List<String> temp = new ArrayList<>();
            String tempCity=cityNames.get(i);
            if (!tempCity.matches("[0-9]+") & tempCity.length()>=3)
            {
                filteredCityNames.add(cityNames.get(i));
                temp.add("Weather Summary \t:  " + summaryList.get(i));
                temp.add("Prediction \t\t\t\t:  " + predictionList.get(i));
                listHashMap.put(cityNames.get(i), temp);
                Log.d(TAG, "Hashmap " + temp);
                Log.d(TAG, "cityNumber " + cityNames.get(i));
            }
            else {

            }
            progressBar.setVisibility(View.INVISIBLE);
        }

        expandableListAdapter=new ExpandableListAdapter(this,filteredCityNames,listHashMap);
        expandableListView.setAdapter(expandableListAdapter);
    }
}
