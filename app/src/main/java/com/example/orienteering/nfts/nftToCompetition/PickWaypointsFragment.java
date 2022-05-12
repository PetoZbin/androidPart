package com.example.orienteering.nfts.nftToCompetition;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.location.Address;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.orienteering.R;
import com.example.orienteering.databinding.FragmentPickWaypointsBinding;
import com.example.orienteering.retrofit.responseClasses.tokens.AddressNftsData;
import com.example.orienteering.userAccess.accountPicker.AccountPickerViewModel;
import com.google.android.gms.dynamic.IFragmentWrapper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.internal.operators.maybe.MaybeError;
import jnr.ffi.Struct;


public class PickWaypointsFragment extends com.example.orienteering.tracking.BasicLocationFragment
        implements View.OnClickListener, GoogleMap.OnMarkerClickListener, OnMapReadyCallback,
        PickWaypointOptionsDialog.OnDeleteListener, SearchView.OnQueryTextListener {

    // Vypracovane na zaklade gps kurzu: https://www.youtube.com/watch?v=_xUcYfbtfsI

    public static final String SERIALIZED_WAYPOINTS = "serializedWaypoints";
    public static final String SERIALIZED_NFT = "serializedNft";

    private String serializedNft;

    private GoogleMap mapInstance;
    private SearchView searchView;
    private LatLng lastLocation;

    private ImageButton proceedBtn;
    private FragmentPickWaypointsBinding waypointsBinding;
    private PickWaypointsViewModel waypointsViewModel;

    private SupportMapFragment mapFragment;

    private List<Marker> customMarkers = new ArrayList<>();

    public PickWaypointsFragment() {
        // Required empty public constructor
    }


    public static PickWaypointsFragment newInstance(String param1, String param2) {
        PickWaypointsFragment fragment = new PickWaypointsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((getArguments() != null) && getArguments().containsKey(SERIALIZED_NFT)) {
            serializedNft = getArguments().getString(SERIALIZED_NFT);
        }

        initTracking(); // inicializacia pociatocnej polohy zariadenia
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        waypointsBinding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.fragment_pick_waypoints, container, false);
        waypointsBinding.setLifecycleOwner(this);
        waypointsViewModel = new ViewModelProvider(this).get(PickWaypointsViewModel.class);
        waypointsBinding.setPickWaypointsViewModel(waypointsViewModel);

        //zdroj: kurz - https://www.youtube.com/watch?v=YCFPClPjDIQ

        // inicializujem google map fragment

        initMap();


        proceedBtn = waypointsBinding.wayPickerContinue;
        proceedBtn.setOnClickListener(this);
        proceedBtn.setEnabled(false);   // nema vybraty waypoint - nemoze este pokracovat
        proceedBtn.setImageAlpha(0x3F);

        waypointsBinding.floatingDelWaypointsBtn.setOnClickListener(this);

        searchView = waypointsBinding.municipalitySearch;
        searchView.setOnQueryTextListener(this);

        // updateLocation();   // nie je to tracking - len ukaze uzivatelovu polohu pri zapnuti tohto fragmentu
        waypointsBinding.wayPickerBackBtn.setOnClickListener(this);

        waypointsViewModel.getWayPoints().observe(getViewLifecycleOwner(), new Observer<List<LatLng>>() {
            @Override
            public void onChanged(List<LatLng> waypoints) {

                if (waypoints.size()<1){
                    waypointsBinding.floatingDelWaypointsBtn.setVisibility(View.GONE);
                }
                else {
                    waypointsBinding.floatingDelWaypointsBtn.setVisibility(View.VISIBLE);
                }

                if (waypoints.size() < 2){  //na pretek treba aspon start a ciel

                    proceedBtn.setEnabled(false);
                    proceedBtn.setImageAlpha(0x3F);
                }
                else {

                    proceedBtn.setEnabled(true);
                    proceedBtn.setImageAlpha(0xFF);
                }
            }
        });

        return waypointsBinding.getRoot();
    }


    public void initMap() {


        if (mapFragment == null) {

            mapFragment = SupportMapFragment.newInstance();

            try {
                mapFragment.getMapAsync(this);
            } catch (NullPointerException ex) {

                Toast.makeText(getContext(), getString(R.string.gps_map_loading_err), Toast.LENGTH_SHORT).show();
                Log.e("getMap async", getString(R.string.gps_map_loading_err));
            }
        }

        getChildFragmentManager().beginTransaction().replace(R.id.way_picker_google_map, mapFragment).commit();
    }


    @Override
    public void onLocationProvided(Location location) {    //poloha uspesne najdena - aktualizuj polia na zaklade nej


        if ((location != null) && (mapInstance != null)) {

            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            Log.d("lat: ", String.valueOf(latitude));
            Log.d("lng: ", String.valueOf(longitude));
            Log.d("accuracy: ", String.valueOf(location.getAccuracy()));

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//            MarkerOptions markerOptions = new MarkerOptions();
//            markerOptions.position(latLng);
//            markerOptions.title("Your location");

            //   mapInstance.addMarker(markerOptions);

            mapInstance.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));

