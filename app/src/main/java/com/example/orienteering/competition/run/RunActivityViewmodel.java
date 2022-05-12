package com.example.orienteering.competition.run;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.orienteering.R;
import com.example.orienteering.commonClasses.LoggedViewmodel;
import com.example.orienteering.competition.CompetitionStates;
import com.example.orienteering.competition.CompetitorStates;
import com.example.orienteering.dbWork.registration.UserCompetition;
import com.example.orienteering.dbWork.registration.UserDatabase;
import com.example.orienteering.dbWork.registration.UserRegistration;
import com.example.orienteering.nfts.nftToCompetition.CustomWaypointDesc;
import com.example.orienteering.retrofit.ApiClient;
import com.example.orienteering.retrofit.ApiInterface;
import com.example.orienteering.retrofit.patternClasses.CompetitorToCompetitionPattern;
import com.example.orienteering.retrofit.responseClasses.CommonResponse;
import com.example.orienteering.retrofit.responseClasses.competition.CompetitionOverallData;
import com.example.orienteering.retrofit.responseClasses.competition.CompetitionOverallResponse;
import com.example.orienteering.retrofit.responseClasses.competition.WaypointResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.web3j.abi.datatypes.Int;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.List;

import jnr.ffi.annotations.In;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RunActivityViewmodel extends LoggedViewmodel {

    private MutableLiveData<CompetitionOverallData> competition = new MutableLiveData<CompetitionOverallData>();    // zo servera

    private MutableLiveData<String> currentTime = new MutableLiveData<String>();


    //private MutableLiveData<UserCompetition> dbCompetition = new MutableLiveData<UserCompetition>();    //data o sutazi ulozene v zariadeni

    private UserCompetition dbCompetition;  //prave ulozena sutaz v DB - aktualizovana na zaklade observovania v DB

    private MutableLiveData<String> competitionName = new MutableLiveData<String>(getApplication().getString(R.string.run_main_comp_name));
    private MutableLiveData<List<CustomWaypointDesc>> waypoints = new MutableLiveData<List<CustomWaypointDesc>>();
    private MutableLiveData<Integer> lastWaypointInd = new MutableLiveData<Integer>();

    //responses↓
    private MutableLiveData<CommonResponse> errorResponse = new MutableLiveData<CommonResponse>();
    private MutableLiveData<CommonResponse> atStartResponse = new MutableLiveData<CommonResponse>();
    private MutableLiveData<CommonResponse> leftResponse = new MutableLiveData<CommonResponse>();

    // constructor ↓

    public RunActivityViewmodel(@NonNull Application application) {
        super(application);
        checkLogged(UserDatabase.getInstance(getApplication()));
    }


    public LiveData<UserCompetition> getPerformingCompetition() {

        return UserDatabase.getInstance(getApplication())
                .competitionDao().loggedLiveUserCompetitionByCompetitorStatus(CompetitorStates.PERFORMING.toString());
    }

    public LiveData<UserCompetition> getUsersActiveCompetition() {      //aktivna sutaz je len 1, zaujimaju ma stavy

        return UserDatabase.getInstance(getApplication())
                .competitionDao().getLiveLoggedUserActiveCompetition();
    }

    public MutableLiveData<CompetitionOverallData> getCompetition() {
        return competition;
    }

    public void setCompetition(MutableLiveData<CompetitionOverallData> competition) {
        this.competition = competition;
    }

    public MutableLiveData<String> getCompetitionName() {
        return competitionName;
    }

    public void setCompetitionName(MutableLiveData<String> competitionName) {
        this.competitionName = competitionName;
    }

    public MutableLiveData<CommonResponse> getLeftResponse() {
        return leftResponse;
    }

    public void setLeftResponse(MutableLiveData<CommonResponse> leftResponse) {
        this.leftResponse = leftResponse;
    }

    //    public MutableLiveData<UserCompetition> getDbCompetition() {
//        return dbCompetition;
//    }
//
//    public void setDbCompetition(MutableLiveData<UserCompetition> dbCompetition) {
//        this.dbCompetition = dbCompetition;
//    }

    public UserCompetition getDbCompetition() {
        return dbCompetition;
    }

    public void setDbCompetition(UserCompetition dbCompetition) {
        this.dbCompetition = dbCompetition;
    }

    public MutableLiveData<CommonResponse> getErrorResponse() {
        return errorResponse;
    }

    public void setErrorResponse(MutableLiveData<CommonResponse> errorResponse) {
        this.errorResponse = errorResponse;
    }

    public MutableLiveData<CommonResponse> getAtStartResponse() {
        return atStartResponse;
    }

    public void setAtStartResponse(MutableLiveData<CommonResponse> atStartResponse) {
        this.atStartResponse = atStartResponse;
    }

    public Integer getLastVisitedWaypointPos(){     //vrat posledny navstiveny waypoint - poradove cislo (0 ak start)

        //return getDbCompetition().getValue().getLastCheckpointPos();
        return getDbCompetition().lastCheckpointPos;
    }

    // v intente posielam json sutaze v ktorej je aktualne uzivatel
    public void deserializeCompetition(String serializedCompetition){

        Type myType = new TypeToken<CompetitionOverallData>(){}.getType();
        CompetitionOverallData deserializedComp = (CompetitionOverallData) new Gson().fromJson(serializedCompetition,myType);

        competition.postValue(deserializedComp);

        try {
            //activateCompetition(deserializedComp.getCompetitionId());
        }catch (NullPointerException ex){
            Log.e("Null ptr when activating competition", "");
        }
    }


    public void checkIfStartConfirmed(){    // ak bol start potvrdeny - Stav v DB je performing / ak nie ONBOARDING



        UserDatabase db = UserDatabase.getInstance(getApplication());

        db.getTransactionExecutor().execute(

            () -> {

                //nacitaj onboarding sutaz prave prihaseneho uzivatela - ak najde - tuto sutaz treba zaregistrovat


                UserCompetition dbComp = db.competitionDao().loggedUserCompetitionByCompetitorStatus(CompetitorStates.ONBOARDING.toString());

                if (dbComp == null){    //nenasiel - sutaz uz bola zaregistrovana

                    dbComp = db.competitionDao().loggedUserCompetitionByCompetitorStatus(CompetitorStates.PERFORMING.toString());

                    if (dbComp != null){

                        //dbCompetition.postValue(dbComp);  //TODO ZRUSIL SOM
                    }else {

                        //jedna sa o chybovy stav
                        Log.e("DB competition check","No ONBOARDING or performing competition");
                    }

                }else {

                    // aktualna sutaz je doposial neregistrovana - zaregistruj na server
                    confirmUserInCompetition(dbComp);
                }


            }
        );
    }

    public void fetchCompetition(){ // aktualne info o sutazi zo servera, na zaklade ID sutaze (ulozeneho v DB)

        UserDatabase db = UserDatabase.getInstance(getApplication());

        UserDatabase.getInstance(getApplication()).getTransactionExecutor().execute(

                () -> {
                    //nacitaj z db prihlaseneho uziatela

                    String userId = getLoggedUserId(db);

                    //nacitaj prave prebiehajucu sutaz uzivatela - uzivatel moze byt v max 1 sutazi ktora je v stave performing
                    UserCompetition dbComp =  db.competitionDao()
                            .getUsersPerformingCompetition(userId, CompetitorStates.PERFORMING.toString());

                    //dbCompetition.postValue(dbComp);
                    fetchCompetitionServer(dbComp.getCompetitionId()); //dotiahnutie aktualneho stavu sutaze zo servera
                }
        );
    }

    public void fetchCompetitionServer(String competitionId){

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<CompetitionOverallResponse> call = apiInterface.getCompetitionById(competitionId);

        call.enqueue(new Callback<CompetitionOverallResponse>() {
            @Override
            public void onResponse(Call<CompetitionOverallResponse> call, Response<CompetitionOverallResponse> response) {


                if (!response.isSuccessful()){

                    try {
                        setErrResponse(response.errorBody().string());
                    } catch (IOException e) {
                        setErrResponse(getApplication().getString(R.string.run_main_comp_fetch_error));
                        Log.e("cannot fetch actual comp state err message: ","Cannot fetch!");;
                    }
                }
                else if(response.code() == 200){

                    Log.d("message", response.message());

                    CompetitionOverallResponse res = response.body();

                    try {
                        competition.postValue(res.getData().get(0));
                    }catch (IndexOutOfBoundsException | NullPointerException ex){

                        Log.e("Competition from server: ", "Incorrect data received!");
                    }
                }

            }

            @Override
            public void onFailure(Call<CompetitionOverallResponse> call, Throwable t) {

                //chyba spojenia toast
                Log.d("connection error", "smt went wrong");
                setErrResponse(getApplication().getString(R.string.common_con_error));
            }
        });
    }

//    private UserCompetition fetchCurrentUserCompetitionDb(String userId){
//
//        UserDatabase db = UserDatabase.getInstance(getApplication());
//        // nacitaj z db udaje sutaze pre sutaziaceho
//        String userId = db.usersDao().getLoggedUser().getUserId();
//
//        UserCompetition currentComp =
//                db.competitionDao().getUsersPerformingCompetition(userId, competitionId, CompetitorStates.PERFORMING.toString());
//
//        // na zaklade udajov dotiahni sutaz z db
//        return currentComp;
//    }

    private void confirmUserInCompetition(UserCompetition competition){     //zasli na server potvrdenie, ze uzivatel prisiel na start a zucastni sa

        // uzivatel je na starte (je v okoli prveho checkpointu)

        if (true){  //je v tolerancii

            //zasli priznak na server


            CompetitorToCompetitionPattern pattern = new CompetitorToCompetitionPattern();
            pattern.setCompetitorId(competition.getUserId());
            pattern.setCompetitionId(competition.competitionId);
            pattern.setCompetitorEthAddress(competition.address);

            ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
            Call<WaypointResponse> call = apiInterface.putCompetitorAtStart(pattern);

            call.enqueue(new Callback<WaypointResponse>() {
                @Override
                public void onResponse(Call<WaypointResponse> call, Response<WaypointResponse> response) {


                    if (!response.isSuccessful()){

                        try {
                            setErrResponse(response.errorBody().string());
                        } catch (IOException e) {
                            setErrResponse(getApplication().getString(R.string.run_main_comp_atStart_error));
                            Log.e("at start server err ","Wrong request");
                        }
                    }
                    else if(response.code() == 200){

                        Log.d("message", response.message());

                        CustomWaypointDesc waypoint = response.body().getWaypoint();

                        if (waypoint == null){
                            setErrResponse(getApplication().getString(R.string.run_main_comp_atStart_error));
                            return;
                        }

                        CommonResponse res = new CommonResponse();
                        res.setSuccess(response.isSuccessful());
                        res.setMessage(getApplication().getString(R.string.run_main_comp_atStart_success));

                        competition.setCompetitorStatus(CompetitorStates.PERFORMING.toString());
                        competition.setNextCheckpointLat(waypoint.getLat());
                        competition.setNextCheckpointLng(waypoint.getLng());
                        competition.setLastCheckpointPos(0);
                        competition.setNextWaypointId(waypoint.getId());
                        activateCompetitionDb(competition);

                        //atStartResponse.postValue(res);

                    }

                }

                @Override
                public void onFailure(Call<WaypointResponse> call, Throwable t) {

                    //chyba spojenia toast
                    Log.d("connection error", "smt went wrong");
                    setErrResponse(getApplication().getString(R.string.common_con_error));
                }
            });

        }

    }

    //aktualizuj sutaz v DB na PERFORMING CompetitorState
    private void activateCompetitionDb(UserCompetition competition){

        competition.setCompetitorStatus(CompetitorStates.PERFORMING.toString());
        competition.setActive(true);

        UserDatabase db = UserDatabase.getInstance(getApplication());

            db.getTransactionExecutor().execute(

                () -> {

                    db.competitionDao().updateCompetition(competition);
                    //dbCompetition.postValue(competition);   // todo v tomto okamihu je sutaz v stave performing - observujem a vyvolam refresh

                    CommonResponse res = new CommonResponse();
                    res.setSuccess(true);
                    res.setMessage("Competition Successfully activated!");
                    atStartResponse.postValue(res);
                }
            );

        // najdi sutaz, kde je sutaziacim tento uzivatel, jej id je id ktore je ulozenej sutaze
        // tejto sutazi nastav priznak aktivny - ostatnym sutaziam co ma uzivatel daj stav inactive

       // try {




//            UserDatabase db = UserDatabase.getInstance(getApplication());
//
//            db.getTransactionExecutor().execute(
//
//                () -> {
//
//                    //zisti, ci sa uz v db nenachadza sutaz, v ktorej uz ucastnik bezi
//                    UserCompetition activeCompetition = db.competitionDao().loggedUserPerformingCompetition(competitionId, CompetitorStates.PERFORMING.toString());
//
//                    if (competition != null){
//
//                        // sutaz kde ucastnik bezi sa uz nachadza v db - nevytvaraj druhu
//                        dbCompetition.postValue(activeCompetition);
//                    }
//                    else {  // pripad - prve spustenie sutaze - vytvorenie zaznamu v db
//
//                        UserCompetition dbComp = db.competitionDao().getCompetitionById(competitionId);
//                        dbComp.setCompetitionStatus(CompetitionStates.ONGOING.toString());
//                        dbComp.setCompetitorStatus(CompetitorStates.PERFORMING.toString());
//                        dbComp.setLastCheckpointPos(-1); //start = je pred prvym checkpointom
//
//                        db.competitionDao().updateCompetition(dbComp);
//                        dbCompetition.postValue(dbComp);
//                        confirmUserCame(1.0,2.0, dbComp);
//                    }
//                }
//            );
//
//        }catch (NullPointerException ex){
//            Log.e("Activate competition: ","Nullptr when accessing competition Id!");
//        }
    }

    public void deactivateCompetition(UserCompetition dbComp){

        deactivateCompetitionServer(dbComp);
        //deactivateCompetitionDB(dbComp); robim na callback

    }

    private void deactivateCompetitionServer(UserCompetition dbComp){

            if (dbComp == null){return;}

            ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
            Call<CommonResponse> call = apiInterface.putCompetitorAsLeft(dbComp.competitionId, dbComp.getUserId());

            call.enqueue(new Callback<CommonResponse>() {
                @Override
                public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {

                    deactivateCompetitionDB(dbComp);

                    if (!response.isSuccessful()){
                        try {
                            Log.e("Server update on left err: ", response.errorBody().string());

                        } catch (IOException e) {
                            Log.e("server left comp err message: ","Wrong err msg form");
                        }

                    }
                    else if(response.code() == 200){

                        Log.d("server left comp success: ","Server successfully notified!");
                    }
                }

                @Override
                public void onFailure(Call<CommonResponse> call, Throwable t) {

                    //chyba
                    Log.d("connection error", "smt went wrong");
                    deactivateCompetitionDB(dbComp);
                }
            });
    }

    private void deactivateCompetitionDB(UserCompetition dbComp){

        UserDatabase db = UserDatabase.getInstance(getApplication());

        dbComp.setActive(false);


        db.getTransactionExecutor().execute(

                () -> {

                    db.competitionDao().updateCompetition(dbComp);

                    CommonResponse leftRes = new CommonResponse();
                    leftRes.setSuccess(true);
                    leftRes.setMessage("left");

                    leftResponse.postValue(leftRes);
                    //zrus appku
                }
        );
    }

    private void setErrResponse(String message){

        CommonResponse res = new CommonResponse();
        res.setSuccess(false);
        res.setMessage(message);

        errorResponse.postValue(res);
    }



    // nova implementacia







}
