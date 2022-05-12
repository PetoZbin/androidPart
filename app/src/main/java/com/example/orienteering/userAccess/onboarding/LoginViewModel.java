package com.example.orienteering.userAccess.onboarding;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.orienteering.R;
import com.example.orienteering.commonClasses.LoggedViewmodel;
import com.example.orienteering.dbWork.registration.UserCredentials;
import com.example.orienteering.dbWork.registration.UserDatabase;
import com.example.orienteering.dbWork.registration.UserRegistration;
import com.example.orienteering.dbWork.registration.UserWithCredentials;
import com.example.orienteering.dbWork.registration.UsersDao;
import com.example.orienteering.helpers.CipherHelper;
import com.example.orienteering.retrofit.ApiClient;
import com.example.orienteering.retrofit.ApiInterface;
import com.example.orienteering.retrofit.patternClasses.LoginPattern;
import com.example.orienteering.retrofit.responseClasses.login.LoginResponse;
import com.example.orienteering.retrofit.responseClasses.registration.RegistrationResponse;
import com.example.orienteering.userAccess.onboarding.responses.LoginFailResponse;
import com.example.orienteering.userAccess.onboarding.responses.LoginSuccessResponse;
import com.example.orienteering.userAccess.onboarding.responses.RegFailResponse;
import com.example.orienteering.userAccess.onboarding.responses.RegSuccessResponse;

import org.web3j.abi.datatypes.Bool;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginViewModel extends LoggedViewmodel {

    private MutableLiveData<String> userName = new MutableLiveData<String>("");
    private  MutableLiveData<String> password = new MutableLiveData<String>("");

    private MutableLiveData<Boolean> hasLoggedAndPicked = new MutableLiveData<Boolean>();
    private  MutableLiveData<Boolean> isOnlyLogged = new MutableLiveData<Boolean>();

    private MutableLiveData<LoginSuccessResponse> loginSucessResponse = new MutableLiveData<LoginSuccessResponse>();
    private MutableLiveData<LoginFailResponse> loginFailResponse = new MutableLiveData<LoginFailResponse>();

    public LoginViewModel(@NonNull Application application) {

        super(application);
        //checkLogged(UserDatabase.getInstance(getApplication()));
    }

    public MutableLiveData<String> getUserName() {
        return userName;
    }

    public MutableLiveData<String> getPassword() {
        return password;
    }

    public void setUserName(MutableLiveData<String> userName) {
        this.userName = userName;
    }

    public void setPassword(MutableLiveData<String> password) {
        this.password = password;
    }

    public MutableLiveData<Boolean> getHasLoggedAndPicked() {
        return hasLoggedAndPicked;
    }

    public void setHasLoggedAndPicked(MutableLiveData<Boolean> hasAccountPicked) {
        this.hasLoggedAndPicked = hasAccountPicked;
    }

    public MutableLiveData<Boolean> getIsOnlyLogged() {
        return isOnlyLogged;
    }

    public void setIsOnlyLogged(MutableLiveData<Boolean> isLogged) {
        this.isOnlyLogged = isLogged;
    }

    public MutableLiveData<LoginSuccessResponse> getLoginSucessResponse() {
        return loginSucessResponse;
    }

    public void setLoginSucessResponse(MutableLiveData<LoginSuccessResponse> loginSucessResponse) {
        this.loginSucessResponse = loginSucessResponse;
    }

    public MutableLiveData<LoginFailResponse> getLoginFailResponse() {
        return loginFailResponse;
    }

    public void setLoginFailResponse(MutableLiveData<LoginFailResponse> loginFailResponse) {
        this.loginFailResponse = loginFailResponse;
    }


    private final UsersDao usersDao = UserDatabase.getInstance(getApplication()).usersDao();

    public UsersDao getUsersDao() {
        return usersDao;
    }

    public void login(){        // na stlacenie login - prihlasenie

        String username = this.userName.getValue();
        String password = this.password.getValue();

        if ((password == null) || !Validator.isPasswordValid(password)){

            //TODO error response
            return;
        }

        if ((username == null) || !Validator.isLoginValid(username)){

            //TODO error response
            return;
        }


        LoginPattern loginData = new LoginPattern();
        loginData.setUserName(username);
        loginData.setPassword(password);

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<LoginResponse> call = apiInterface.postLogin(loginData);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {

                LoginResponse loginResponse = response.body();

                if (response.code() == 500){

                    LoginFailResponse res = new LoginFailResponse();
                    res.setServerError(true);
                    res.setMessage(getApplication().getResources().getString(R.string.reg_server_msg));
                    loginFailResponse.postValue(res);

                    //TODO parsovanie chyb
                    Log.d("connection error", "smt went wrong");

                    //vypis chybu response.errorBody().string() - treba v try catch IO exception
                }
                else if (response.code() == 403){

                    LoginFailResponse res = new LoginFailResponse();
                    res.setServerError(true);
                    res.setMessage(getApplication().getResources().getString(R.string.login_incorrect_username_password));
                    loginFailResponse.postValue(res);

                    //vypis chybu response.errorBody().string() - treba v try catch IO exception
                }
                else if (response.code() == 400){

                    LoginFailResponse res = new LoginFailResponse();
                    res.setServerError(true);
                    res.setMessage(getApplication().getResources().getString(R.string.login_incorrect_inputs));
                    loginFailResponse.postValue(res);

                    //vypis chybu response.errorBody().string() - treba v try catch IO exception
                }
                else if(response.code() == 200){

                    //switchActivity(login) - uspesna registracia try catch tiez
                    Log.d("message", loginResponse.getMessage());
                    Log.d("user_id", loginResponse.getData().getUser_id());
                    Log.d("username", loginResponse.getData().getUsername());
                    Log.d("token", loginResponse.getData().getToken());

                    //skontroluj ci uz je user v db

                    UserRegistration user = new UserRegistration();
                    user.setUserId(loginResponse.getData().getUser_id());
                    user.setUsername(loginResponse.getData().getUsername());
                    user.setLogged(true);
                    user.setToken(loginResponse.getData().getToken());
                    user.setEmail(loginResponse.getData().getEmail());
                    user.setPassword(password);
                    checkInDb(user);

                }
                else {
                    Log.d("connection error", "smt went wrong");

                    LoginFailResponse res = new LoginFailResponse();
                    res.setServerError(true);
                    res.setMessage(getApplication().getResources().getString(R.string.reg_server_msg));
                    loginFailResponse.postValue(res);

                }

            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {


                if (t instanceof IOException){
                    //chyba spojenia toast
                    LoginFailResponse res = new LoginFailResponse();
                    res.setServerError(true);
                    res.setMessage(getApplication().getResources().getString(R.string.common_con_error));
                    loginFailResponse.postValue(res);
                }


                Log.d("connection error", "smt went wrong");
                LoginFailResponse res = new LoginFailResponse();
                res.setServerError(true);
                res.setMessage(getApplication().getResources().getString(R.string.common_app_error));
                loginFailResponse.postValue(res);
            }
        });

    }

    private Boolean logUserIn(String userId, String token){      //nastav logged priznak pre uzivatela

        try {

            UserDatabase.getInstance(getApplication()).getTransactionExecutor().execute(

                    () -> {
                        usersDao.logAllOut();   //odhlas vsetkych inych
                        usersDao.logUserIn(userId, token); //prihlas tohto uzivatela
                    }

            );
            return true;
        }catch (Exception e){

            return false;
        }
    }

    private void checkInDb(UserRegistration user){   //pozri, ci je uzivatel v databaze

        UserDatabase.getInstance(getApplication()).getTransactionExecutor().execute(

                () -> {

                    Boolean userExists = usersDao.isUserInTable(user.getUserId());

                    if (userExists){   // uz je v db v zariadeni

                       if(logUserIn(user.userId, user.getToken())){

                           updateSuccessResponse(user, getApplication().getString(R.string.login_logged_message));
                       }
                       else updateFailResponse(getApplication().getResources().getString(R.string.reg_roomdb_msg));
                    }
                    else {

                        //musim vytvorit uzivatela - je registrovany ale nie je v db v zariadeni

                        String hashedPassword = CipherHelper.hashPassword(user.getPassword());

                        usersDao.insertUser(user);
                        //uz som ho vytvoril - prihlas ho - improvement - check ci uz ma ucet na danom zariadeni

                        if (logUserIn(user.getUserId(), user.getToken())){
                            updateSuccessResponse(user, getApplication().getString(R.string.login_newuser_logged_message));
                        }
                        else updateFailResponse(getApplication().getResources().getString(R.string.reg_roomdb_msg));
                    }
                }

        );

    }

