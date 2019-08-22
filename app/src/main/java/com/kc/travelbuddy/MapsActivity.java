package com.kc.travelbuddy;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private static final String TAG = "MapsActivity";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final int AUTOCOMPLETE_REQUEST_CODE_FOR_START=500;
    private static final int AUTOCOMPLETE_REQUEST_CODE_FOR_END=700;
    private Location location;
    Boolean locationFound=false;
    String apiKey ;
    View mapView;
    PlacesClient placesClient;
    EditText etTravelDate,etPickup,etDrop,etTravelTime;
    Button btnSearch;
    AutocompleteSupportFragment autocompleteSupportFragment;
    LatLng startLocation,endLocation;
    Calendar travelTime,travelDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        etPickup=findViewById(R.id.etPickup_location);
        etDrop=findViewById(R.id.etDrop_location);
        etTravelTime=findViewById(R.id.etTravel_Time);
        btnSearch=findViewById(R.id.btnSearch);
        requestPermissions();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();
        initDatePicker();
        initTimePicker();

        // Google api client
        googleApiClient = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).build();
        apiKey = getString(R.string.api_key);
        //Places API Client
        initPlacesAPI();

        initSearch();
    }

    private void initSearch() {
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(startLocation==null)
                {
                    Toast.makeText(MapsActivity.this, "Please Input Start Location to Continue", Toast.LENGTH_LONG).show();
                }
                else if(endLocation==null)
                {
                    Toast.makeText(MapsActivity.this, "Please Input End Location to Continue", Toast.LENGTH_LONG).show();
                }
                else if(travelDate==null)
                {
                    Toast.makeText(MapsActivity.this, "Please Input Travel Date to Continue", Toast.LENGTH_LONG).show();
                }
                else if(travelTime==null)
                {
                    Toast.makeText(MapsActivity.this, "Please Input Travel Time to Continue", Toast.LENGTH_LONG).show();
                }
                else {
                    final Intent searchActivityIntent=new Intent(MapsActivity.this,SearchActivity.class);
                    Bundle values = new Bundle();
                    values.putParcelable("startLocation", startLocation);
                    values.putParcelable("endLocation", endLocation);
                    searchActivityIntent.putExtra("locations",values);
                    searchActivityIntent.putExtra("travelDate",travelDate.toString());
                    searchActivityIntent.putExtra("travelTime",travelTime.toString());
                    startActivity(searchActivityIntent);
                }
            }
        });
    }

    private void initPlacesAPI() {
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }
        etPickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSearchCalled(AUTOCOMPLETE_REQUEST_CODE_FOR_START);
            }
        });
        etDrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSearchCalled(AUTOCOMPLETE_REQUEST_CODE_FOR_END);
            }
        });
    }

    private void onSearchCalled(int whatClicked) {
        // Set the fields to specify which types of place data to return.
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields).setCountry("LK")
                .build(this);
        if (whatClicked==AUTOCOMPLETE_REQUEST_CODE_FOR_START) {
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE_FOR_START);
        }
        else if(whatClicked==AUTOCOMPLETE_REQUEST_CODE_FOR_END){
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE_FOR_END);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE_FOR_START) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId() + ", " + place.getAddress());
                //Toast.makeText(this, "ID: " + place.getId() + "address:" + place.getAddress() + "Name:" + place.getName() + " latlong: " + place.getLatLng(), Toast.LENGTH_LONG).show();
                String address = place.getAddress();
                // do query with address
                etPickup.setText(address);

                /**
                 * Decode LatLng
                 */
                Geocoder geocoder = new Geocoder(this,Locale.getDefault());
                List<Address> decodedAddress;

                try {
                    decodedAddress = geocoder.getFromLocationName(address, 1);
                    if (!(decodedAddress == null)) {
                        Address location = decodedAddress.get(0);
                        double lat = location.getLatitude();
                        double lng = location.getLongitude();
                        startLocation=new LatLng(lat,lng);
                    }
                    else {

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Toast.makeText(this, "Error: " + status.getStatusMessage(), Toast.LENGTH_LONG).show();
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
                Toast.makeText(this, "Search Cancelled: ", Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == AUTOCOMPLETE_REQUEST_CODE_FOR_END)
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId() + ", " + place.getAddress());
                //Toast.makeText(this, "ID: " + place.getId() + "address:" + place.getAddress() + "Name:" + place.getName() + " latlong: " + place.getLatLng(), Toast.LENGTH_LONG).show();
                String address = place.getAddress();
                // do query with address
                etDrop.setText(address);

                /**
                 * Decode LatLng
                 */
                Geocoder geocoder = new Geocoder(this,Locale.getDefault());
                List<Address> decodedAddress;

                try {
                    decodedAddress = geocoder.getFromLocationName(address, 1);
                    if (!(decodedAddress == null)) {
                        Address location = decodedAddress.get(0);
                        double lat = location.getLatitude();
                        double lng = location.getLongitude();
                        endLocation=new LatLng(lat,lng);
                    }
                    else {

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                //Toast.makeText(this, "Error: " + status.getStatusMessage(), Toast.LENGTH_LONG).show();
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
                Toast.makeText(this, "Search Cancelled: ", Toast.LENGTH_LONG).show();
            }




    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
            return;
        }
        mMap.setMyLocationEnabled(true);
        if (mapView != null &&
                mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 800, 80);

        }

       /* // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
    }
    /**
     * Date Picker
     * */
    private void initDatePicker() {
        final Calendar myCalendar = Calendar.getInstance();

        etTravelDate = findViewById(R.id.etTravel_date);
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel(myCalendar);
            }

        };

        etTravelDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(MapsActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }
    private void updateLabel(Calendar myCalender) {
        travelDate=myCalender;
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        etTravelDate.setText(sdf.format(myCalender.getTime()));
    }
    /**
     * Time Picker
     * */
    private void initTimePicker() {
        etTravelTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(MapsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        String am_pm = "";

                        travelTime = Calendar.getInstance();
                        travelTime.set(Calendar.HOUR_OF_DAY, selectedHour);
                        travelTime.set(Calendar.MINUTE, selectedMinute);

                        if (travelTime.get(Calendar.AM_PM) == Calendar.AM)
                            am_pm = "AM";
                        else if (travelTime.get(Calendar.AM_PM) == Calendar.PM)
                            am_pm = "PM";

                        String strHrsToShow = (travelTime.get(Calendar.HOUR) == 0) ?"12":travelTime.get(Calendar.HOUR)+"";
                        etTravelTime.setText( strHrsToShow+":"+travelTime.get(Calendar.MINUTE)+" "+am_pm);
                    }
                }, hour, minute, false);//Yes 24 hour time
                mTimePicker.setTitle("Select Travelling Time");
                mTimePicker.show();
            }
        });

    }



    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        requestPermissions();
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!checkPlayServices()) {
            Toast.makeText(this, "You need to install Google Play Services to use the App properly", Toast.LENGTH_LONG).show();
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            } else {
                finish();
            }

            return false;
        }

        return true;
    }


    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            // locationTv.setText("Latitude : " + location.getLatitude() + "\nLongitude : " + location.getLongitude());
            //double latitude=location.getLatitude();
            Log.d(TAG, "Latitude : " + location.getLatitude() + "Longitude : " + location.getLongitude());
            findCity(location.getLatitude(),location.getLongitude());
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            startLocation=latLng;
            CameraUpdate cameraUpdate= CameraUpdateFactory.newLatLngZoom(latLng, 10);
            mMap.animateCamera(cameraUpdate);
            //locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
            return;
        }
        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (location != null) {
            Log.d(TAG, "Latitude2 : " + location.getLatitude() + "Longitude2 : " + location.getLongitude());
            findCity(location.getLatitude(),location.getLongitude());
        }

        startLocationUpdates();
    }
    private void startLocationUpdates() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(5000);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&  ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "You need to enable permissions to display location !", Toast.LENGTH_SHORT).show();
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    /**Finding the address from the values taken from location*/
    private void findCity(double latitude,double longitude) {
        Geocoder geocoder = new Geocoder(this,Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude,longitude,1);
            if (geocoder.isPresent()) {
                StringBuilder stringBuilder = new StringBuilder();
                if (addresses.size()>0) {
                    Address returnAddress = addresses.get(0);

                    String localityString = returnAddress.getLocality();
                    String name = returnAddress.getFeatureName();
                    String subLocality = returnAddress.getSubLocality();
                    String country = returnAddress.getCountryName();
                    String region_code = returnAddress.getCountryCode();
                    String zipcode = returnAddress.getPostalCode();
                    String state = returnAddress.getAdminArea();
                    Log.d(TAG, "name : "+name);
                    Log.d(TAG, "localityString : "+localityString);
                    Log.d(TAG, "subLocality : "+subLocality);
                    Log.d(TAG, "country : "+country);
                    Log.d(TAG, "region_code : "+region_code);
                    Log.d(TAG, "zipcode : "+zipcode);
                    Log.d(TAG, "state : "+state);
                    if(name!=null && (!name.matches("[0-9]+")))
                    {
                        etPickup.setText(name+","+subLocality);
                    }
                    else {
                        if (subLocality!=null){
                            etPickup.setText(subLocality);
                        }
                    }

                }
            } else {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
