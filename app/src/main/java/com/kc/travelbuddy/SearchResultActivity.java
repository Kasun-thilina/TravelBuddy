package com.kc.travelbuddy;

import android.graphics.Color;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Column;
import com.anychart.enums.HoverMode;
import com.anychart.enums.Position;
import com.anychart.enums.TooltipPositionMode;
import com.github.ybq.android.spinkit.style.Circle;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class SearchResultActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "SearchResultActivity";
    LatLng startLocation,endLocation;
    String travelTime,travelDate;
    private RequestQueue requestQueue ;
    private RequestQueue requestQueue2 ;
    private String ROUTEURL,WEATHERURL;
    private ExpandableListView expandableListView;
    private ExpandableListAdapter expandableListAdapter;
    private List<String> lisDataHeader;
    private HashMap<String,List<String>> listHashMap;
    private ArrayList<String> summaryList;
    private ArrayList<String> predictionList;
    private ArrayList<Double> temperatureList;
    final WeatherDataStore weatherDataStore=new WeatherDataStore();
    final String APIKEY="AIzaSyAvj6UR1FtWxJwjBiKOyrvdBPK5tnFFuDs";
    Button bt;
    TextView tvDuration,tvDistance;
    ProgressBar progressBar;
    FrameLayout frame;
    GoogleMap mMap;
    MapView mapView;
    View view;
    AnyChartView anyChartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        mapView = findViewById(R.id.mapView2);
        mapView.onCreate(savedInstanceState);

        anyChartView=findViewById(R.id.any_chart_view);
        tvDuration=findViewById(R.id.tvTravelDuration);
        tvDistance=findViewById(R.id.tvTravelDistance);
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

        expandableListView=findViewById(R.id.lvExpandable);

        //expandableListAdapter=new ExpandableListAdapter(this,lisDataHeader,listHashMap);
        //expandableListView.setAdapter(expandableListAdapter);


    }
    @Override
    protected void onResume()
    {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;
        DecodeAdress decodeAdress=new DecodeAdress();
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());


        // Add a marker in Sydney and move the camera
        LatLng SL = new LatLng(7.8731, 80.7718);
        //mMap.addMarker(new MarkerOptions().position(SL).title("Sri Lanka"));
        mMap.setMyLocationEnabled(true);
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(startLocation));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(startLocation.latitude, startLocation.longitude)).zoom(15).build();
        mMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));

        mMap.addMarker(new MarkerOptions().position(startLocation).title(decodeAdress
                .getDecodedCityName(startLocation.latitude,startLocation.longitude,geocoder)));
        mMap.addMarker(new MarkerOptions().position(endLocation).title(decodeAdress
                .getDecodedCityName(endLocation.latitude,endLocation.longitude,geocoder)));
        for (int i=0;i<weatherDataStore.getPolylines().size();i++) {
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.width(15);
            polylineOptions.color(Color.BLUE);
            polylineOptions.addAll(PolyUtil.decode(weatherDataStore.getPolylines().get(i)));
            mMap.addPolyline(polylineOptions);
        }

    }
    @Override
    public final void onDestroy()
    {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public final void onLowMemory()
    {
        mapView.onLowMemory();
        super.onLowMemory();
    }

    @Override
    public final void onPause()
    {
        mapView.onPause();
        super.onPause();
    }

    private class LoadData extends AsyncTask<LatLng, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }
        @Override
        protected String doInBackground(LatLng... latLngs) {
            String s="success";
            findCitiesUsingGMAPS(latLngs[0],latLngs[1]);
            LoadTripData loadTripData=new LoadTripData();
            loadTripData.execute(latLngs[0],latLngs[1]);
            //findCities(latLngs[0],latLngs[1]);
            return s;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "Striiing : " + s);


            //progressBar.setVisibility(View.INVISIBLE);

        }
    }
    private class LoadTripData extends AsyncTask<LatLng, Void, ArrayList<String>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }
        @Override
        protected ArrayList<String> doInBackground(LatLng... latLngs) {
            String s="sucess";
            final ArrayList<String> polylines=new ArrayList<>();
            ROUTEURL="https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=" +startLocation.latitude+","+startLocation.longitude+
                    "&destination="+endLocation.latitude+","+endLocation.longitude+
                    "&key="+APIKEY;
            requestQueue2= Volley.newRequestQueue(SearchResultActivity.this);
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
                                JSONArray stepsArray=response.getJSONArray("routes")
                                        .getJSONObject(0).getJSONArray("legs")
                                        .getJSONObject(0).getJSONArray("steps");
                                JSONObject directions=response.getJSONArray("routes")
                                        .getJSONObject(0).getJSONArray("legs").getJSONObject(0);

                                String duration=directions.getJSONObject("duration").getString("text");
                                String distance=directions.getJSONObject("distance").getString("text");
                                Log.d(TAG, "duration : " + duration);
                                tvDuration.setText("Travel Duration : "+duration);
                                tvDistance.setText("Travel Distance : "+distance);
                                JSONObject stepsObject;
                                for (int i=0;i<=(stepsArray.length())-1;i++)
                                {
                                    stepsObject=stepsArray.getJSONObject(i);
                                    String tempPolyline=stepsObject.getJSONObject("polyline")
                                            .getString("points");
                                    polylines.add(tempPolyline);
                                    weatherDataStore.setPolylines(tempPolyline);
                                    Log.d(TAG, "Striiing : " +tempPolyline);
                                    mapView.getMapAsync(SearchResultActivity.this);
                                }
                                Log.d(TAG, "polylines : " +polylines+"--**"+weatherDataStore.getPolylines());
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener(){
                        @Override
                        public void onErrorResponse(VolleyError error){
                            Toast.makeText(getApplicationContext(),"Error on parsing Polylines"+error,Toast.LENGTH_LONG).show();
                            error.printStackTrace();
                        }
                    }
            );

            // Add JsonObjectRequest to the RequestQueue
            requestQueue2.add(jsonObjectRequest);

            return polylines;
        }

        @Override
        protected void onPostExecute(ArrayList<String> s) {
            super.onPostExecute(s);
            Log.d(TAG, "Striiing : " + s.size());
            //progressBar.setVisibility(View.INVISIBLE);

        }
    }

    private void findCities(LatLng startLocation, LatLng endLocation) {
        //final WeatherDataStore weatherDataStore=new WeatherDataStore();
        weatherDataStore.clearLatLng();
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
                        Toast.makeText(getApplicationContext(),"Error on parsing location data from HEREMAPS"+error,Toast.LENGTH_LONG).show();
                        error.printStackTrace();
                    }
                }
        );

        // Add JsonObjectRequest to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }

    private void findCitiesUsingGMAPS(LatLng startLocation, LatLng endLocation){
        weatherDataStore.clearLatLng();
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
                        Toast.makeText(getApplicationContext(),"Error on parsing Location Data from GOOGLE MAPS"+error,Toast.LENGTH_LONG).show();
                        error.printStackTrace();
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
            temperatureList=new ArrayList<>();
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
                                Double temperature=Double.parseDouble(jsonResponse.getString("temperature"));
                                Double celsius=((temperature - 32)*5)/9;
                                Log.d(TAG, "summary : " + summary +"| prediction: "+prediction);
                                summaryList.add(summary);
                                predictionList.add(prediction);
                                temperatureList.add(celsius);
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
                            Toast.makeText(getApplicationContext(),"Error on parsing Weather " +
                                    "Data"+error,Toast.LENGTH_LONG).show();
                            error.printStackTrace();
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
        ArrayList<String> finalCityNames=new ArrayList<>();
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

        /*for (int i=0;i<cityNames.size();i++){
            for (int j=0;j<cityNames.size();j++){
                if (cityNames.get(i)==cityNames.get(j)){
                    cityNames.remove(j);
                }
            }
        }*/

        List<String> details=new ArrayList<>();
        details.add("Details Test");

        List<String> details2=new ArrayList<>();
        details2.add("Details Test 2");
        details2.add("Details Test  2 22");
        Cartesian cartesian = AnyChart.column();
        List<DataEntry> data = new ArrayList<>();
        for (int i = 0; i < summaryList.size(); i++) {
            List<String> temp = new ArrayList<>();
            String tempCity=cityNames.get(i);
            if ((!tempCity.matches("[0-9]+")) & tempCity.length()>3)
            {
                filteredCityNames.add(cityNames.get(i));
                temp.add("Weather Summary \t:  " + summaryList.get(i));
                temp.add("Prediction \t\t\t\t:  " + predictionList.get(i));
                temp.add("Temperature \t\t\t:  " + temperatureList.get(i));
                data.add(new ValueDataEntry(cityNames.get(i), temperatureList.get(i)));
                String cityname=(i+1)+")."+cityNames.get(i);
                listHashMap.put(cityNames.get(i), temp);
                Log.d(TAG, "Hashmap " + temp);
                Log.d(TAG, "cityNumber " + cityNames.get(i));
            }
            else {

            }
            progressBar.setVisibility(View.INVISIBLE);
        }

        Column column = cartesian.column(data);
        column.tooltip()
                .titleFormat("{%X}")
                .position(Position.CENTER_BOTTOM)
                .offsetX(0d)
                .offsetY(5d)
                .format("{%Value}{groupsSeparator: }");
        cartesian.animation(true);
        cartesian.title("Weather Details by City ID");
        cartesian.yScale().minimum(0d);

        cartesian.yAxis(0).labels().format("{%Value}{groupsSeparator: }");

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        cartesian.interactivity().hoverMode(HoverMode.BY_X);

        cartesian.xAxis(0).title("City/Road (ID)");
        cartesian.yAxis(0).title("Temperature (Celsius) ");

        anyChartView.setChart(cartesian);
        expandableListAdapter=new ExpandableListAdapter(this,filteredCityNames,listHashMap);
        expandableListView.setAdapter(expandableListAdapter);
    }
}
