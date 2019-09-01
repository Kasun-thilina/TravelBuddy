package com.kc.travelbuddy;

import java.util.ArrayList;

public class WeatherDataStore {
    private ArrayList<Double> latitudeList;
    private ArrayList<Double> longitudeList;
    private ArrayList<Double> cityNames;


    public WeatherDataStore(){
        latitudeList=new ArrayList<>();
        longitudeList=new ArrayList<>();
        cityNames=new ArrayList<>();
    }

    public  void clearArrayLists(){
        if (!latitudeList.isEmpty()) {
            this.latitudeList.clear();
        }
        if (!longitudeList.isEmpty()) {
            this.longitudeList.clear();
        }
    }

    public ArrayList<Double> getLatitudeList() {
        return latitudeList;
    }

    public void setLatitudeList(Double latitude) {
        this.latitudeList.add(latitude);
    }

    public ArrayList<Double> getLongitudeList() {
        return longitudeList;
    }

    public void setLongitudeList(Double longitude) {
        this.longitudeList.add(longitude);
    }

    public ArrayList<Double> getCityNames() {
        return cityNames;
    }

    public void setCityNames(ArrayList<Double> cityNames) {
        this.cityNames = cityNames;
    }
}
