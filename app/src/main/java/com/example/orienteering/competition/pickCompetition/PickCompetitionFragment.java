package com.example.orienteering.competition.pickCompetition;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.orienteering.R;
import com.example.orienteering.databinding.FragmentPickCompetitionBinding;
import com.example.orienteering.databinding.FragmentPickWaypointsBinding;
import com.example.orienteering.nfts.nftToCompetition.CustomWaypointDesc;
import com.example.orienteering.nfts.nftToCompetition.PickWaypointsViewModel;
import com.example.orienteering.retrofit.responseClasses.competition.CompetitionOverallData;
import com.example.orienteering.retrofit.responseClasses.competition.CompetitionOverallResponse;
import com.example.orienteering.tracking.BasicLocationFragment;
import com.example.orienteering.userAccess.onboarding.responses.LoginSuccessResponse;
import com.google.android.gms.dynamic.IFragmentWrapper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class PickCompetitionFragment extends BasicLocationFragment
        implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, SearchView.OnQueryTextListener, View.OnClickListener {

    //zdroj - na zaklade kurzu: https://www.youtube.com/watch?v=iWYsBDCGhGw

    private GoogleMap mapInstance;
    private SupportMapFragment mapFragment;
    private SearchView searchView;
    private Marker userMarker;
    private List<Marker> competitionMarkers = new ArrayList<>();

    public static final String SERIALIZED_COMPETITION = "serializedCompetition";

    private FragmentPickCompetitionBinding pickCompetitionBinding;
    private PickCompetitionViewmodel pickCompetitionViewmodel;

//geofencing
    public PickCompetitionFragment() {
        // Required empty public constructor
    }

    public static PickCompetitionFragment newInstance(String param1, String param2) {
        PickCompetitionFragment fragment = new PickCompetitionFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
        initTracking();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        pickCompetitionBinding = DataBindingUtil.inflate(getLayoutInflater(),
                R.layout.fragment_pick_competition, container, false);
        pickCompetitionBinding.setLifecycleOwner(this);
        pickCompetitionViewmodel = new ViewModelProvider(this).get(PickCompetitionViewmodel.class);
        pickCompetitionBinding.setPickCompetitionViewmodel(pickCompetitionViewmodel);

        searchView = pickCompetitionBinding.municipalitySearch;
        searchView.setOnQueryTextListener(this);

        pickCompetitionBinding.floatingCenterButton.setOnClickListener(this);

        pickCompetitionViewmodel.getUserId().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String userId) {
                // mam id uzivatela, inicializuj mapu s waypointami
                // inicializuj az ked z db nacita aktualny ucet - ak uzivatel je ucastnikom  farebne sa rozlisi waypoint sutaze
                initMap();
            }
        });

        pickCompetitionViewmodel.getCompetitionsResponse().observe(getViewLifecycleOwner(), new Observer<CompetitionOverallResponse>() {
            @Override
            public void onChanged(CompetitionOverallResponse response) {
                //nacitali sa sutaze pre danu lokalitu
                try {
                    onLocalCompetitionsFetched(response);
                }catch (NullPointerException  ex){
                    Log.e("Competition by locality changed: ", "Null pointer exception in markers");
                }
            }
        });

        return pickCompetitionBinding.getRoot();
    }

    @Override
    public void onLocationProvided(double lat, double lng) {

        LatLng latLng = new LatLng(lat, lng);

        if ((latLng != null) && (mapInstance != null)) {

            Log.d("lat: ", String.valueOf(lat));
            Log.d("lng: ", String.valueOf(lng));
           // Log.d("accuracy: ", String.valueOf(location.getAccuracy()));

            mapInstance.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));

            //TODO: geocoder - zisti mesto, skus dotiahnut preteky

            Address addr = addressByLocation(lat, lng);
            Set<String>locations = prepareLocations(addressByLocation(lat, lng));

            if (locations.isEmpty()){

                Toast.makeText(getContext(),R.string.gps_geocoder_address_err, Toast.LENGTH_SHORT).show();
            }
            else {

                pickCompetitionViewmodel.getEventsByMunicipality(locations);
                pickCompetitionViewmodel.setLastSearch(addr);
            }
        }
    }

    @Override
    public void onLocationProvided(Location location) {

        if ((mapInstance!=null) && (location != null) && (getContext() != null)){

            if (userMarker != null){
                userMarker.remove();
            }
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(location.getLatitude(),location.getLongitude()));
            markerOptions.title(getContext().getString(R.string.gps_your_pos));
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

            userMarker = mapInstance.addMarker(markerOptions);
            userMarker.showInfoWindow();

            onLocationProvided(location.getLatitude(), location.getLongitude());
        }
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

        getChildFragmentManager().beginTransaction().replace(R.id.comp_picker_google_map, mapFragment).commit();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mapInstance = googleMap;
        if (getContext()!= null){
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style_basic));
        }
        updateLocation();   // ak sa mapa inicializuje, najde uzivatelovu polohu

        //googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

        mapInstance.setOnMarkerClickListener(this);
        mapInstance.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng latLng) {
                Toast.makeText(getContext(),"Lat:" + latLng.latitude + " Lng: " + latLng.longitude, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {

        //z listu najdi prisluchajucu sutaz, z kt je marker
        CompetitionOverallData competition =
                pickCompetitionViewmodel.getCompetitionsByMarkerCoords(marker.getPosition().latitude, marker.getPosition().longitude);
        if ((competition == null) || (getView() == null)){
            return false;
        }

        //odosli ju do preview fragmentu v serializovanom formate

        String serializedCompetition = new Gson().toJson(competition);

        //prepni na preview fragment
        Bundle bundle = new Bundle();
        bundle.putString(SERIALIZED_COMPETITION, serializedCompetition);
        Navigation.findNavController(getView()).navigate(R.id.action_wrappingFragment2_to_compPreviewFragment, bundle);
        return true;
    }

    @Override
    public void onClick(View v) {

        if (v.equals(pickCompetitionBinding.floatingCenterButton)){
            initTracking();
            updateLocation();
        }


    }

    //na zaklade kurzu: https://www.youtube.com/watch?v=iWYsBDCGhGw

    @Override
    public boolean onQueryTextSubmit(String query) {

        String locationName = searchView.getQuery().toString();

        Address address = addressByName(locationName);

        if (address !=null){
            //vycentruj mapu na adresu + natiahni sutaze v meste, okoli
            pickCompetitionViewmodel.setLastSearch(address);
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

    // z databazy najde waypointy ktore zodpovedaju danemu mestu
    private void fetchMunicipalityWaypoints(String municipality){
        //zavolaj funckiu viewmodelu
    }

    private Set<String> prepareLocations(Address address){

        Set<String> locations = new HashSet<>();

        //defaultne zahrniem do query mesto a okres

        if (address == null){return locations;}

        if ((address.getLocality() != null) && (!address.getLocality().replaceAll(" ","").equals(""))){

            locations.add(address.getLocality());
        }

        if ((address.getSubAdminArea() != null) && (!address.getSubAdminArea().replaceAll(" ","").equals(""))){

            locations.add(address.getSubAdminArea());
        }
        else if((address.getAdminArea() != null) && (!address.getAdminArea().replaceAll(" ","").equals(""))){
            //v tomto pripade nie je pritomny ani okres - zober kraj
            locations.add(address.getAdminArea());
        }

        return locations;
    }

    private void onLocalCompetitionsFetched(CompetitionOverallResponse competitions){

        CustomWaypointDesc customWaypoint = competitions.getData().get(0).getWaypointList().get(0);

        centerCamera(new LatLng(customWaypoint.getLat(), customWaypoint.getLng())); // vycentruj kameru na 1. zo sutazi

        for (CompetitionOverallData competition : competitions.getData()){

            customWaypoint = competition.getWaypointList().get(0);

            float markerColor = BitmapDescriptorFactory.HUE_ROSE;

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(customWaypoint.getLat(), customWaypoint.getLng()));
            markerOptions.title(customWaypoint.getThoroughfare());

            if (competition.getCompetitorsList().size() <= competition.getMaxCompetitors()){    //este sa mozno prihlasit
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                markerOptions.alpha(1f);
            }else { //sutaz plna - limit sutziacich naplneny - bleda cervena
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                markerOptions.alpha(0.35f);
            }

            Marker marker = mapInstance.addMarker(markerOptions);
            competitionMarkers.add(marker);
        }
    }

    private void centerCamera(LatLng coords){
        mapInstance.animateCamera(CameraUpdateFactory.newLatLngZoom(coords, 15f));
    }

    @Override
    public void onResume() {

        Address lastSearch = pickCompetitionViewmodel.getLastSearch();

        if ((mapInstance != null) && (competitionMarkers != null)){ // spustanie v navratoch na fragment

            mapInstance.clear();
            competitionMarkers.clear();
        }

        if(lastSearch != null){

            onLocationProvided(lastSearch.getLatitude(),lastSearch.getLongitude());
        }
        else{
            initTracking(); //znova lokalizuj podla potreby
            updateLocation();
        }

        super.onResume();
    }
}