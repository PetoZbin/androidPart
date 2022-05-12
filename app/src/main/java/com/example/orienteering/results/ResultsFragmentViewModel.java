package com.example.orienteering.results;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.orienteering.R;
import com.example.orienteering.commonClasses.LoggedViewmodel;
import com.example.orienteering.dbWork.registration.UserDatabase;
import com.example.orienteering.dbWork.registration.UsersDao;
import com.example.orienteering.retrofit.ApiClient;
import com.example.orienteering.retrofit.ApiInterface;
import com.example.orienteering.retrofit.patternClasses.BasicResultPattern;
import com.example.orienteering.retrofit.responseClasses.CommonResponse;
import com.example.orienteering.retrofit.responseClasses.competition.CompetitionAddData;
import com.example.orienteering.retrofit.responseClasses.competition.CompetitionOverallResponse;
import com.example.orienteering.retrofit.responseClasses.results.PageResultData;
import com.example.orienteering.retrofit.responseClasses.results.PageResultResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ResultsFragmentViewModel extends LoggedViewmodel {



   // private Map<Integer, PageResultData> pageResultMap = new HashMap<>();

    private MutableLiveData<List<BasicResultPattern>> pageResults = new MutableLiveData<List<BasicResultPattern>>();
    private MutableLiveData<Integer> currentPage = new MutableLiveData<Integer>(1);
    private MutableLiveData<Integer> lastPage = new MutableLiveData<Integer>(1);

    private MutableLiveData<CommonResponse> errorResponse = new MutableLiveData<CommonResponse>();

    public ResultsFragmentViewModel(@NonNull Application application) {
        super(application);
        checkLogged(UserDatabase.getInstance(getApplication()));
    }

    public MutableLiveData<List<BasicResultPattern>> getPageResults() {
        return pageResults;
    }

    public void setPageResults(MutableLiveData<List<BasicResultPattern>> pageResults) {
        this.pageResults = pageResults;
    }

    public MutableLiveData<Integer> getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(MutableLiveData<Integer> currentPage) {
        this.currentPage = currentPage;
    }

    public MutableLiveData<Integer> getLastPage() {
        return lastPage;
    }

    public void setLastPage(MutableLiveData<Integer> lastPage) {
        this.lastPage = lastPage;
    }

    public MutableLiveData<CommonResponse> getErrorResponse() {
        return errorResponse;
    }

    public void setErrorResponse(MutableLiveData<CommonResponse> errorResponse) {
        this.errorResponse = errorResponse;
    }


    public String getCompetitionIdByListPos(int position){

        try {

            return getPageResults().getValue().get(position).getCompetitionId();

        }catch (NullPointerException |IndexOutOfBoundsException ex){

            return null;
        }

    }

    public void initResults(int intemsPerPage){
        //nacitaj sutaze - prve spustenie

        if (getUserId().getValue() !=null){

            fetchResults(getUserId().getValue(), 1, intemsPerPage);
        }
    }


    public void fetchResults(String userId, int pageNum, int itemsPerPage){

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<PageResultResponse> call = apiInterface.getFinishedResultsPageByUserId(userId, pageNum, itemsPerPage);

        call.enqueue(new Callback<PageResultResponse>() {
            @Override
            public void onResponse(Call<PageResultResponse> call, Response<PageResultResponse> response) {


                if (!response.isSuccessful()){
                    try {
                        Log.e("Finished page fetch error: ", response.errorBody().string());
                        setErrResponse(response.errorBody().string());

                    } catch (IOException e) {
                        Log.e("Finished page fetch error: ","Wrong err msg form");
                        setErrResponse(getApplication().getString(R.string.results_fetch_error));
                    }

                }
                else if(response.code() == 200){

                    // prisla odpoved so sutazami

                    try {
                        PageResultData responseData = response.body().getData();

                        currentPage.postValue(responseData.getPageNum());
                        lastPage.postValue(responseData.getAllPages());
                        pageResults.postValue(responseData.getResults());

                    }catch (NullPointerException ex){

                        Log.e("Finished page content error: ","Null ptr exception");
                        currentPage.postValue(1);
                        lastPage.postValue(1);
                    }

                }
            }

            @Override
            public void onFailure(Call<PageResultResponse> call, Throwable t) {

                if (t instanceof IOException){

                    //chyba spojenia
                    setErrResponse(getApplication().getString(R.string.common_con_error));
                }
                //chyba
                Log.d("Finished page fetch error:", t.getMessage());
            }
        });

    }


    private void setErrResponse(String errorStr){

        CommonResponse response = new CommonResponse();
        response.setMessage(errorStr);
        response.setSuccess(false);
        errorResponse.postValue(response);
    }

}
