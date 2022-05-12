package com.example.orienteering.competition.pickCompetition;

import android.app.Application;
import android.location.Address;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.orienteering.R;
import com.example.orienteering.commonClasses.LoggedViewmodel;
import com.example.orienteering.dbWork.registration.UserDatabase;
import com.example.orienteering.nfts.nftToCompetition.CustomWaypointDesc;
import com.example.orienteering.retrofit.ApiClient;
import com.example.orienteering.retrofit.ApiInterface;
import com.example.orienteering.retrofit.patternClasses.CompetitionPattern;
import com.example.orienteering.retrofit.responseClasses.competition.CompetitionAddData;
import com.example.orienteering.retrofit.responseClasses.competition.CompetitionAddResponse;
import com.example.orienteering.retrofit.responseClasses.competition.CompetitionOverallData;
import com.example.orienteering.retrofit.responseClasses.competition.CompetitionOverallResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PickCompetitionViewmodel extends LoggedViewmodel {

    //private MutableLiveData<List<CompetitionOverallData>> competitions = new MutableLiveData<List<CompetitionOverallData>>();

    public PickCompetitionViewmodel(@NonNull Application application) {
        super(application);
        super.checkLogged(UserDatabase.getInstance(getApplication()));
    }

    public MutableLiveData<CompetitionOverallResponse> competitionsResponse = new MutableLiveData<CompetitionOverallResponse>();

    public MutableLiveData<CompetitionOverallResponse> getCompetitionsResponse() {
        return competitionsResponse;
    }

    private Address lastSearch;

    public Address getLastSearch() {
        return lastSearch;
    }

    public void setLastSearch(Address lastSearch) {
        this.lastSearch = lastSearch;
    }


//    public MutableLiveData<List<CompetitionOverallData>> getCompetitions() {
//        return competitions;
//    }


    public CompetitionOverallData getCompetitionsByMarkerCoords(double lat, double lng){

        try {

            for (CompetitionOverallData competition : competitionsResponse.getValue().getData()){

                double searchLat = competition.getWaypointList().get(0).getLat();
                double searchLng = competition.getWaypointList().get(0).getLng();

                if ((searchLat==lat) && (searchLng==lng)){
                    return competition;
                }
            }

        }catch (NullPointerException ex){
            Log.e("pickcomp view model getCompetitionsByMarker: ","null ptr accessing the competition");
        }
        return null;
    }


    public void getEventsByMunicipality(Set<String> locations){    //mesto + okres, ak nedostupne - kraj

        //dotazuj server na sutaze

       String queryLocations = prepareLocationsString(locations);

       if (queryLocations.equals("")){

           setErrResponse(getApplication().getString(R.string.gps_geocoder_address_err));
       }
       else {

           ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
           Call<CompetitionOverallResponse> call = apiInterface.getCompetitionsByLocality(queryLocations);

           call.enqueue(new Callback<CompetitionOverallResponse>() {
               @Override
               public void onResponse(Call<CompetitionOverallResponse> call, Response<CompetitionOverallResponse> response) {


                   if (response.code() == 500){

                       //TODO parsovanie chyb
                       Log.d("connection error", response.message());
                       setErrResponse(getApplication().getString(R.string.reg_server_msg));
                   }
                   else if(response.code() == 200){

                       Log.d("message", response.message());
                       CompetitionOverallResponse compResponse = response.body();

                       if (compResponse.getData() == null){
                           setErrResponse(getApplication().getString(R.string.comp_picker_search_address_nocomp_error));
                           return;
                       }

                       if (compResponse.getData().isEmpty()){
                           setErrResponse(getApplication().getString(R.string.comp_picker_search_address_nocomp_error));
                           return;
                       }

                       competitionsResponse.postValue(compResponse);
                   }
                   else {
                       Log.d("connection error", "smt went wrong");

                       setErrResponse(getApplication().getString(R.string.reg_server_msg));

                   }
               }

               @Override
               public void onFailure(Call<CompetitionOverallResponse> call, Throwable t) {

                   //chyba spojenia toast
                   Log.d("connection error", "smt went wrong");
                   CompetitionAddData errResponse = new CompetitionAddData();
                   setErrResponse(getApplication().getString(R.string.common_con_error));
               }
           });

       }
    }

    private void setErrResponse(String errorStr){

        CompetitionOverallResponse response = new CompetitionOverallResponse();
        response.setMessage(errorStr);
        response.setSuccess(false);
        competitionsResponse.postValue(response);
    }


    private String prepareLocationsString(Set<String> locations){

        String queryLocations ="";

        for (String location : locations){

            location = location.replaceAll(",","");

            queryLocations = queryLocations + "," + location;
        }

        return  queryLocations.replaceFirst(",", "");

    }




}
