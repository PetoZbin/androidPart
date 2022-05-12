package com.example.orienteering.userAccess.accountPicker;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.orienteering.R;
import com.example.orienteering.commonClasses.LoggedViewmodel;
import com.example.orienteering.dbWork.registration.UserCredentials;
import com.example.orienteering.dbWork.registration.UserDatabase;
import com.example.orienteering.dbWork.registration.UserRegistration;
import com.example.orienteering.dbWork.registration.UsersDao;
import com.example.orienteering.retrofit.ApiClient;
import com.example.orienteering.retrofit.ApiInterface;
import com.example.orienteering.retrofit.responseClasses.CommonResponse;
import com.example.orienteering.retrofit.responseClasses.login.LoginResponse;
import com.example.orienteering.userAccess.accountAdding.responses.AddingSysResponse;
import com.example.orienteering.userAccess.onboarding.responses.LoginFailResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountPickerViewModel extends  LoggedViewmodel {


   // private String userId;  // should be initialized On logged In
    private MutableLiveData<List<UserCredentials>> userAccounts = new MutableLiveData<List<UserCredentials>>();
    private MutableLiveData<Boolean> wasAccountPicked = new MutableLiveData<Boolean>();
    private MutableLiveData<AddingSysResponse> delResponse = new MutableLiveData<AddingSysResponse>();

    private MutableLiveData<Boolean> logoutEvent = new MutableLiveData<Boolean>(false);

    private UsersDao usersDao = UserDatabase.getInstance(getApplication()).usersDao();

    public AccountPickerViewModel(@NonNull Application application) {
        super(application);
        //super.checkLogged(UserDatabase.getInstance(getApplication())); // hned aj nacitaj prihlaseneho uzivatela
    }

    public MutableLiveData<List<UserCredentials>> getUserAccounts() {
        return userAccounts;
    }

    public MutableLiveData<Boolean> getWasAccountPicked() {
        return wasAccountPicked;
    }

    public MutableLiveData<AddingSysResponse> getDelResponse() {
        return delResponse;
    }

    public MutableLiveData<Boolean> getLogoutEvent() {
        return logoutEvent;
    }

    public void fetchAccounts(){   //nacitanie uctov z databazy

        UserDatabase.getInstance(getApplication()).getTransactionExecutor().execute(
                () -> {

                    List<UserCredentials> allAccounts = usersDao.getUserCredentials(this.userId.getValue());

                    if (allAccounts != null){

                        userAccounts.postValue(allAccounts);    //vloz polozky do listu
                    }
                    else {
                        userAccounts.postValue(new ArrayList<>());  // nic nenaslo - prazdny list
                    }
                }
        );
    }

    public void deleteAccount(int position){    //zmaz ucet aj na serveri aj interne v roomdb

        String userId = this.userId.getValue();
        String address = getUserAccounts().getValue().get(position).getAddress();

        //najskor zmaz na serveri
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<CommonResponse> call = apiInterface.deleteAddress(userId, address);

        call.enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {

                CommonResponse loginResponse = response.body();

                if (response.code() == 500){

                    //TODO parsovanie chyb
                    Log.d("connection error", "smt went wrong");
                    AddingSysResponse res = new AddingSysResponse();
                    res.setServerError(true);
                    res.setMessage(getApplication().getResources().getString(R.string.reg_server_msg));
                    delResponse.postValue(res);

                    //vypis chybu response.errorBody().string() - treba v try catch IO exception
                }
                else if(response.code() == 200){

                    //zmaz aj z local db
                    removeAccountLocal(position);

                }
                else {
                    Log.d("connection error", "smt went wrong");

                    AddingSysResponse res = new AddingSysResponse();
                    res.setServerError(true);
                    res.setMessage(getApplication().getResources().getString(R.string.reg_server_msg));
                    delResponse.postValue(res);

                }

            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {

                //chyba spojenia toast
                Log.d("connection error", "smt went wrong");

                AddingSysResponse res = new AddingSysResponse();
                res.setServerError(true);
                res.setMessage(getApplication().getResources().getString(R.string.reg_server_msg));
                delResponse.postValue(res);
            }
        });

    }





    public void pickAccount(int position){      // nastav v db ucet ako picked a vysli response, ze je vybraty

        try {

            Long accountId = userAccounts.getValue().get(position).getId();

            UserDatabase.getInstance(getApplication()).getTransactionExecutor().execute(
                    () -> {

                        usersDao.unpickAllUsersAccounts(this.userId.getValue());
                        usersDao.pickUsersAccount(this.userId.getValue(), accountId);
                        fetchAccounts();    //nacitaj ucty
                        wasAccountPicked.postValue(true);       //observovana hodnota
                    }
            );
        }catch (NullPointerException | IndexOutOfBoundsException e){

            Log.e("Nullptr exception in accpickerviewmodel", "Nullptr ex caused by item in accounts live data");
        }

    }

    private void removeAccountLocal(int position){  //zmazanie credentials z local db

        try {

            UserCredentials cred2remove = getUserAccounts().getValue().get(position);

            UserDatabase.getInstance(getApplication()).getTransactionExecutor().execute(

                    () -> {

                        usersDao.deleteCredentials(cred2remove);
                        fetchAccounts();
                    }
            );

        }catch (NullPointerException e){

            Log.e("acc picker null ptr exception", "Nullptr ex caused by item in accounts live data");

            AddingSysResponse res = new AddingSysResponse();
            res.setDbError(true);
            res.setMessage(getApplication().getResources().getString(R.string.acc_picker_roomdb_delete_error));
            delResponse.postValue(res);
        }


    }

    //TODO toto sa musi diat v login view modeli
    public void checkAccountPicked(){   // ak si v minulom spusteni uz vybral - preskoc vyber uctu

        //zisti, ci je nejaky uzivatel uz prihlaseny
        UserDatabase.getInstance(getApplication()).getTransactionExecutor().execute(

                () -> {

                    int num_picked = usersDao.countAccountsPicked(userId.getValue());

                    if (num_picked > 1){   //viac nez 1 - chyba

                        usersDao.unpickAllUsersAccounts(userId.getValue());
                        wasAccountPicked.postValue(false);
                    }
                    else if(num_picked == 1){   //prave jeden

                        wasAccountPicked.postValue(true);
                    }
                    else {

                        wasAccountPicked.postValue(false);  // ma ucty ale nevybrate
                    }
                    // else popremyslat ci nedam nieco
                }
        );
    }

    public void logout(){

        UserDatabase.getInstance(getApplication()).getTransactionExecutor().execute(

                () -> {
                    UserDatabase.getInstance(getApplication()).usersDao().logAllOut();
                    logoutEvent.postValue(true);
                }
        );

    }

}
