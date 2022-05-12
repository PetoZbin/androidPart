package com.example.orienteering.nfts.nftManager;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.orienteering.R;
import com.example.orienteering.commonClasses.LoggedViewmodel;
import com.example.orienteering.dbWork.registration.UserDatabase;
import com.example.orienteering.dbWork.registration.UserRegistration;
import com.example.orienteering.dbWork.registration.UsersDao;
import com.example.orienteering.nfts.nftManager.responses.NftFetchFailResponse;
import com.example.orienteering.retrofit.ApiClient;
import com.example.orienteering.retrofit.ApiInterface;
import com.example.orienteering.retrofit.responseClasses.login.LoginResponse;
import com.example.orienteering.retrofit.responseClasses.tokens.AddressNftsData;
import com.example.orienteering.retrofit.responseClasses.tokens.AddressNftsResponse;
import com.example.orienteering.userAccess.onboarding.Validator;
import com.example.orienteering.userAccess.onboarding.responses.LoginFailResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NftManagerViewModel extends LoggedViewmodel {

    private UsersDao usersDao = UserDatabase.getInstance(getApplication()).usersDao();


    private MutableLiveData<List<AddressNftsData>> nftsList = new MutableLiveData<List<AddressNftsData>>();
    private MutableLiveData<String> currentNftName = new MutableLiveData<>("-");
    private MutableLiveData<String> currentNftDescription = new MutableLiveData<>("-");

    private MutableLiveData<NftFetchFailResponse> failResponse = new MutableLiveData<NftFetchFailResponse>();

    private MutableLiveData<Boolean> isLoading = new MutableLiveData<Boolean>();

    public NftManagerViewModel(@NonNull Application application) {
        super(application);
        //super.checkLogged(UserDatabase.getInstance(getApplication()));
    }

    public MutableLiveData<List<AddressNftsData>> getNftsList() {
        return nftsList;
    }

    public MutableLiveData<String> getCurrentNftName() {
        return currentNftName;
    }

    public MutableLiveData<NftFetchFailResponse> getFailResponse() {
        return failResponse;
    }

    public void setCurrentNftName(MutableLiveData<String> currentNftName) {
        this.currentNftName = currentNftName;
    }

    public MutableLiveData<String> getCurrentNftDescription() {
        return currentNftDescription;
    }

    public void setCurrentNftDescription(MutableLiveData<String> currentNftDescription) {
        this.currentNftDescription = currentNftDescription;
    }

    public void setNftsList(MutableLiveData<List<AddressNftsData>> nftsList) {
        this.nftsList = nftsList;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    //    @Override
//    public MutableLiveData<String> getUserId() {
//        return getUserId();
//    }
//
//    @Override
//    public MutableLiveData<String> getPickedAddress() {
//        return getPickedAddress();
//    }

    public void fetchNfts(){    // nacitaj nftcka zo servera na zaklade adresy

        if ((getPickedAddress().getValue() != null) && Validator.isAddressValid(getPickedAddress().getValue())){

            //adresa ok - nacitaj nftcka - retrofit
            isLoading.postValue(true);
            ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
            Call<AddressNftsResponse> call = apiInterface.getNftsByAddress(getPickedAddress().getValue());

            call.enqueue(new Callback<AddressNftsResponse>() {
                @Override
                public void onResponse(Call<AddressNftsResponse> call, Response<AddressNftsResponse> response) {

                    AddressNftsResponse nftsResponse = response.body();

                    if (response.code() == 500){

                        isLoading.postValue(false);
                        NftFetchFailResponse res = new NftFetchFailResponse();
                        res.setServerError(true);
                        res.setMessage(getApplication().getString(R.string.nft_man_server_msg));

                        failResponse.postValue(res);

                        Log.d("connection error", "smt went wrong");

                        //vypis chybu response.errorBody().string() - treba v try catch IO exception
                    }
                    else if(response.code() == 200){

                        isLoading.postValue(false);



                        if ((nftsResponse.getData() != null) && (!nftsResponse.getData().isEmpty())){
                            Log.d("nft img", nftsResponse.getData().get(0).getImageUrl());
                            nftsList.postValue(nftsResponse.getData());
                        }
                        else {

                            nftsList.postValue(new ArrayList<>());  //prazdny list
                        }

                    }
                    else {
                        Log.d("connection error", "smt went wrong");

                        isLoading.postValue(false);
                        NftFetchFailResponse res = new NftFetchFailResponse();
                        res.setServerError(true);
                        res.setMessage(getApplication().getString(R.string.nft_man_server_msg));

                        failResponse.postValue(res);

                    }

                }

                @Override
                public void onFailure(Call<AddressNftsResponse> call, Throwable t) {

                    //chyba spojenia toast
                    Log.d("connection error", "smt went wrong");
                    isLoading.postValue(false);

                    NftFetchFailResponse res = new NftFetchFailResponse();
                    res.setServerError(true);
                    res.setMessage(getApplication().getString(R.string.nft_man_server_msg));

                    failResponse.postValue(res);
                }
            });


        }


    }


    public void updateTexts(int index){

        try {
            currentNftName.postValue(getNftsList().getValue().get(index).getName());
            currentNftDescription.postValue(getNftsList().getValue().get(index).getDescription());
        }catch (NullPointerException ex){

            Log.e("Wrong indexing in nft manager change texts", "Null ptr exception");
        }

    }


}
