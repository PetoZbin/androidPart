package com.example.orienteering.tracking;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.orienteering.R;
import com.google.android.gms.dynamic.IFragmentWrapper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.bouncycastle.util.Pack;

import java.io.IOException;
import java.util.List;

public abstract class BasicLocationFragment extends Fragment {

    private static final int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    public static final int DEFAULT_UPDATAE_INTERVAL = 2000;
    public static final int FASTEST_UPDATE_INTERVAL = 1000;
    public static final int APERMISSION_FINE_LOCATION = 99;    //unique request code - moze byt lub. cislo

    public FusedLocationProviderClient fusedLocationProviderClient;    // umoznuje viacere sposoby lokalizacie
    public LocationRequest locationRequest;    // konfiguracny subor pre lokaciu
    public LocationCallback locationCallback;

    public Geocoder geocoder;   // prelozenie polohy do adresy pribliznej


    public void initTracking() {

        if (getContext() != null){

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        }

        locationRequest = new LocationRequest();
        locationRequest.setInterval(DEFAULT_UPDATAE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);

        // LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY  - BTS a WIFI
        // PRIORITY_HIGH_ACCURACY  - GPS

        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);  // aky vykon vynalozi zariadenie

        geocoder = new Geocoder(getContext());
    }

    public void updateLocation() {

        if ((getActivity() != null) && (getContext() != null)) {

            //fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

            if (checkPermission()) {

                fusedLocationProviderClient.getLastLocation()
                        .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {

                                onLocationProvided(location); //vyuzi ziskanu lokaciu
                            }
                        });
            } else {

                askPermission();

//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//
//
//                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                            APERMISSION_FINE_LOCATION);
//                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case FINE_LOCATION_ACCESS_REQUEST_CODE:

                if ((grantResults.length > 0) &&  (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                    updateLocation();   // aktualizuj polohu
                } else {

//                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                            APERMISSION_FINE_LOCATION);
                    showAlertDialog();
                    Toast.makeText(getContext(), getString(R.string.gps_without_permission), Toast.LENGTH_LONG).show();
                }
                break;
        }

    }

    public void setupLocationCallback() {

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                onLocationProvided(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());   //zareaguje na novu aktualnu lokaciu
            }
        };
    }

    public void stopTracking() { // prestan ukladat lokaciu

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    public void startTracking() {


        if (checkPermission()){

            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
            updateLocation();
        }else{

            askPermission();
        }
    }

    private Boolean checkPermission(){

        if ((getContext() != null) &&
                (ActivityCompat.checkSelfPermission(getContext(),        //mam povolenie k polohe zariadeina
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {

            return true;
        }

        return false;
    }

    private void askPermission(){

        if ((getContext() != null) && (getActivity() != null)){

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)){

                //dialog request
                showAlertDialog();
//                ActivityCompat.requestPermissions(getActivity(),
//                        new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            }else {

                ActivityCompat.requestPermissions(getActivity(),
                        new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            }

        }
    }

    public void showAlertDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setMessage(R.string.comp_picker_gps_denied);
                alertDialogBuilder.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                askPermission();
                            }
                        });

        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }


    // Geocoder sluzba - podla lokacie sa snazi najst adresu - vaypoint by mohol obsahovat adresu po kliku

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
