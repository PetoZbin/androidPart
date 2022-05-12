package com.example.orienteering.competition.activeCompetitions;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.orienteering.R;
import com.example.orienteering.dbWork.registration.UserCompetition;
import com.example.orienteering.dbWork.registration.UserDatabase;
import com.example.orienteering.retrofit.ApiClient;
import com.example.orienteering.retrofit.ApiInterface;
import com.example.orienteering.retrofit.responseClasses.CommonResponse;
import com.example.orienteering.retrofit.responseClasses.competition.CompetitionOverallData;
import com.example.orienteering.retrofit.responseClasses.competition.CompetitionOverallResponse;
import com.google.android.gms.common.internal.service.Common;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActiveCompetitionsViewModel extends AndroidViewModel {


    private MutableLiveData<List<UserCompetition>> dbFocusedCompetitions = new MutableLiveData<List<UserCompetition>>();
    private MutableLiveData<CommonResponse> errorResponse = new MutableLiveData<CommonResponse>();
    private MutableLiveData<CompetitionOverallData> competitionToPreview = new MutableLiveData<CompetitionOverallData>();

    public ActiveCompetitionsViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<List<UserCompetition>> getDbCompetitions() {
        return dbFocusedCompetitions;
    }

    public void setDbCompetitions(MutableLiveData<List<UserCompetition>> dbCompetitions) {
        this.dbFocusedCompetitions = dbCompetitions;
    }

    public MutableLiveData<CommonResponse> getErrorResponse() {
        return errorResponse;
    }

    public MutableLiveData<CompetitionOverallData> getCompetitionToPreview() {
        return competitionToPreview;
    }

    public void setCompetitionToPreview(MutableLiveData<CompetitionOverallData> competitionToPreview) {
        this.competitionToPreview = competitionToPreview;
    }

    public void fetchFocusedDbCompetitions(){

        UserDatabase db = UserDatabase.getInstance(getApplication());

        db.getTransactionExecutor().execute(()->{

            List<UserCompetition> competitions = db.competitionDao().getLoggedUsersFocusedCompetitions();
            dbFocusedCompetitions.postValue(competitions);

        });
    }

    public void fetchCompetitionServer(String competitionId){

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<CompetitionOverallResponse> call = apiInterface.getCompetitionById(competitionId);

        call.enqueue(new Callback<CompetitionOverallResponse>() {
            @Override
            public void onResponse(Call<CompetitionOverallResponse> call, Response<CompetitionOverallResponse> response) {


                if (!response.isSuccessful()){

                    competitionToPreview.postValue(null);
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
                        competitionToPreview.postValue(res.getData().get(0));
                    }catch (IndexOutOfBoundsException | NullPointerException ex){
                        competitionToPreview.postValue(null);
                        Log.e("Competition from server: ", "Incorrect data received!");
                    }
                }

            }

            @Override
            public void onFailure(Call<CompetitionOverallResponse> call, Throwable t) {
                competitionToPreview.postValue(null);

                if (t instanceof IOException){

                    //chyba spojenia toast
                    setErrResponse(getApplication().getString(R.string.common_con_error));
                    Log.d("connection error", "smt went wrong");
                }
                else Log.d("competition fetch error", "on failure - not network");
            }
        });
    }

    public void orderChosenCompToBeFetched(int compIndex){  // na zaklade pozicie v zozname dotiahni sutaz zo servera

        try{

            String compId = this.getDbCompetitions().getValue().get(compIndex).competitionId;
            fetchCompetitionServer(compId);

        }catch (NullPointerException | IndexOutOfBoundsException ex){

            Log.d("active comp dialog", "wrong data - server fetch");
            setErrResponse(getApplication().getString(R.string.run_main_comp_fetch_error));
        }
    }

    private void setErrResponse(String message){

        CommonResponse res = new CommonResponse();
        res.setSuccess(false);
        res.setMessage(message);

        errorResponse.postValue(res);
    }
}
