package com.example.orienteering.competition.run;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.orienteering.R;
import com.example.orienteering.competition.CompetitionStates;
import com.example.orienteering.competition.CompetitorStates;
import com.example.orienteering.competition.competitionPreview.CompPreviewViewmodel;
import com.example.orienteering.competition.competitionPreview.otherPreviews.PrizePreviewDialogFragment;
import com.example.orienteering.competition.run.dialogues.RunFinishDialogueFragment;
import com.example.orienteering.competition.run.dialogues.RunQuitDialogFragment;
import com.example.orienteering.competition.run.geofence.GeofenceHelper;
import com.example.orienteering.competition.run.locationService.LocationService;
import com.example.orienteering.competition.run.locationService.LocationServiceStates;
import com.example.orienteering.databinding.ActivityRun2Binding;
import com.example.orienteering.dbWork.registration.UserCompetition;
import com.example.orienteering.nfts.nftToCompetition.CustomWaypointDesc;
import com.example.orienteering.retrofit.responseClasses.CommonResponse;
import com.example.orienteering.retrofit.responseClasses.competition.CompetitionOverallData;
import com.example.orienteering.tracking.BasicLocationActivity;
import com.google.android.gms.common.internal.service.Common;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

public class RunActivity extends BasicLocationActivity implements OnMapReadyCallback, View.OnClickListener,
        OnCompetitionLeftListener, PopupMenu.OnMenuItemClickListener {

    private Boolean isRegistered = false;

    //https://www.youtube.com/watch?v=lvcGh2ZgHeA
    //zdroj naucne video - youtube: https://www.youtube.com/watch?v=nmAtMqljH9M
    // https://www.youtube.com/watch?v=ycBVe3iYtqQ

    public static final int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    public static final int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;
    public static final String SERIALIZED_COMPETITION = "serializedCompetition";
    public static final float GEOFENCE_RADIUS = 10.0f; //priemer kruhu v ktorom je zaznamenavana poloha - tolerancia
    private String GEOFENCE_START_ID = "GEOFENCE_START_ID";
    private String GEOFENCE_WAYPOINT_ID = "GEOFENCE_WAYPOINT_ID";

    private ActivityRun2Binding runBinding;
    private RunActivityViewmodel runViewModel;

    private GoogleMap mapInstance;
    private GeofencingClient geofencingClient;
    private GeofenceHelper geofenceHelper;

    private LatLng currentLocation;
    private Marker currentPosMarker;

    private Circle circle;
    private CircleOptions circleOptions;
    private List<MarkerOptions> markerOptionsList = new ArrayList<>();
    private List<Marker> markers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        runBinding = DataBindingUtil.setContentView(this, R.layout.activity_run2);
        runBinding.setLifecycleOwner(this);
        runViewModel = new ViewModelProvider(this).get(RunActivityViewmodel.class);
        runBinding.setRunViewmodel(runViewModel);

        runBinding.floatingCenterButton.setOnClickListener(this);
        runBinding.floatingCenterButton.setVisibility(View.GONE);
        runBinding.runMainUpperMenuBtn.setOnClickListener(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.run_google_map);

        if (mapFragment != null) {

            mapFragment.getMapAsync(this);
        }


        // skontroluj, ci je uz potvrdeny start - ci uz server vie o sutaziacom

        runViewModel.checkIfStartConfirmed();


        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceHelper = new GeofenceHelper(this);

        runViewModel.getCompetition().observe(this, new Observer<CompetitionOverallData>() {
            @Override
            public void onChanged(CompetitionOverallData newCompetition) {

                //ked dotiahol sutaz - vykresli waypointy
                try {
                    onCompetitionFetched(newCompetition);
                }catch (NullPointerException ex){
                    Log.e("On competition fetch - waypoints: ","Nullptr exception");
                }
            }
        });


        //uzivatel odisiel zo sutaze - uz bol notifikovany aj server a aktualizovana interna db
        runViewModel.getLeftResponse().observe(this, new Observer<CommonResponse>() {
            @Override
            public void onChanged(CommonResponse res) {

              if (res.getSuccess()){
                  onLeftResponse();
              }
            }
        });


        runViewModel.getErrorResponse().observe(this, new Observer<CommonResponse>() {
            @Override
            public void onChanged(CommonResponse errResponse) {

                Toast.makeText(getApplication(), errResponse.getMessage(), Toast.LENGTH_SHORT).show();
                // chyba - vypni aktivitu, deaktivuj service
                onLeftResponse();

            }
        });

        runViewModel.getAtStartResponse().observe(this, new Observer<CommonResponse>() {
            @Override
            public void onChanged(CommonResponse response) {

                Toast.makeText(getApplication(), response.getMessage(), Toast.LENGTH_SHORT).show();

                isRegistered = true;
                startCompetitionService(LocationServiceStates.INIT.toString());

//                try {
//
//                    onCompetitionFetched(runViewModel.getCompetition().getValue());
//                } catch (NullPointerException ex) {
//                    Log.e("On competition fetch - waypoints: ", "Nullptr exception");
//                }
            }
        });


//        runViewModel.getDbCompetition().observe(this, new Observer<UserCompetition>() {
//            @Override
//            public void onChanged(UserCompetition response) {
//
//                Toast.makeText(getApplication(), "Competition in DB updated!", Toast.LENGTH_SHORT).show();
//
//                // nasleduje  natiahnutie sutaze zo servera
//
////                try {
////
////                    onCompetitionFetched(runViewModel.getCompetition().getValue());
////                } catch (NullPointerException ex) {
////                    Log.e("On competition fetch - waypoints: ", "Nullptr exception");
////                }
//            }
//        });

    //iba pre performing stav↓
        runViewModel.getPerformingCompetition().observe(this, new Observer<UserCompetition>() {
            @Override
            public void onChanged(UserCompetition response) {

                if (response != null){
                    Toast.makeText(getApplication(), "Performing competition state in DB updated!", Toast.LENGTH_SHORT).show();

                    // zapamataj si tuto aktualnu sutaz
                    runViewModel.setDbCompetition(response);

                    //obsluha po aktualizacii stavu sutaze v internej mob. DB
                    onPerformingCompetitionUpdate(response);
                }
            }
        });

        // vsetky ostatne stavy - specialne pripady - sutaz zlyhala, skoncila, vyskytla sa chyba
        // v tomto momente priznak active i pri sutaziach inych ako onboarding a performing!
        runViewModel.getUsersActiveCompetition().observe(this, new Observer<UserCompetition>() {
            @Override
            public void onChanged(UserCompetition response) {

                if ((response != null)
                        && (!response.getCompetitorStatus().equalsIgnoreCase(CompetitorStates.ONBOARDING.toString()))
                        && (!response.getCompetitorStatus().equalsIgnoreCase(CompetitorStates.PERFORMING.toString()))){

                    Toast.makeText(getApplication(), "Actual competitor state: " + response.getCompetitorStatus() , Toast.LENGTH_SHORT).show();

                    // zapamataj si tuto aktualnu sutaz
                   // runViewModel.setDbCompetition(response);

                    //obsluha po aktualizacii stavu sutaze v internej mob. DB
                    onActiveCompetitionMarginStates(response);
                }else {
                    //competition has ended - check your notifications
                    // ask to leave
                }



                // nasleduje  natiahnutie sutaze zo servera

//                try {
//
//                    onCompetitionFetched(runViewModel.getCompetition().getValue());
//                } catch (NullPointerException ex) {
//                    Log.e("On competition fetch - waypoints: ", "Nullptr exception");
//                }
            }
        });


    }


    //nacita sa mapa, nacitaj novu sutaz

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        this.mapInstance = googleMap;

        if (!markerOptionsList.isEmpty()){  //mapa nebola dostupna ked sa tvorili markery, ostali v zozname
            addMarkersToMap();  // vykreslim
        }

        setupLocationCallback();
        initTracking(); // zacni ziskavat polohu
        startTracking();




        //stara implementacia↓
