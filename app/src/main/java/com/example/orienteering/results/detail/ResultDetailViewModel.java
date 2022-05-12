package com.example.orienteering.results.detail;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.orienteering.R;
import com.example.orienteering.commonClasses.LoggedViewmodel;
import com.example.orienteering.dbWork.registration.UserDatabase;
import com.example.orienteering.retrofit.ApiClient;
import com.example.orienteering.retrofit.ApiInterface;
import com.example.orienteering.retrofit.patternClasses.CompetitorPattern;
import com.example.orienteering.retrofit.responseClasses.CommonResponse;
import com.example.orienteering.retrofit.responseClasses.competition.CompetitionAddData;
import com.example.orienteering.retrofit.responseClasses.competition.CompetitionOverallData;
import com.example.orienteering.retrofit.responseClasses.competition.CompetitionOverallResponse;
import com.example.orienteering.retrofit.responseClasses.results.PageResultData;
import com.example.orienteering.retrofit.responseClasses.results.PageResultResponse;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ResultDetailViewModel extends LoggedViewmodel {

    private String competitionId;
    private MutableLiveData<CommonResponse> errorResponse = new MutableLiveData<CommonResponse>();

    private MutableLiveData<String> competitionName = new MutableLiveData<String>("");
    private MutableLiveData<String> userTime = new MutableLiveData<String>("");
    private MutableLiveData<String> userPosToCompetitors = new MutableLiveData<String>();       //format standing/allCompetitorsNum
    private MutableLiveData<String> winnerNick = new MutableLiveData<String>("");
    private MutableLiveData<String> winnerTime = new MutableLiveData<String>("");
    private MutableLiveData<CompetitionOverallData> competition = new MutableLiveData<CompetitionOverallData>();

    public MutableLiveData<String> getCompetitionName() {
        return competitionName;
    }

    public MutableLiveData<String> getUserTime() {
        return userTime;
    }

    public MutableLiveData<String> getUserPosToCompetitors() {
        return userPosToCompetitors;
    }

    public MutableLiveData<String> getWinnerNick() {
        return winnerNick;
    }

    public MutableLiveData<String> getWinnerTime() {
        return winnerTime;
    }

    public String getCompetitionId() {
        return competitionId;
    }

    public void setCompetitionId(String competitionId) {
        this.competitionId = competitionId;
    }

    public MutableLiveData<CompetitionOverallData> getCompetition() {
        return competition;
    }

    public MutableLiveData<CommonResponse> getErrorResponse() {
        return errorResponse;
    }

    public void setErrorResponse(MutableLiveData<CommonResponse> errorResponse) {
        this.errorResponse = errorResponse;
    }

    public ResultDetailViewModel(@NonNull Application application) {
        super(application);
        checkLogged(UserDatabase.getInstance(getApplication()));
    }

    public void fetchCompetition(String competitionId){

        if (competitionId != null){

            ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
            Call<CompetitionOverallResponse> call = apiInterface.getCompetitionById(competitionId);

            call.enqueue(new Callback<CompetitionOverallResponse>() {
                @Override
                public void onResponse(Call<CompetitionOverallResponse> call, Response<CompetitionOverallResponse> response) {


                    if (!response.isSuccessful()){
                        try {
                            Log.e("Competition fetch error: ", response.errorBody().string());
                            setErrResponse(response.errorBody().string());

                        } catch (IOException e) {
                            Log.e("Competition fetch error: ","Wrong err msg form");
                            setErrResponse(getApplication().getString(R.string.results_fetch_error));
                        }

                    }
                    else if(response.code() == 200){

                        // prisla odpoved so sutazami



                            if ((response.body().getData() != null) && !response.body().getData().isEmpty()){

                                competition.postValue(response.body().getData().get(0));
                            }
                            else {
                                setErrResponse(getApplication().getString(R.string.results_fetch_error));
                            }
                    }
                }

                @Override
                public void onFailure(Call<CompetitionOverallResponse> call, Throwable t) {

                    if (t instanceof IOException){

                        //chyba spojenia
                        setErrResponse(getApplication().getString(R.string.common_con_error));
                    }
                    //chyba
                    Log.d("Competition fetch error: fetch error:", t.getMessage());
                }
            });
        }

    }

    public void setupFieldsData(CompetitionOverallData competition){

        try {

            CompetitorPattern userCompetitor = competition.getCompetitorById(getUserId().getValue());
            CompetitorPattern winnerCompetitor = competition.getWinner();

            if (userCompetitor !=null){

                userTime.postValue(userCompetitor.getTotalTime());
                userPosToCompetitors.postValue(userCompetitor.getTotalRank() + " / " + competition.getCompetitorsNum());
            }

            if (winnerCompetitor != null){

                winnerNick.postValue(winnerCompetitor.getUsername());
                winnerTime.postValue(winnerCompetitor.getTotalTime());
            }

            competitionName.postValue(competition.getName());


        }catch (NullPointerException ex){

            Log.e("Competition fields set: ","server competition data null ptr ex");
        }
    }

    private void setErrResponse(String errorStr){

        CommonResponse response = new CommonResponse();
        response.setMessage(errorStr);
        response.setSuccess(false);
        errorResponse.postValue(response);
    }
}
