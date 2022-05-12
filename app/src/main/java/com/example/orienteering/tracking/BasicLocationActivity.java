package com.example.orienteering.tracking;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.orienteering.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;

public abstract class BasicLocationActivity extends FragmentActivity {

    public static final int DEFAULT_UPDATE_INTERVAL = 500;
    public static final int FASTEST_UPDATE_INTERVAL = 400;
    public static final int SMALLEST_DISPLACEMENT = 1;
    public static final int PERMISSION_FINE_LOCATION = 99;    //unique request code - moze byt lub. cislo

    public FusedLocationProviderClient fusedLocationProviderClient;    // umoznuje viacere sposoby lokalizacie
    public LocationRequest locationRequest;    // konfiguracny subor pre lokaciu
    public LocationCallback locationCallback;

    public Geocoder geocoder;   // prelozenie polohy do adresy pribliznej

    public Boolean trackingInitialised = false;

    public void initTracking() {


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(DEFAULT_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        locationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT);

        // LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY  - BTS a WIFI
        // PRIORITY_HIGH_ACCURACY  - GPS

        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);  // aky vykon vynalozi zariadenie
        geocoder = new Geocoder(this);
    }

    public void updateLocation() {

            //fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

            if (ActivityCompat.checkSelfPermission(this,        //mam povolenie k polohe zariadeina
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                fusedLocationProviderClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {

                                onLocationProvided(location); //vyuzi ziskanu lokaciu
                            }
                        });
            } else {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSION_FINE_LOCATION);
                }
            }
        }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case PERMISSION_FINE_LOCATION:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    updateLocation();   // aktualizuj polohu
                } else {

                    Toast.makeText(this, getString(R.string.gps_without_permission), Toast.LENGTH_LONG).show();
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSION_FINE_LOCATION);

                }
                break;
        }

    }

    public void setupLocationCallback() {

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onLocationProvided(locationResult.getLastLocation());
                //onLocationProvided(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());   //zareaguje na novu aktualnu lokaciu
            }
        };
    }

    public void stopTracking() { // prestan ukladat lokaciu

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        //trackingOn = false;
    }

    public void startTracking() {


        if (checkPermission()){

            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            trackingInitialised = true;
            //updateLocation();
        }

    }

    private Boolean checkPermission(){

        if ((ActivityCompat.checkSelfPermission(this,        //mam povolenie k polohe zariadeina
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {return true;}

        return false;
    }


    public Address addressByLocation(double latitude, double longitude){

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            return addresses.get(0);
        }catch (IOException ex){

            Log.e("Geocoder address exceptiom: ", getString(R.string.gps_geocoder_address_err));
            return null;    // adresa zlyhala
        }
    }

    public Address addressByName(String locationName){

        try {
            List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
            return addresses.get(0);
        }catch (IOException ex){

            Log.e("Geocoder address exceptiom: ", getString(R.string.gps_geocoder_address_err));
            return null;    // adresa zlyhala
        }
    }

    public abstract void onLocationProvided(double latitude, double longitude);

    public abstract void onLocationProvided(Location location);

//    {    //poloha uspesne najdena - aktualizuj polia na zaklade nej
//
//        double latitude = location.getLatitude();
//        double longitude = location.getLongitude();
//        float accuracy = location.getAccuracy();
//
//        Log.d("lat: ",String.valueOf(latitude));
//        Log.d("lng: ",String.valueOf(longitude));
//        Log.d("accuracy: ",String.valueOf(accuracy));
//
//        if (location.hasSpeed()){
//            //ukaz rychlost - tuna sa nebude pouzivat ale v sutazi ano
//        }
//
//
//    }

}
