package com.example.orienteering.competition.run.locationService;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.orienteering.MainActivity;
import com.example.orienteering.R;
import com.example.orienteering.competition.CompetitionStates;
import com.example.orienteering.competition.CompetitorStates;
import com.example.orienteering.competition.run.RunActivity;
import com.example.orienteering.dbWork.registration.UserCompetition;
import com.example.orienteering.dbWork.registration.UserDatabase;
import com.example.orienteering.nfts.nftToCompetition.CustomWaypointDesc;
import com.example.orienteering.retrofit.ApiClient;
import com.example.orienteering.retrofit.ApiInterface;
import com.example.orienteering.retrofit.patternClasses.CompetitorPattern;
import com.example.orienteering.retrofit.patternClasses.LeaderBoardItemPattern;
import com.example.orienteering.retrofit.responseClasses.CommonResponse;
import com.example.orienteering.retrofit.responseClasses.competition.CompetitionOverallData;
import com.example.orienteering.retrofit.responseClasses.competition.CompetitionOverallResponse;
import com.google.android.gms.common.internal.service.Common;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jnr.ffi.annotations.In;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
//naucne clanky - zdroje:
//https://www.youtube.com/watch?v=NHkcM3IunN8&list=PLQkwcJG4YTCQ6emtoqSZS2FVwZR9FT3BV&index=11
//https://www.youtube.com/watch?v=BXwDM5VVuKA

//part 12  location foreground service
//https://www.youtube.com/watch?v=JpVBPKf2mIU&list=PLQkwcJG4YTCQ6emtoqSZS2FVwZR9FT3BV&index=12

public class LocationService extends LifecycleService  {

//location ↓
    public static final float WAYPOINT_RADIUS = 10f;    // priemer kruhu tolerancie v metroch - tolerancia zapocitania waypointu
    public static final int DEFAULT_UPDATE_INTERVAL = 2000;
    public static final int FASTEST_UPDATE_INTERVAL = 1000;
    public static final int SMALLEST_DISPLACEMENT = 1;
    public static final int PERMISSION_FINE_LOCATION = 99;    //unique request code - moze byt lub. cislo
    public FusedLocationProviderClient fusedLocationProviderClient;    // umoznuje viacere sposoby lokalizacie
    public LocationRequest locationRequest;    // konfiguracny subor pre lokaciu
    public LocationCallback locationCallback;


    private Boolean trackingStarted = false;
    private Boolean recentCheckpointSent = false;


    // foreground service↓
    private static final String NOTIF_CHANNEL_ID = "ORIENTEERING_NOTIF_CHANNEL";
    private static final String NOTIF_WP_CHANNEL_ID = "WAYPOINT_NOTIF_CHANNEL";
    private static final String NOTIF_STATE_CHANNEL_ID = "STATE_NOTIF_CHANNEL";
    private static final int PI_REQUEST_CODE = 147;
    private static final int PI_REQUEST_CODE1 = 148;
    private static final int PI_REQUEST_CODE2 = 149;
    private static final int FOREGROUND_NOTIF_ID = 1111;
    private static final int WP_NOTIF_ID = 2222;
    private static final int STATE_NOTIF_ID = 3333;

    //data↓
    private MutableLiveData<UserCompetition>  dbCompetition = new MutableLiveData<UserCompetition>();

    // v dobe od zacatia zasielania na server po response je inicializovany, inak null
    //resend poziadavky
    private LeaderBoardItemPattern recordTobeSend = null;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if ((intent !=null) && (intent.getAction()!= null)){


            if (intent.getAction().equals(LocationServiceStates.INIT.toString())){

                Intent intent1 = new Intent(this, RunActivity.class);
                createNotificationChannel();


                PendingIntent pendingIntent  = PendingIntent.getActivity(this, PI_REQUEST_CODE, intent1,0);
                Notification notification = new NotificationCompat.Builder(this, NOTIF_CHANNEL_ID)
                        .setContentTitle("Orienteering")
                        .setContentText("Competition running. GPS tracking.")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentIntent(pendingIntent).build();

                startForeground(1, notification);
                Log.d("LocationService", "Service onStartComdand");

                dbCompetition.observe(this, new Observer<UserCompetition>() {
                    @Override
                    public void onChanged(UserCompetition dbComp) {

                        try{
                            onDBCompetitionUpdated(dbComp);
                        }catch (NullPointerException ex){

                            //zle data - stopni servis
                            notifyServerCompLeft(getString(R.string.run_notif_comp_state_error));
                            Log.e("DB competition null ptr: ","Competition in DB - inconsistent data");
                        }
                    }
                });

                fetchDbCompetition();

            }
            else if(intent.getAction().equals(LocationServiceStates.RESEND.toString())){

                resendAtWaypoint();
            }


        }