//        if ((getIntent() != null) && !isRegistered  // prvy raz si posielam sutaz zo stare aktivity - po serializacii sa ulozi do DB
//                && (getIntent().hasExtra(SERIALIZED_COMPETITION))) {
//            runViewModel.deserializeCompetition(getIntent().getStringExtra(SERIALIZED_COMPETITION));
//
//        }
//        else{       // pripad, ze nejde o prvy pristup do sutaze - dotiahni a vykresli aktualny stav
//
//            runViewModel.fetchCompetition(); // dotiahnutie aktualnych dat o sutazi na zaklade stavu ulozeneho v DB
//        }

        //runViewModel.fetchCompetition();    //dotiahni sutaz

        //this.mapInstance = googleMap;
       // enableTracking();
//        addMarkersToMap();
//
//        if (circleOptions != null) {
//            this.circle = mapInstance.addCircle(circleOptions);
//            handleGeofenceAdding(circleOptions.getCenter(),"");
//        }
//
//        if (!markers.isEmpty()) {
//            centerCamera(markers.get(0).getPosition());
//        }


    }

    private void enableTracking() {

        if (checkPermission() && mapInstance != null) {

            mapInstance.setMyLocationEnabled(true);
        } else {
            // nema povolene gps - ziadam o povolenie + dialog (shouldShow - musim pouzit dialog - nemozem sa rovno pytat )
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            } else {
                //rovno sa pytam na povolenia
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            }

        }

    }

    private Boolean checkPermission() {

        if (
                (ActivityCompat.checkSelfPermission(this,        //mam povolenie k polohe zariadeina
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            return true;
        }

        return false;
    }

    @Override
    public void onLocationProvided(double latitude, double longitude) {


    }


    @Override       //vola sa v callbacku na ziskanie polohy
    public void onLocationProvided(Location location) {

        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

        if (mapInstance != null){

            if (currentPosMarker != null){
                currentPosMarker.remove();
            }
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(currentLocation);
            markerOptions.title(getApplication().getString(R.string.gps_your_pos));
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

            currentPosMarker = mapInstance.addMarker(markerOptions);
            currentPosMarker.showInfoWindow();

            runBinding.floatingCenterButton.setVisibility(View.VISIBLE);


        }






        Log.d("latitude: ", String.valueOf(location.getLatitude()));
        Log.d("longitude: ", String.valueOf(location.getLongitude()));
    }

    private void onCompetitionFetched(CompetitionOverallData competition) {

        if (!runViewModel.getDbCompetition().competitorStatus.equals(CompetitorStates.PERFORMING.toString())){

            // sutaziaci uz nesutazi - ukaz dialog a tlacidlo na ukoncenie sutaze
            Toast.makeText(this, "Competitor out of competition ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!competition.getStatus().equals(CompetitionStates.ONGOING.toString())){
            // sutaz skoncila! ukaz dialog s tlacidlom na opustenie sutaze
            Toast.makeText(this, "Competition state:  " + competition.getStatus(), Toast.LENGTH_SHORT).show();
            return;
        }


        // sutaz je ONGOING, sutaziaci PERFORMING - sutaz prebieha - vykresli na mapu
        // vykresli waypointy

        int lastVisitedMarkerSeqNum = runViewModel.getDbCompetition().lastCheckpointPos;   // posledny navstiveny checkpoint je v DB

        if (mapInstance != null){

           // vycisti mapu od neaktualnych markerov a kruhov
            removeMarkersFromMap();

            if (circle!= null){
                circle.remove();
            }

            markerOptionsList.clear();  // pri novomm nacitani - zmaz stare markery
            showWaypoints(competition.getWaypointList());   // vytvori zoznam wayointov pre mapu - tento zoznam pred vykreslenim este modifikujem
            resolvePassedMarkers(lastVisitedMarkerSeqNum);  // vizualne odlisenie aktualneho markera
            addMarkersToMap();
            this.circle = mapInstance.addCircle(circleOptions);

            centerCamera(markers.get(lastVisitedMarkerSeqNum).getPosition());
        }

//
//        int newCheckpointPos = runViewModel.getLastVisitedWaypointPos() +1;
//
//        if (newCheckpointPos == 0){ //som posunuty - defaultne prichadza -1 - ziadny checkpoint nenavstiveny
//
//            newCheckpointPos = 1;
//        }
//
//        if (newCheckpointPos <= competition.getWaypointList().size()){
//
//            LatLng fenceCenter = competition.getCheckpointPosBySeqNum(newCheckpointPos);
//
//            if (fenceCenter !=null){
//
//                handleGeofenceAdding(fenceCenter,"");
//            }
//        }

    }


    //foreground service - snimanie polohy - na podmienku - odoslanie casu na server
    private void startCompetitionService(String action){

        Intent intent = new Intent(this,LocationService.class);
        intent.setAction(action);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            startForegroundService(intent);
        }
        else {
            //starsie zariadenia nemaju obmedzenia na foreground service
            startService(intent);
        }

    }

    private Boolean checkGeofencePermission(){ //boolean

        if (Build.VERSION.SDK_INT >= 29){   // od and 10 je treba permission pre background location

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED){

                return true;
                //addGeofence(circleOptions.getCenter(), GEOFENCE_RADIUS);
            }
            else{
                // request for permission - dialog okno
                if(ActivityCompat
                        .shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)){

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                }
                else {

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                }

            }

        }else {

            return true;
            //addGeofence(circleOptions.getCenter(), GEOFENCE_RADIUS);
        }

        return false;
    }

    private void handleGeofenceAdding(LatLng latLng, String actionTypeId){  //ci je startovny alebo ostatne

        if (checkGeofencePermission()){
            addGeofence(latLng,GEOFENCE_RADIUS);
        }
    }

    // naplnenie zoznamu markerov pre mapu, marker ma atributy podla vstupu, farebne odlisenie podla aktualnosti markera ↓

    private void showWaypoints(List<CustomWaypointDesc> waypoints) {

        int color = 0;
        // show markers on map
        for (CustomWaypointDesc waypoint : waypoints) {

            if (waypoint.getSeqNumber() == waypoints.size()) {

                color = getColor(R.color.picked_green);
            } else if (waypoint.getSeqNumber() == 1) {

                color = getColor(R.color.black);
            } else {
                color = getColor(R.color.dark_purple);
            }

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(waypoint.getLat(), waypoint.getLng()));
            markerOptions.title(waypoint.getThoroughfare());
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(color, String.valueOf(waypoint.getSeqNumber()))));

            markerOptionsList.add(markerOptions);
        }
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

    private void resolvePassedMarkers(int lastVisitedCheckpointSeqNum) {    //farebne odlisenie uz navstivenych markerov

        // argumentom je sekvencne cislo posledne navstiveneho chechpointu (znacene od 1 po n)

        // ak pride 0 - doposial nedosiahol nijaky checkpoint - prvy v zozname je zvyrazneny - zvysne nevyrazne
        // ak pride 1 - dosiahol prvy checkpoint - druhy zvyrazneny  - zvysne nevyrazne

        for (int i = 0; i < markerOptionsList.size(); i++) {

          if ((i == (lastVisitedCheckpointSeqNum))) {    //iba nasledujuci marker nechaj vyrazny

                markerOptionsList.get(i).alpha(1.0f);
              // priprav kruh, co znaci toleranciu zaznamenania - iba aktualny marker
                this.circleOptions = createCircleOptions(markerOptionsList.get(i).getPosition(), GEOFENCE_RADIUS);

            } else {
                //ak nie je dalsim markerom na navstivenie - nevyrazny
                //dalsi marker na navstivenie
                markerOptionsList.get(i).alpha(0.4f);

            }
        }
    }

    private void addMarkersToMap() {

        if (mapInstance != null) {

            for (MarkerOptions marker : markerOptionsList) {
                Marker addedMarker = mapInstance.addMarker(marker);
                markers.add(addedMarker);
            }
        }
    }

    private void removeMarkersFromMap() {

        if (mapInstance != null) {

            for (MarkerOptions marker : markerOptionsList) {
                Marker addedMarker = mapInstance.addMarker(marker);
                markers.add(addedMarker);
            }
        }
    }

    //checkpoint zaznamena sutaziaceho v tolerancii radius kruhu okolo checkpointu
    private CircleOptions createCircleOptions(LatLng latLng, float radius) {

        CircleOptions circleOptions = new CircleOptions();  // kruh zobrazeny len k
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(R.color.purple_200);
        circleOptions.strokeWidth(4);
        circleOptions.fillColor(R.color.purple_geofence_circle);

        return circleOptions;
    }

    private void centerCamera(LatLng coords) {
        mapInstance.animateCamera(CameraUpdateFactory.newLatLngZoom(coords, 17f));
    }


    @SuppressLint("MissingPermission")  //check robim inde v kode - ok
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                mapInstance.setMyLocationEnabled(true);
            } else {
                //obsluha - ak neda povolenie
                Toast.makeText(getApplication(), getString(R.string.run_main_comp_location_not_granted),Toast.LENGTH_SHORT).show();

            }
        }

        if (requestCode == BACKGROUND_LOCATION_ACCESS_REQUEST_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                mapInstance.setMyLocationEnabled(true);
            } else {
                //obsluha - ak neda povolenie
                Toast.makeText(getApplication(), getString(R.string.run_main_comp_location_not_granted),Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void addGeofence(LatLng pos, float radius) {

        Geofence geofence = geofenceHelper
                .getGeofence(GEOFENCE_START_ID, pos, radius,
                        Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geoRequest = geofenceHelper.getGeofencingRequest(geofence);
        PendingIntent intent = geofenceHelper.getPendingIntent();


        geofencingClient.addGeofences(geoRequest, intent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("Geofence success: ", "Geofence has been added");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception ex) {

                        String errMsg = geofenceHelper.getErrorString(ex);
                        Log.e("Geofencing failure: ", errMsg);
                        Toast.makeText(getApplication(), errMsg, Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void onPerformingCompetitionUpdate(UserCompetition competitionDb){

        try{
            runViewModel.fetchCompetitionServer(competitionDb.getCompetitionId());

//            if (competitionDb.competitionStatus.equals(CompetitionStates.CANCELED.toString())
//                    || competitionDb.competitionStatus.equals(CompetitionStates.FINALIZED.toString())){
//
//                // dialog o skonceni sutaze + presun spat na hlavnu aktivitu
//                Toast.makeText(this, "competition status is: " + competitionDb.competitionStatus, Toast.LENGTH_SHORT).show();
//            }
//            else if (competitionDb.competitionStatus.equals(CompetitionStates.ONGOING.toString())
//                    && competitionDb.competitorStatus.equals(CompetitorStates.PERFORMING.toString())){
//
//                // dotiahni sutaz zo servera
//                runViewModel.fetchCompetitionServer(competitionDb.getCompetitionId());
//            }

        }catch (NullPointerException ex){

            Log.e("Performing competition update ", "Null ptr exception");
        }

    }

    private void onActiveCompetitionMarginStates(UserCompetition dbComp){

        dbComp.setActive(false);    //deaktivuj sutaz

        if (dbComp.competitorStatus.equalsIgnoreCase(CompetitorStates.QUIT.toString())){

            //1 zobraz dialog, ze sutaz pre uzivatela skoncila, dovod najde v notifikacii
            //2 nastav priznak competitorStatus na joined
            //3 nastav priznak active na 0 a vytvor event odidenie z aktivity

            RunQuitDialogFragment dialog = new RunQuitDialogFragment(dbComp);
            dialog.setOnCompetitionLeftListener(this);
            dialog.show(getSupportFragmentManager(),"quitDialog");
        }
        else if(dbComp.competitorStatus.equalsIgnoreCase(CompetitorStates.FINISHED.toString())){
            //1 sutaziaici uspesne v cieli
            //2 priznak competitorStatus nechaj FINISHED
            //3 otvor dialog okno s info o sutazi, umiestneni - v nom nacitaj sutaz zo serveru
            // active na 0
            // vyvolaj event odchodu z aktivity

            RunFinishDialogueFragment dialog = new RunFinishDialogueFragment(dbComp);
            dialog.setOnCompetitionLeftListener(this);
            dialog.show(getSupportFragmentManager(),"finishDialog");
        }
        else{
            // mozno doplnim dalsi stav

        }
    }

    private void onLeftResponse(){
        stopTracking();
        stopService(new Intent(this, LocationService.class));
        finish();
    }


    @Override
    public void onClick(View v) {

        if (v.equals(runBinding.floatingCenterButton) && (mapInstance != null) && (currentLocation != null)){

            mapInstance.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18f));
        }
        else if (v.equals(runBinding.runMainUpperMenuBtn)){

            PopupMenu popupMenu = new PopupMenu(this, v);
            popupMenu.setOnMenuItemClickListener(this);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.run_menu, popupMenu.getMenu());
            popupMenu.show();
        }

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        if (item.getItemId() == R.id.menu_item_leaveCompetition){

            UserCompetition dbComp = runViewModel.getDbCompetition();
            RunQuitDialogFragment dialog = new RunQuitDialogFragment(dbComp);
            dialog.setOnCompetitionLeftListener(this);
            dialog.show(getSupportFragmentManager(),"quitDialog");
            //onCompetitionLeft(dbComp);
            return true;
        }
        else if(item.getItemId() == R.id.menu_item_resendWaypoint){
            startCompetitionService(LocationServiceStates.RESEND.toString());
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (trackingInitialised){   // nie je to prve vytvorenie aktivity - v tomto pripade uz mam inicializovane premenne na lokalizaciu
            startTracking();    //nepotrebujem pre tuto aktivitu ziskavat gps info ked uzivatel nesleduje obrazovku
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTracking();
    }

    @Override
    public void onBackPressed() {
        stopTracking();
        super.onBackPressed();
        this.finish();
    }

    //nepouzivam
    // tato funkcia prebrata zo zdroja https://stackoverflow.com/a/63358282
    private BitmapDescriptor getMarkerIcon(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        return BitmapDescriptorFactory.defaultMarker(hsv[0]);
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        stopService(new Intent(this, LocationService.class));
//    }


    @Override
    public void onCompetitionLeft(UserCompetition competitionDb) {

        runViewModel.deactivateCompetition(competitionDb);
    }


}