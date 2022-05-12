package com.example.orienteering.competition.competitionPreview;

import android.app.Application;
import android.util.Log;
import android.widget.MultiAutoCompleteTextView;
import android.widget.SimpleAdapter;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.orienteering.R;
import com.example.orienteering.commonClasses.LoggedViewmodel;
import com.example.orienteering.competition.CompetitionStates;
import com.example.orienteering.competition.CompetitorStates;
import com.example.orienteering.dbWork.registration.CompetitionDao;
import com.example.orienteering.dbWork.registration.UserCompetition;
import com.example.orienteering.dbWork.registration.UserDatabase;
import com.example.orienteering.dbWork.registration.UserRegistration;
import com.example.orienteering.dbWork.registration.UsersDao;
import com.example.orienteering.retrofit.ApiClient;
import com.example.orienteering.retrofit.ApiInterface;
import com.example.orienteering.retrofit.patternClasses.CompetitorPattern;
import com.example.orienteering.retrofit.patternClasses.CompetitorToCompetitionPattern;
import com.example.orienteering.retrofit.responseClasses.CommonResponse;
import com.example.orienteering.retrofit.responseClasses.competition.CompetitionAddData;
import com.example.orienteering.retrofit.responseClasses.competition.CompetitionOverallData;
import com.example.orienteering.retrofit.responseClasses.competition.CompetitionOverallResponse;
import com.google.android.gms.dynamic.IFragmentWrapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.bouncycastle.math.ec.ECLookupTable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import okhttp3.internal.cache.CacheInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CompPreviewViewmodel extends LoggedViewmodel {


    public CompPreviewViewmodel(@NonNull Application application) {
        super(application);
        checkLogged(UserDatabase.getInstance(getApplication()));        // prave prihlaseny uzivatel + prave vybrany ucet tohto pouzivatela
    }

    private UsersDao usersDao = UserDatabase.getInstance(getApplication()).usersDao();

    public static final String SK_DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";
    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    private MutableLiveData<CompetitionOverallData> competition = new MutableLiveData<CompetitionOverallData>();
    private MutableLiveData<String> compTime = new MutableLiveData<String>();
    private MutableLiveData<String> timeLimit = new MutableLiveData<String>();
    private MutableLiveData<String> competitorsNumberString = new MutableLiveData<String>();
    private MutableLiveData<CommonResponse> competitorResponse = new MutableLiveData<CommonResponse>();
    private MutableLiveData<CommonResponse> competitionCancelResponse = new MutableLiveData<CommonResponse>();

    private MutableLiveData<Boolean> accessCompetitionEvent = new MutableLiveData<>();

    public MutableLiveData<CompetitionOverallData> getCompetition() {
        return competition;
    }

    public MutableLiveData<CommonResponse> getCompetitorResponse() {
        return competitorResponse;
    }

    public MutableLiveData<CommonResponse> getCompetitionCancelResponse() {
        return competitionCancelResponse;
    }

    public void setCompetitionCancelResponse(MutableLiveData<CommonResponse> competitionCancelResponse) {
        this.competitionCancelResponse = competitionCancelResponse;
    }

    public void setCompetitorResponse(MutableLiveData<CommonResponse> competitorResponse) {
        this.competitorResponse = competitorResponse;
    }

    public MutableLiveData<Boolean> getAccessCompetitionEvent() {
        return accessCompetitionEvent;
    }

    public void setAccessCompetitionEvent(MutableLiveData<Boolean> accessCompetitionEvent) {
        this.accessCompetitionEvent = accessCompetitionEvent;
    }

    public void setCompetition(String jsonCompetition){ //deserializacia z json

        Type myType = new TypeToken<CompetitionOverallData>(){}.getType();

        CompetitionOverallData deserializedComp = (CompetitionOverallData) new Gson().fromJson(jsonCompetition,myType);

        this.competition.postValue(deserializedComp);

            try{
                if (deserializedComp.isCompetitor(getUserId().getValue())
                        || deserializedComp.isUserOwner(getUserId().getValue())){

                    persistCompetition(resolveCompetitor(deserializedComp, getUserId().getValue()));
                }

            }catch (NullPointerException ex){

                Log.e("init competition: userId" , "NULL ptr exception");
            }

        }

    private UserCompetition resolveCompetitor(CompetitionOverallData competition, String userId){

        CompetitorPattern competitor = competition.getCompetitorById(userId);



            UserCompetition newComp = new UserCompetition();

        if((competitor != null)){    //nie je sutaziaci ale je vlastnik

            newComp.setAddress(competitor.getCompetitorEthAddress());
            newComp.setCompetitorStatus(competitor.getStatus());
        }

            newComp.setUserId(userId);
            newComp.setCompetitionId(competition.getCompetitionId());
            newComp.setCompetitionStatus(competition.getStatus());
            newComp.setOwner(competition.isUserOwner(userId));
            newComp.setLastCheckpointPos(0);

            return newComp;
    }


    public void setCompInfoVars(CompetitionOverallData competition){

        //hodnoty cez one way databinding
        competitorsNumberString
                .postValue(prepareNumCompetitorsString(
                                String.valueOf(competition.getCompetitorsList().size()),
                                String.valueOf(competition.getMaxCompetitors())));
        timeLimit.postValue(String.valueOf(competition.getDurationMins()));

        String skDate = convertToSkDateFormat(competition.getCompDateTime());

        if (skDate != null) {
            compTime.postValue(skDate);
        }
    }

    private String prepareNumCompetitorsString(String numComps, String maxComps){

        return numComps + " / " + maxComps + " " + "(max)";
    }

    private String convertToSkDateFormat(String isoDateTime){

        //iso format co pride z DB
       SimpleDateFormat sdf = new SimpleDateFormat(ISO_DATE_FORMAT, Locale.getDefault());

       try {
           Date datetime = sdf.parse(isoDateTime);
           sdf = new SimpleDateFormat(SK_DATE_FORMAT, Locale.getDefault());  // SK format
           return sdf.format(datetime);
       }
       catch (ParseException | NullPointerException e){

            Log.e("comp preview date conversion: ","date conversion error");
            return null;
        }

    }


    public void setCompetition(MutableLiveData<CompetitionOverallData> competition) {
        this.competition = competition;
    }

    public MutableLiveData<String> getCompTime() {
        return compTime;
    }

    public void setCompTime(MutableLiveData<String> compTime) {
        this.compTime = compTime;
    }

    public MutableLiveData<String> getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(MutableLiveData<String> timeLimit) {
        this.timeLimit = timeLimit;
    }

    public MutableLiveData<String> getCompetitorsNumberString() {
        return competitorsNumberString;
    }

    public void setCompetitorsNumberString(MutableLiveData<String> competitorsNumberString) {
        this.competitorsNumberString = competitorsNumberString;
    }

    public void joinCompetition(){  //uzivatel si narokuje miesto v sutazi


        // nacitanie dat ohladne aktualneho uctu z room db
        try {

            UserDatabase.getInstance(getApplication()).getTransactionExecutor().execute(

                    () -> {

                        UserRegistration currentUser = usersDao.getUserById(userId.getValue());

                        CompetitorToCompetitionPattern competitor = new CompetitorToCompetitionPattern();
                        competitor.setUsername(currentUser.getUsername());
                        competitor.setCompetitorId(currentUser.getUserId());
                        competitor.setCompetitorEthAddress(this.pickedAddress.getValue());
                        competitor.setCompetitionId(this.competition.getValue().getCompetitionId());
                        competitor.setActive(false);     //potvrdenie, ci sa nachadza na starte
                        competitor.setStatus(CompetitorStates.JOINED.toString());

                        sendCompetitor(competitor); //posli competitora na server
                    }
            );
        }catch (NullPointerException e){

            // chyba pri tvorbe CompetitorPattern objektu
            Log.e("Retrieving competitor from db: ", "Nullpointer exception");
            setErrResponse("Retrieving competitor from db ERR");
        }
    }

    public void giveUpCompetition(){  //uzivatel si narokuje miesto v sutazi

        final String userId = getUserId().getValue();;
        final String competitionId = getCompetition().getValue().getCompetitionId();
        final Boolean isOwner = getCompetition().getValue().isUserOwner(userId);

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<CommonResponse> call = apiInterface.removeCompetitor(competitionId, userId);

        call.enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {


                if (!response.isSuccessful()){

                    try {
                        setErrResponse(response.errorBody().string());
                    } catch (IOException e) {
                        setErrResponse(getApplication().getString(R.string.comp_preview_cannot_giveup_err));
                        Log.e("give up comp err message: ","Wrong form");;
                    }
                }
                else if(response.code() == 200){

                    Log.d("message", response.message());

                    CommonResponse res = new CommonResponse();
                    res.setSuccess(response.isSuccessful());
                    res.setMessage(response.body().getMessage());

                    competitorResponse.postValue(res);

                    if (isOwner){       //ak je vlastnik, nechaj sutaz v db ale uprav status
                        giveUpCompetitionDB(userId, competitionId);
                    }
                    else {  //ak nie je vlastnik - vsetky vztahy k sutazi zanikli - zmaz
                        deleteCompetition(userId, competitionId);
                    }
                }

            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {

                //chyba spojenia toast
                Log.d("connection error", "smt went wrong");
                setErrResponse(getApplication().getString(R.string.common_con_error));
            }
        });
    }

    private void sendCompetitor(CompetitorPattern competitor){

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<CommonResponse> call = apiInterface.postCompetitor(competitor);

        call.enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {


                if (!response.isSuccessful()){

                    CommonResponse res = new CommonResponse();
                    res.setSuccess(response.isSuccessful());
                    res.setMessage(getApplication().getString(R.string.common_con_error));
                    try {
                        res.setMessage(response.errorBody().string());
                    } catch (IOException e) {
                        Log.e("send comp err message: ","Wrong form");;
                    }

                    competitorResponse.postValue(res);
                }
                else if(response.code() == 200){

                    Log.d("message", response.message());

                    CommonResponse res = new CommonResponse();
                    res.setSuccess(response.isSuccessful());
                    res.setMessage(response.body().getMessage());

                    competitorResponse.postValue(res);

                    persistJoinedCompetition(competitor);
                }

            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {

                //chyba spojenia toast
                Log.d("connection error", "smt went wrong");
                setErrResponse(getApplication().getString(R.string.common_con_error));
            }
        });
    }

    public Boolean canUserJoin(){

        try {

            if (getCompetition().getValue().isFreePlace()
                    && getCompetition().getValue().notStartedYet()
                    && !getCompetition().getValue().isCompetitor(getUserId().getValue())){

                return true;
            }

        }catch (NullPointerException ex){
            Log.e("join check: ", "null ptr exception");
            return false;
        }
        catch (ParseException ex){

            Log.e("join check time: ", "parse exception");
            return false;
        }

        return false;
    }

    public Boolean canUserCancel(){

        try {

            if (getCompetition().getValue().isUserOwner(getUserId().getValue())
                && getCompetition().getValue().notStartedYet()) {

                return true;
            }

        }catch (NullPointerException ex){
            Log.e("join check: ", "null ptr exception");
            return false;
        }
        catch (ParseException ex){
            Log.e("cancel check: ", "date parse exc.");
            return false;
        }
        return false;
    }

    public Boolean canUserGiveUp(){ //moze odist? len ak je ucastnik

        try {

            if ( getCompetition().getValue().isCompetitor(getUserId().getValue())){

                return true;
            }

        }catch (NullPointerException ex){
            Log.e("join check: ", "null ptr exception");
            return false;
        }

        return false;

    }
    public Boolean canUserAttend(){ //moze pristupit k samotnemu preteku - 2 min do startu + jeho poloha je na mieste zacatia preteku

        try {

            if ( getCompetition().getValue().isCompetitor(getUserId().getValue())){

                return true;
            }

        }catch (NullPointerException ex){
            Log.e("join check: ", "null ptr exception");
            return false;
        }

        return false;

    }

    private void setErrResponse(String message){

        CommonResponse res = new CommonResponse();
        res.setSuccess(false);
        res.setMessage(message);

        competitorResponse.postValue(res);
    }

    public void loadCompetition(){  //refresh dat danej sutaze - aktualne data z db

        String competitionId;

        try {
            competitionId = getCompetition().getValue().getCompetitionId();

        }catch (NullPointerException ex){
            setErrResponse(getApplication().getString(R.string.comp_preview_no_comps_err));
            return;
        }


        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<CompetitionOverallResponse> call = apiInterface.getCompetitionById(competitionId);

        call.enqueue(new Callback<CompetitionOverallResponse>() {
            @Override
            public void onResponse(Call<CompetitionOverallResponse> call, Response<CompetitionOverallResponse> response) {


                if (!response.isSuccessful()){
                    try {
                        setErrResponse(response.errorBody().string());
                    } catch (IOException e) {
                        Log.e("send comp err message: ","Wrong form");;
                        setErrResponse(getApplication().getString(R.string.common_con_error));
                    }

                }
                else if(response.code() == 200){

                    Log.d("message", response.message());

                    CompetitionOverallResponse res = response.body();


                    try {

                        CompetitionOverallData competitionRes = res.getData().get(0);
                        if (competition!=null){

                            competition.postValue(competitionRes);
                        }
                        else setErrResponse(getApplication().getString(R.string.comp_preview_no_comps_err));

                    }catch (NullPointerException ex){

                        setErrResponse(getApplication().getString(R.string.comp_preview_no_comps_err));
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

    public void cancelCompetition(){

        String competitionId;

        try {
            competitionId = getCompetition().getValue().getCompetitionId();

        }catch (NullPointerException ex){
            setErrResponse(getApplication().getString(R.string.comp_preview_no_comps_err));
            return;
        }

        CompetitorToCompetitionPattern comp = new CompetitorToCompetitionPattern();
        comp.setCompetitionId(competitionId);
        comp.setCompetitorId(getUserId().getValue());

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<CommonResponse> call = apiInterface.removeCompetition(competitionId, getUserId().getValue());

        call.enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {


                if (!response.isSuccessful()){
                    try {
                        setErrResponse(response.errorBody().string());
                    } catch (IOException e) {
                        Log.e("send comp err message: ","Wrong form");;
                        setErrResponse(getApplication().getString(R.string.common_con_error));
                    }
                }
                else if(response.code() == 200){

                    Log.d("message", response.message());

                    try {

                        CommonResponse res = new CommonResponse();
                        res.setMessage(getApplication().getString(R.string.comp_preview_competition_revoked));
                        res.setSuccess(response.isSuccessful());
                        competitionCancelResponse.postValue(res);

                        deleteCompetition(getUserId().getValue(), competitionId);

                    }catch (NullPointerException ex){

                        setErrResponse(getApplication().getString(R.string.comp_preview_no_comps_err));
                    }
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {

                //chyba spojenia toast
                Log.d("connection error", "smt went wrong");
                setErrResponse(getApplication().getString(R.string.common_con_error));
            }
        });


    }

    private void deleteCompetition(String userId, String competitionId){

            //zisti, ci je nejaky uzivatel uz prihlaseny

            UserDatabase db = UserDatabase.getInstance(getApplication());

            db.getTransactionExecutor().execute(

                () -> {

                    UserCompetition comp = db.competitionDao().getLoggedUserCompetitionById(competitionId);

                    if (comp != null){
                        db.competitionDao().deleteUserCompetition(userId, competitionId);
                    }
                    else {
                        Log.e("Delete competition: ", "internal db data inconsistence");
                    }
                });
    }

    private void persistJoinedCompetition(CompetitorPattern competitor){

        //zisti, ci je nejaky uzivatel uz prihlaseny

        try {

            UserCompetition newComp = new UserCompetition();

            newComp.setUserId(getUserId().getValue());
            newComp.setCompetitionId(competitor.getCompetitionId());
            newComp.setCompetitionStatus(competition.getValue().getStatus());
            newComp.setCompetitorStatus(CompetitorStates.JOINED.toString());
            newComp.setOwner(competition.getValue().isUserOwner(competitor.getCompetitorId()));
            newComp.setAddress(competitor.getCompetitorEthAddress());
            newComp.setLastCheckpointPos(0);
            newComp.setCompDateTime(competition.getValue().getCompDateTime());
            newComp.setDurationMins(competition.getValue().getDurationMins());
            newComp.setActive(false);


            persistCompetition(newComp);

        }catch (NullPointerException ex){

            Log.e("Competition db error: ", "nullptr ex. when adding competition");
        }
    }

    private void persistCompetition(UserCompetition newComp){

        UserDatabase db = UserDatabase.getInstance(getApplication());

        db.getTransactionExecutor().execute(

                () -> {

                    Long userCompId = db.competitionDao().getUserCompId(newComp.getUserId(), newComp.getCompetitionId());

                    if (userCompId != null){
                        //uz existuje - pravdepodobne sa prihlasil organizator aj do sutaze
                        newComp.setId(userCompId);
                        db.competitionDao().updateCompetition(newComp);
                    }
                    else {
                        db.competitionDao().insertCompetition(newComp);
                    }
                }
        );

    }

    private void giveUpCompetitionDB(String userId, String competitionId){

        //zisti, ci je organizator - ak je nemaz, len nastav priznak status na canceled

        try {

            UserCompetition newComp = new UserCompetition();
            newComp.userId = getUserId().getValue();
            newComp.setCompetitionId(competitionId);
            newComp.setCompetitionStatus(competition.getValue().getStatus());
            newComp.setCompetitorStatus(CompetitorStates.SPECTATOR.toString());
            newComp.setOwner(true); //organizator true
            newComp.setAddress(null);
            newComp.setActive(false);

            persistCompetition(newComp);

        }catch (NullPointerException ex){

            Log.e("Competition db error: ", "nullptr ex. when adding competition");
        }
    }

    public void onToCompetitionBtn(String compId){

        UserDatabase db = UserDatabase.getInstance(getApplication());

        db.getTransactionExecutor().execute(
                () ->{

                    // skusim pripad pre uz prebiehajucu sutaz s danym ucastnikom↓
                    List<UserCompetition> dbComps = db.competitionDao().getLoggedUserActiveCompetitions();

                    if ((dbComps.size() == 1) && dbComps.get(0).getCompetitionId().equals(compId)){

                            // nezrus sutaz - moze pokracovat v prave prebiehajucej - je zhodna s vyberom sutaze
                            accessCompetitionEvent.postValue(true);
                    }
                    else {

                        // pripad pre zvolenie novej sutaze, pripadne error pripad pre viacere prebiehajuce sutaze

                        db.competitionDao().updateDupliciteActiveCompetitions();    //pre ONBOARDING A AWAITING sutaze uzivatela daj stav JOINED
                        db.competitionDao().updateDupliciteActiveCompetitionsToInactive();
                        // nacitaj zvolenu sutaz, do kt. chce ucastnik vstupit
                        UserCompetition comp = db.competitionDao().getLoggedUserCompetitionById(compId);

                        if ((comp != null) && comp.competitorStatus.equals(CompetitorStates.JOINED.toString())){

                            comp.setCompetitorStatus(CompetitorStates.ONBOARDING.toString());   // stav ktory sa zmeni na performing po potvrdeni serverom
                            comp.setCompetitionStatus(competition.getValue().getStatus());      // ONGOING
                            db.competitionDao().updateCompetition(comp);
                            accessCompetitionEvent.postValue(true);

                        }else {
                            // stav by nemal nastat, pretoze joined sutaze su uz v DB, aktualizacia db voci serveru prebehla v tomto momente
                            Log.e("competitition DB error: ", "Competition not found in local DB!");
                        }
                    }
                });
    }


}