        //requestLocation();
        return super.onStartCommand(intent, flags, startId);
    }


    private void resendAtWaypoint(){

        if ((this.recordTobeSend!= null) && recentCheckpointSent){

            if (getApplicationContext() != null){
                Toast.makeText(getApplicationContext(), "Waypoint resent!", Toast.LENGTH_SHORT).show();
            }
            //v pripade ak retrofit call skonci na chybe spojenia v onFailed
            sendToLeaderboard(recordTobeSend);
        }

    }

    private void createNotificationChannel(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            NotificationChannel notificationChannel =
                    new NotificationChannel(NOTIF_CHANNEL_ID, "Orienteering notif.", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);

            // kanal pre dosiahnutuie waypointu
            NotificationChannel notificationWpChannel =
                    new NotificationChannel(NOTIF_WP_CHANNEL_ID, "WP passed notif.", NotificationManager.IMPORTANCE_HIGH);

            notificationManager.createNotificationChannel(notificationWpChannel);

            // kanal pre nitofikaciu o stave sutaze - chyby, zmeny, ...
            NotificationChannel notificationStateChannel =
                    new NotificationChannel(NOTIF_STATE_CHANNEL_ID, "Comp. state notif.", NotificationManager.IMPORTANCE_HIGH);

            notificationManager.createNotificationChannel(notificationStateChannel);
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

    public void initTracking() {


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(DEFAULT_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        locationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT);

        // LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY  - BTS a WIFI
        // PRIORITY_HIGH_ACCURACY  - GPS

        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);  // aky vykon vynalozi zariadenie
    }


    private void onLocationProvided(Location location){

        //prisla nova aktualna lokacia - skontroluj, ci sa uzivatel nachadza v aktualnom waypointe

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        Date date = new Date();

        Log.d("Foreground service loc. update in time: ", sdf.format(date));
        Log.d("Latitude: ",String.valueOf(location.getLatitude()));
        Log.d("Longitude: ",String.valueOf(location.getLongitude()));

        try{
            // ak uz neprebieha posielanie na server a zaroven je v range checkpointu
            if (!recentCheckpointSent && checkInRange(location)){

                recentCheckpointSent = true;

                UserCompetition dbComp = dbCompetition.getValue();

                LeaderBoardItemPattern leadItem = new LeaderBoardItemPattern();
                leadItem.setArrivalTime(sdf.format(new Date()));
                leadItem.setCompetitionId(dbComp.competitionId);
                leadItem.setCompetitorId(dbComp.userId);
                leadItem.setWaypointId(dbComp.getNextWaypointId());

                sendToLeaderboard(leadItem);
            }

        }catch (NullPointerException ex){

            recentCheckpointSent = false;
            Log.e("DB compet. error", "Null ptr");
            stopService();
        }

    }

    private Boolean checkInRange(Location location){

        // funkcia kontroluje, ci sa poloha nachadza v tolerancii voci aktualnemu checkpointu

        UserCompetition comp = dbCompetition.getValue();

        float[] distanceToCenter = new float[1];
        Location.distanceBetween(comp.getNextCheckpointLat(),comp.getNextCheckpointLng(),
                        location.getLatitude(), location.getLongitude(), distanceToCenter);

        return distanceToCenter[0] <= WAYPOINT_RADIUS;
    }

    private Boolean checkOreoAbove(){

        // notification channel je potrebne vytvorit pre android vezie >=OREO



        return false;
    }

    private void stopService(){

        stopTracking();
        stopForeground(true);       //true  - zmaz notifikaciu
        stopSelf();

    }


    @Override
    public void onDestroy() {

        stopService();
        super.onDestroy();
    }

    public void stopTracking() { // prestan ukladat lokaciu

        if ((fusedLocationProviderClient != null) && (locationCallback != null) && (locationRequest!=null)){

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        trackingStarted = false;
        }
    }

    public void startTracking() {


        if (checkPermission()){

            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            trackingStarted = true;
            //updateLocation();
        }

    }


    private Boolean checkPermission(){

        if ((ActivityCompat.checkSelfPermission(this,        //mam povolenie k polohe zariadeina
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {return true;}

        return false;
    }

    private LiveData<UserCompetition> getPerformingCompetition() {
        return UserDatabase.getInstance(getApplication())
                .competitionDao().loggedLiveUserCompetitionByCompetitorStatus(CompetitorStates.PERFORMING.toString());
    }

    private void fetchDbCompetition(){

        UserDatabase db = UserDatabase.getInstance(getApplication());

        UserDatabase.getInstance(getApplication()).getTransactionExecutor().execute(

                () -> {
                    //nacitaj z db sutaz prave prihlaseneho uzivatela, taku, ktora ma stav prebiehajuca - moze byt len jedna -  zapis sync so serverom

                    UserCompetition dbComp = db.competitionDao()
                            .loggedUserCompetitionByCompetitorStatus(CompetitorStates.PERFORMING.toString());

                    if (dbComp == null){
                        // nie je v databaze - zastav service - inkonzistencia v DB
                        stopService();
                    }

                    dbCompetition.postValue(dbComp);
                }
        );
    }


    private void onDBCompetitionUpdated(UserCompetition dbComp){

        recentCheckpointSent = false;   // bool hodnota - pri false umozni na location update poslanie na server

        if (dbComp.getCompetitionStatus().equalsIgnoreCase(CompetitionStates.FINALIZED.toString())){
            onCompetitionStop(getString(R.string.run_notif_comp_state_finalized));
            stopService();
            return;
        }

        if (dbComp.getCompetitionStatus().equalsIgnoreCase(CompetitionStates.CANCELED.toString())){
            //notifyServerCompLeft(getString(R.string.run_notif_comp_state_quit));
            onCompetitionStop(getString(R.string.run_notif_comp_state_quit));
            stopService();
            return;
        }

        if (!dbComp.getCompetitionStatus().equalsIgnoreCase(CompetitionStates.ONGOING.toString())){
            //sutaz uz je ukoncena - ukaz notifikaciu, skonci service
            //notifyServerCompLeft(getString(R.string.run_notif_comp_state_quit));
            onCompetitionStop(getString(R.string.run_notif_comp_state_quit));
            stopService();
            return;
        }

        //kazdy zo stavov je uz v tomto momente obsluzeny - notifikacia a zmena v DB
        if (!dbComp.competitorStatus.equalsIgnoreCase(CompetitorStates.PERFORMING.toString())){
            stopService();
            return;
        }


        if (!trackingStarted){  // ak este nezacalo pravidelne sledovanie polohy

            setupLocationCallback();
            initTracking();
            startTracking();    // zacni ziskavat polohu
        }
    }

    private void sendToLeaderboard(LeaderBoardItemPattern leadItem){
        //zaslanie zaznamu o dosiahnuti checkpointu na server

        recentCheckpointSent = true;    // informuje dalsie callbacky polohy, ze maju ignorovat dosiahnutie checkpointu
        UserCompetition dbComp = dbCompetition.getValue();

        //retrofit server call

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

        this.recordTobeSend = leadItem;

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<CompetitionOverallResponse> call = apiInterface.postToLeaderboard(leadItem);

        Log.d("Checkpoint sent: ", "Checkpoint no. " + String.valueOf(dbComp.getLastCheckpointPos()+1) + "sent.");

        call.enqueue(new Callback<CompetitionOverallResponse>() {
            @Override
            public void onResponse(Call<CompetitionOverallResponse> call, Response<CompetitionOverallResponse> response) {

                // server nerpijal udaje - zla forma alebo chyba v internej mobilnej DB - ukonci sutaz
                if (!response.isSuccessful()){
                    try {
                        Log.e("Sending leaderboard item error: ", response.errorBody().string());

                       // server odpoveda no odmietol poziadavku
                        recentCheckpointSent = false;
                        notifyServerCompLeft(getString(R.string.run_notif_comp_state_error));
                        //onCompetitionStop(getString(R.string.run_notif_comp_state_error));

                    } catch (IOException e) {
                        Log.e("send comp err message: ","Wrong form");
                       //setErrResponse(getApplication().getString(R.string.common_con_error));
                        recentCheckpointSent = false;
                        notifyServerCompLeft(getString(R.string.run_notif_comp_state_error));
                        //onCompetitionStop(getString(R.string.run_notif_comp_state_error));
                    }
                }
                else if(response.code() == 200){

                    recordTobeSend = null;

                    Log.d("message", response.message());

                    try {

                        //navratova hodnota je zoznam sutazi - kde je aktualna sutaz
                        CompetitionOverallData competition = response.body().getData().get(0);

                        CustomWaypointDesc reachedWaypoint =
                                findWaypointBySeqNum(competition.getWaypointList(), (dbComp.lastCheckpointPos + 1));

                        int competitorStanding
                                = resolveStanding(dbComp.lastCheckpointPos+1, competition, dbComp.userId);


                        notifyUserOnCheckpointPassed(reachedWaypoint, competitorStanding);

                        if (reachedWaypoint.isFinish()){

                            //uzivatel dorazil do ciela
                            onFinishReached(competition);
                        }
                        else {

                            CustomWaypointDesc nextWaypoint
                                    = findWaypointBySeqNum(competition.getWaypointList(), (reachedWaypoint.getSeqNumber() +1));

                            updateDbCompetitionOnCheckpointPassed(nextWaypoint, dbComp);
                        }

                    }catch (NullPointerException ex){
                        Log.e("On leaderboard update: ", "Null ptr exception - Inconsistent server competition response");
                       // setErrResponse(getApplication().getString(R.string.comp_preview_no_comps_err));
                    }
                    catch (ParseException ex){

                        Log.e("On leaderboard update: ", "Date parse exception");
                    }
                }
            }

            @Override
            public void onFailure(Call<CompetitionOverallResponse> call, Throwable t) {

                if (t instanceof IOException){

                    Log.d("connection error", "smt went wrong");
                    //onCompetitionStop(getString(R.string.run_notif_comp_state_error));  //chyba spojenia
                    notifyOnStopCause(getString(R.string.run_resend_info), getString(R.string.run_need_action));
                }
            }
        });


    }

    private void notifyUserOnCheckpointPassed(CustomWaypointDesc reachedWaypoint, int standing){

        String notifTitle = getString(R.string.run_notif_checkpoint_reached);
        String notifText = getString(R.string.run_notif_checkpoint_reached_info) + " " + ((standing > 0)? standing : "Start");

        if (reachedWaypoint.isFinish()){
            notifTitle = getString(R.string.run_notif_checkpoint_reached_last);
        }else {

           notifTitle = notifTitle.replace("$1", String.valueOf(reachedWaypoint.getSeqNumber()));
        }

        Intent intent1 = new Intent(this, RunActivity.class);  //MainActivity????
        PendingIntent pendingIntent  = PendingIntent.getActivity(this, PI_REQUEST_CODE1, intent1,0);

        // vytvor notifikaciu, ze dosiahol chechpoint
        Notification notification = new NotificationCompat.Builder(this, NOTIF_WP_CHANNEL_ID)
                .setContentTitle(notifTitle)
                .setContentText(notifText)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent).build();

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.notify(WP_NOTIF_ID, notification);
    }

    private void updateDbCompetitionOnCheckpointPassed(CustomWaypointDesc nextWaypoint, UserCompetition dbComp){

        //aktualizuje nasledujuci checkpoint - suradnice, id, lastCheckpoint passed

        dbComp.setLastCheckpointPos(dbComp.getLastCheckpointPos() +1); //na aktualne dosiahnuty
        dbComp.setNextWaypointId(nextWaypoint.getId());
        dbComp.setNextCheckpointLat(nextWaypoint.getLat());
        dbComp.setNextCheckpointLng(nextWaypoint.getLng());

        UserDatabase db = UserDatabase.getInstance(getApplication());

        db.getTransactionExecutor().execute(

                () -> {
                    //nacitaj z db sutaz prave prihlaseneho uzivatela, taku, ktora ma stav prebiehajuca - moze byt len jedna -  zapis sync so serverom
                    if (dbComp == null){
                        // nie je v databaze - zastav service - inkonzistencia v DB
                        notifyServerCompLeft(getString(R.string.run_notif_comp_state_error));
                        //onCompetitionStop(getString(R.string.run_notif_comp_state_error));  //notifikuj a ukonci service
                    }

                    db.competitionDao().updateCompetition(dbComp);
                    dbCompetition.postValue(dbComp);
                }
        );

    }

    private void onFinishReached(CompetitionOverallData competition){

        // nastav sutaziaceho ako ukonceneho
        //skonci service

        UserDatabase db = UserDatabase.getInstance(getApplication());

        db.getTransactionExecutor().execute(

                () -> {
                    //nacitaj z db sutaz prave prihlaseneho uzivatela, taku, ktora ma stav prebiehajuca - moze byt len jedna -  zapis sync so serverom
                    if (dbCompetition.getValue() == null){
                        // nie je v databaze - zastav service - inkonzistencia v DB

                        onCompetitionStop(getString(R.string.run_notif_comp_state_error));  //notifikuj a ukonci service
                    }

                    //nastav parametre ukoncenej sutaze

                    UserCompetition finishedComp = dbCompetition.getValue();
                    finishedComp.setCompetitorStatus(CompetitorStates.FINISHED.toString());
                    finishedComp.setNextCheckpointLat(null);
                    finishedComp.setNextCheckpointLng(null);
                    finishedComp.setNextWaypointId(null);
                    finishedComp.setActive(true);           //pre aplikaciu vo foreground - FINISHED + ACTIVE = ukaze dialog
                    finishedComp.setLastCheckpointPos(0);

                    db.competitionDao().updateCompetition(finishedComp);
                    dbCompetition.postValue(finishedComp);
                }
        );
       // stopService();
    }

    private void onCompetitionStop(String cause){
        //aktualizuj DB

        UserDatabase db = UserDatabase.getInstance(getApplication());

        db.getTransactionExecutor().execute(

                () -> {
                    //nacitaj z db sutaz prave prihlaseneho uzivatela, taku, ktora ma stav prebiehajuca - moze byt len jedna -  zapis sync so serverom
                    if (dbCompetition.getValue() == null){
                        // nie je v databaze - zastav service - inkonzistencia v DB

                        //onCompetitionStop(getString(R.string.run_notif_comp_state_error));  //notifikuj a ukonci service
                        return;
                    }

                    //nastav parametre ukoncenej sutaze

                    UserCompetition quitComp = dbCompetition.getValue();
                    quitComp.setCompetitorStatus(CompetitorStates.QUIT.toString());
                    quitComp.setNextCheckpointLat(null);
                    quitComp.setNextCheckpointLng(null);
                    quitComp.setNextWaypointId(null);
                    quitComp.setNextWaypointId(null);
                    quitComp.setLastCheckpointPos(0);

                    db.competitionDao().updateCompetition(quitComp);
                    dbCompetition.postValue(quitComp);
                }
        );

        notifyOnStopCause(cause, getString(R.string.run_notif_comp_state_title));
    }


    private void notifyServerCompLeft(String cause){    //vzdaj sa sutaze

        if (dbCompetition.getValue() == null){return;}

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<CommonResponse> call = apiInterface.putCompetitorAsLeft(dbCompetition.getValue().competitionId, dbCompetition.getValue().getUserId());

        call.enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {


                if (!response.isSuccessful()){
                    try {
                        Log.e("Server update on left err: ", response.errorBody().string());

                        onCompetitionStop(response.errorBody().string());

                    } catch (IOException e) {
                        Log.e("send comp err message: ","Wrong form");
                        // setErrResponse(getApplication().getString(R.string.common_con_error));
                        onCompetitionStop(getString(R.string.run_notif_competitor_quit_fail));
                    }
                }
                else if(response.code() == 200){

                    onCompetitionStop(cause);
                }

            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {

                //chyba spojenia toast
                Log.d("connection error", "smt went wrong");
                onCompetitionStop(getString(R.string.run_notif_competitor_quit_fail));
            }
        });
    }


    private void notifyOnStopCause(String cause, String title){

        //vytvor notifikaciu s info

        Intent intent1 = new Intent(this, MainActivity.class);  //MainActivity????
        PendingIntent pendingIntent  = PendingIntent.getActivity(this, PI_REQUEST_CODE2, intent1,0);

        // vytvor notifikaciu, ze dosiahol chechpoint
        Notification notification = new NotificationCompat.Builder(this, NOTIF_STATE_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(cause)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent).build();

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.notify(STATE_NOTIF_ID, notification);
    }



    private CustomWaypointDesc findWaypointBySeqNum(List<CustomWaypointDesc> waypoints, int seqNum){

        for (CustomWaypointDesc waypoint : waypoints){

            if (waypoint.getSeqNumber() == seqNum){
                return waypoint;
            }
        }
        return null;
    }

    private int resolveStanding(int waypointNumber, CompetitionOverallData competition, String competitorId) throws ParseException {

        if (waypointNumber == 1){   //pre prvy waypoint vrat poziciu 0 - este len zacal na starte

            return 0;
        }


        // prejde cez  zaznamy leaderboardu - recent a first waypoint - urci poradie sutaziaceho

        List<CompetitorPattern> competitors = competition.getCompetitorsList();
        List<CustomWaypointDesc> waypoints = competition.getWaypointList();

        CustomWaypointDesc recentWaypoint = competition.getCheckpointBySeqNum(waypointNumber);
        CustomWaypointDesc firstWaypoint = competition.getCheckpointBySeqNum(1);


        Map<String,Long> leaderMap = new HashMap<>();

        List<CompetitorPattern> recentWPCompetitors = new ArrayList<>();

        // naplnim mapu z posledneho checkpointu - UserId -> cas Long millis

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

        for (LeaderBoardItemPattern leadItem : recentWaypoint.getLeaderboard()){

            Date date = sdf.parse(leadItem.getArrivalTime());

            if (date == null){ throw new NullPointerException();}

            leaderMap.put(leadItem.getCompetitorId(), date.getTime());
        }

        //od kazdeho casu s danym klucom odratam cas zaciatku sutaze (koncovy cas - pociatocny cas = dosiahnuty vysledny cas)

        for (LeaderBoardItemPattern leadItem : firstWaypoint.getLeaderboard()){

            if (leaderMap.containsKey(leadItem.getCompetitorId())){
                // dany ucastnik uz dosiahol nas sledovany waypoint

                Date date = sdf.parse(leadItem.getArrivalTime());

                Long recentWPTime = leaderMap.get(leadItem.getCompetitorId());

                Long resultTime = Long.valueOf(recentWPTime.longValue() - date.getTime());

                leaderMap.put(leadItem.getCompetitorId(),resultTime);

            }
        }

        List<Map.Entry<String, Long>> resultList = new ArrayList<>(leaderMap.entrySet());
        resultList.sort(Map.Entry.comparingByValue());      // casy zoradene


        //najdi sutaziaceho, ktoreho chcem a vrat jeho poradie vramci zoradeneho zoznamu mapovych Entry
        for (int i =0; i< resultList.size(); i++){

            if (resultList.get(i).getKey().equals(competitorId)){
                return i+1; //vrat poradie
            }
        }

        return 0;   // 0 ak neuspech
    }

}
