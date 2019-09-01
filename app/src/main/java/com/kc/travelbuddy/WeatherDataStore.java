package com.kc.travelbuddy;

import com.google.android.gms.maps.model.LatLng;

import java.sql.Array;
import java.util.ArrayList;

public class WeatherDataStore {
    private ArrayList<Double> latitudeList;
    private ArrayList<Double> longitudeList;
    private ArrayList<Double> cityNames;
    private ArrayList<String> polylines;
    private LatLng startLocation;
    private LatLng endLocation;


    public WeatherDataStore(){
        latitudeList=new ArrayList<>();
        longitudeList=new ArrayList<>();
        cityNames=new ArrayList<>();
        polylines=new ArrayList<>();
    }
    public void setStartLocation(LatLng startLocation) {
        this.startLocation = startLocation;
    }

    public void setEndLocation(LatLng endLocation) {
        this.endLocation = endLocation;
    }

    /**
     *
     * To clear up arrays for a second run
     */

    public  void clearLatLng(){
        if (!latitudeList.isEmpty()) {
            this.latitudeList.clear();
        }
        if (!longitudeList.isEmpty()) {
            this.longitudeList.clear();
        }
    }
    public void clearPolylines(){
        if (!polylines.isEmpty()) {
            this.polylines.clear();
        }
    }

    /**
     *
     * Setters
     */
    public void setPolylines(String polylines) {
        this.polylines.add(polylines);
    }
    public void setLatitudeList(Double latitude) {
        this.latitudeList.add(latitude);
    }
    public void setLongitudeList(Double longitude) {
        this.longitudeList.add(longitude);
    }
    public void setCityNames(ArrayList<Double> cityNames) {
        this.cityNames = cityNames;
    }


    /**
     *
     * Getters
     */
    public ArrayList<String> getPolylines() {
        return polylines;
    }
    public ArrayList<Double> getLatitudeList() {
        return latitudeList;
    }
    public ArrayList<Double> getLongitudeList() {
        return longitudeList;
    }
    public ArrayList<Double> getCityNames() {
        return cityNames;
    }

    public LatLng getEndLocation() {
        return endLocation;
    }
    public LatLng getStartLocation() {
        return startLocation;
    }

}
