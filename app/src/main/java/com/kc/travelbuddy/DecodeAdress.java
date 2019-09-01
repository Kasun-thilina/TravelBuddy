package com.kc.travelbuddy;

import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DecodeAdress {
    private String localityString ;
    private String cityName  ;
    private String subLocality ;
    private String country ;
    private String region_code ;
    private String zipcode;
    private String state ;

    public DecodeAdress(){

    }

    /**Finding the address from the values taken from location*/
    public DecodeAdress(double latitude,double longitude,Geocoder geocoder) {
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude,longitude,1);
            if (geocoder.isPresent()) {
                StringBuilder stringBuilder = new StringBuilder();
                if (addresses.size()>0) {
                    Address returnAddress = addresses.get(0);
                    localityString = returnAddress.getLocality();
                    cityName = returnAddress.getFeatureName();
                    subLocality = returnAddress.getSubLocality();
                    country = returnAddress.getCountryName();
                    region_code = returnAddress.getCountryCode();
                    zipcode = returnAddress.getPostalCode();
                    state = returnAddress.getAdminArea();
                }
            } else {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getDecodedCityName(double latitude,double longitude,Geocoder geocoder) {
        String decodedCityName="";
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(latitude,longitude,1);
            if (geocoder.isPresent()) {
                StringBuilder stringBuilder = new StringBuilder();
                if (addresses.size()>0) {
                    Address returnAddress = addresses.get(0);
                    decodedCityName = returnAddress.getFeatureName();
                    String tempdecodedCityName = returnAddress.getSubLocality();
                    String locality = returnAddress.getLocality();
                    Log.d("class","locality"+locality+"  ##"+tempdecodedCityName);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return decodedCityName;
    }

    public String getLocalityString() {
        return localityString;
    }

    public String getCityName() {
        return cityName;
    }

    public String getSubLocality() {
        return subLocality;
    }

    public String getCountry() {
        return country;
    }

    public String getRegion_code() {
        return region_code;
    }

    public String getZipcode() {
        return zipcode;
    }

    public String getState() {
        return state;
    }
}