//    public void checkLogged(){
//
//        //zisti, ci je nejaky uzivatel uz prihlaseny
//        UserDatabase.getInstance(getApplication()).getTransactionExecutor().execute(
//
//                () -> {
//
//                    List<UserRegistration> loggedUsers = usersDao.getLoggedUsers();
//
//                    if (loggedUsers.size() != 1){   //ak nastala chyba a nahodou su viaceri prihlaseni
//
//                        usersDao.logAllOut();   //odhlas vsetkych
//                    }
//                    else {
//
//                        //mam prihlaseneho uzivatela - jedneho
//                        updateSuccessResponse(loggedUsers.get(0), getApplication().getString(R.string.login_logged_user_found));
//                    }
//                }
//        );
//    }


    private void updateSuccessResponse(UserRegistration user, String message){      // vysli response ze uzivatel uspesne prihlaseny

        LoginSuccessResponse res = new LoginSuccessResponse();
        res.setMessage(message);
        res.setUserId(user.getUserId());
        res.setToken(user.getToken());

        loginSucessResponse.postValue(res); //observujem, ze prihlaseny
    }

    private void updateFailResponse(String message){      // vysli response ze uzivatel uspesne prihlaseny

        LoginFailResponse res = new LoginFailResponse();
        res.setServerError(true);
        res.setMessage(message);
        loginFailResponse.postValue(res);
    }



    public void checkUserWithAccountPicked(){

        UserDatabase db = UserDatabase.getInstance(getApplication());

        db.getTransactionExecutor().execute(

            () -> {
                String userId = getLoggedUserId(db);

                if ((userId != null)){

                   String account = getLoggedUserAccount(db, userId);

                   if (account != null){
                       hasLoggedAndPicked.postValue(true);
                   }
                   else {
                       isOnlyLogged.postValue(true);
                   }
                }
            });

    }

}
