package com.kc.travelbuddy;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class WeatherMapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MapView mapView;
    private SharedPreferences sharedPreferences;
    private RequestQueue requestQueue ;
    private String ROUTEURL,WEATHERURL;

    private static final String TAG = "WeatherMapFragment";
    final String APIKEY="AIzaSyAvj6UR1FtWxJwjBiKOyrvdBPK5tnFFuDs";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View root;
        root = inflater.inflate(R.layout.activity_weather_map, container, false);
        // Gets the MapView from the XML layout and creates it
        mapView = root.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        // Gets to GoogleMap from the MapView and does initialization stuff



        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls

        return root;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        final ArrayList<String> polylines=new ArrayList<>();
        Double startLocationLat=Double.parseDouble(sharedPreferences.getString("startLocationLat", "0"));
        Double startLocationLng=Double.parseDouble(sharedPreferences.getString("startLocationLng", "0"));
        Double endLocationLat=Double.parseDouble(sharedPreferences.getString("endLocationLat", "0"));
        Double endLocationLng=Double.parseDouble(sharedPreferences.getString("endLocationLng", "0"));
        WeatherDataStore weatherDataStore=new WeatherDataStore();
        ROUTEURL="https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" +startLocationLat+","+startLocationLng+
                "&destination="+endLocationLat+","+endLocationLng+
                "&key="+APIKEY;
        requestQueue= Volley.newRequestQueue(getActivity());
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
                            JSONObject stepsObject;
                            for (int i=0;i<stepsArray.length();i++)
                            {
                                stepsObject=stepsArray.getJSONObject(i);
                                String tempPolyline=stepsObject.getJSONObject("polyline")
                                        .getString("points");
                                //weatherDataStore.setPolylines(tempPolyline);
                                polylines.add(tempPolyline);
                                Log.d(TAG, "polylines : " + tempPolyline);
                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Toast.makeText(getActivity().getApplicationContext(),"Error on parsing Polylines"+error,Toast.LENGTH_LONG).show();
                        error.printStackTrace();
                    }
                }
        );

        // Add JsonObjectRequest to the RequestQueue
        requestQueue.add(jsonObjectRequest);
        for (int i=0;i<polylines.size();i++) {
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.width(10);
            polylineOptions.addAll(PolyUtil.decode(polylines.get(i)));
            mMap.addPolyline(polylineOptions);
        }

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(7.604699500000001, 80.0726308);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Rain"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }


}