//            if (location.hasSpeed()){
//                //ukaz rychlost - tuna sa nebude pouzivat ale v sutazi ano
//            }
        }
    }


    private void addWaypoint(LatLng latLng) {

        int color = getContext().getColor(R.color.alert_red);
        String number = "";

        if (waypointsViewModel.addWaypoint(latLng)) {

            if (waypointsViewModel.getWaypoints().size() == 1) {

                //prvy waypoint - cierny
                color = getContext().getColor(R.color.black);
                number = String.valueOf(waypointsViewModel.getWaypoints().size());
            } else {

                color = getContext().getColor(R.color.purple_700);
                number = String.valueOf(waypointsViewModel.getWaypoints().size());
            }

            drawWaypoint(latLng, color, number);
        }
    }

    private void drawWaypoint(LatLng latLng, int color, String number) {


        Address address = addressByLocation(latLng.latitude, latLng.longitude);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(resolveThroughFare(address))
                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(color, number)));

        Marker marker = mapInstance.addMarker(markerOptions);
        marker.showInfoWindow();
        customMarkers.add(marker);

    }


    @Override
    public void onClick(View v) {

        if (v.equals(proceedBtn)) {

            //medzi fragmentami prenasam serializovane data
            String serializedWaypointsDesc = new Gson().toJson(createCustomVawpointList());

            Bundle bundle = new Bundle();
            bundle.putString(SERIALIZED_WAYPOINTS, serializedWaypointsDesc);
            bundle.putString(SERIALIZED_NFT, serializedNft);
            Navigation.findNavController(v).navigate(R.id.action_pickWaypointsFragment_to_compSummaryFragment, bundle);
        }
        else if(v.equals(waypointsBinding.wayPickerBackBtn)){
            try {
                getActivity().onBackPressed();
            }catch (NullPointerException ex){

                Log.e("Way picker back btn: ","Null ptr exception");
            }
        }
        else if (v.equals(waypointsBinding.floatingDelWaypointsBtn)){

            //zmaz vsetky markery
            removeCustomMarkers();
            waypointsViewModel.getWayPoints().postValue(new ArrayList<>());
        }
    }

    //vyskusat
    @Override
    public void onDestroyView() {
        super.onDestroyView();
       // getChildFragmentManager().beginTransaction().remove(mapFragment).commit();
    }


    //tvorba custom markera
    // kod ispirovany podla zdroja: https://stackoverflow.com/a/35800880
    private Bitmap getMarkerBitmapFromView(int color, String number) {      //@DrawableRes int resId

        View customMarkerView = getLayoutInflater().inflate(R.layout.custom_marker_view, null);

        TextView customText = customMarkerView.findViewById(R.id.marker_number);

        customText.getBackground().setTint(color);
        customText.setText(number);
        //((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))

        //  ImageView markerImageView = (ImageView) customMarkerView.findViewById(R.id.profile_image);
        //  markerImageView.setImageResource(resId);
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mapInstance = googleMap;
        updateLocation();   // ak sa mapa inicializuje, najde uzivatelovu polohu

        //googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

        mapInstance.setOnMarkerClickListener(this);
        mapInstance.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng latLng) {
                addWaypoint(latLng);
            }
        });
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {

        LatLng latLng = marker.getPosition();

        //Toast.makeText(getContext(), String.valueOf(latLng.latitude), Toast.LENGTH_LONG).show();

        Address address = addressByLocation(latLng.latitude, latLng.longitude);

        CustomWaypointDesc waypointDesc = new CustomWaypointDesc();
        waypointDesc.setLat(latLng.latitude);
        waypointDesc.setLng(latLng.longitude);
        waypointDesc.setSeqNumber((waypointsViewModel.getWayPointIndex(latLng) + 1));
        waypointDesc.setThoroughfare(resolveThroughFare(address));

        PickWaypointOptionsDialog dialog = new PickWaypointOptionsDialog(waypointDesc);
        dialog.setTargetFragment(this, 0);
        dialog.setDeleteListener(this);
        dialog.show(getFragmentManager().beginTransaction(), "wayopt");

        return false;
    }

    @Override
    public void onWayPointDelete(double latitude, double longitude) {
        //remove from viewmodel

        if (waypointsViewModel.removeWaypoint(new LatLng(latitude, longitude))){

            // zmazane - prekresli markery
            removeCustomMarkers();
            redrawWaypoints();
        }
    }

    private void removeCustomMarkers(){

        for(Marker item : customMarkers){

            item.remove();
        }
        customMarkers = new ArrayList<>();
    }

    private void redrawWaypoints(){

        for (int i=0; i<waypointsViewModel.getWaypoints().size(); i++){

            if (i==0){
                // cierny - startovny
                drawWaypoint(waypointsViewModel.getWaypoints().get(i), getContext().getColor(R.color.black), String.valueOf(i+1));
            }
            else {

                drawWaypoint(waypointsViewModel.getWaypoints().get(i), getContext().getColor(R.color.purple_700), String.valueOf(i+1));
            }
        }
    }

    private List<CustomWaypointDesc> createCustomVawpointList(){

        List<CustomWaypointDesc> waypoints = new ArrayList<>();

        for (int i=0; i< waypointsViewModel.getWaypoints().size(); i++){

            LatLng waypoint = waypointsViewModel.getWaypoints().get(i);

            Address waypointAddress = addressByLocation(waypoint.latitude, waypoint.longitude);

            CustomWaypointDesc waypointDesc = new CustomWaypointDesc();
            waypointDesc.setLat(waypoint.latitude);
            waypointDesc.setLng(waypoint.longitude);
            waypointDesc.setSeqNumber(i + 1);
            waypointDesc.setThoroughfare(resolveThroughFare(waypointAddress));
            waypointDesc.setMunicipality(resolveMunicipality(waypointAddress));



            waypoints.add(waypointDesc);
        }

        return waypoints;
    }

    private String resolveThroughFare(Address waypointAddress){

        if ((waypointAddress==null)){

            return getString(R.string.way_picker_location_undefined);
        }

        if ((waypointAddress.getThoroughfare() == null)){

            return getString(R.string.way_picker_location_undefined);
        }

        if ((waypointAddress.getThoroughfare().replaceAll(" ","").equals(""))){

            return getString(R.string.way_picker_location_undefined);
        }

        return waypointAddress.getThoroughfare();
    }

    private String resolveMunicipality(Address waypointAddress){

        if ((waypointAddress==null)){

            return getString(R.string.way_picker_location_undefined);
        }

        if ((waypointAddress.getLocality() != null) && (!waypointAddress.getLocality().equals(""))){
            return waypointAddress.getLocality();
        }
        else if ((waypointAddress.getSubAdminArea() != null) && (!waypointAddress.getSubAdminArea().equals(""))){
            return waypointAddress.getSubAdminArea();
        }
        else if ((waypointAddress.getAdminArea() != null) && (!waypointAddress.getAdminArea().equals(""))){
            return waypointAddress.getAdminArea();
        }
        else if ((waypointAddress.getCountryName() != null) && (!waypointAddress.getCountryName().equals(""))){
            return waypointAddress.getCountryName();
        }
        else {

            return getString(R.string.way_picker_location_undefined);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        String locationName = searchView.getQuery().toString();

        Address address = addressByName(locationName);

        if (address !=null){
            //vycentruj mapu na adresu + natiahni sutaze v meste, okoli
            onLocationProvided(address.getLatitude(), address.getLongitude());
        }
        else {
            Toast.makeText(getContext(),R.string.gps_geocoder_address_err, Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public void onLocationProvided(double latitude, double longitude) {

        centerCamera(new LatLng(latitude,longitude));
    }

    private void centerCamera(LatLng coords){
        mapInstance.animateCamera(CameraUpdateFactory.newLatLngZoom(coords, 15f));
    }
}