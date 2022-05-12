package com.example.orienteering.userAccess.onboarding;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.orienteering.R;
import com.example.orienteering.dbWork.registration.UserCredentials;
import com.example.orienteering.dbWork.registration.UserDatabase;
import com.example.orienteering.dbWork.registration.UserRegistration;
import com.example.orienteering.dbWork.registration.UsersDao;
import com.example.orienteering.helpers.CipherHelper;
import com.example.orienteering.retrofit.ApiClient;
import com.example.orienteering.retrofit.ApiInterface;
import com.example.orienteering.retrofit.patternClasses.RegisterPattern;
import com.example.orienteering.retrofit.responseClasses.registration.RegistrationResponse;
import com.example.orienteering.userAccess.onboarding.responses.RegFailResponse;
import com.example.orienteering.userAccess.onboarding.responses.RegSuccessResponse;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrationViewModel extends AndroidViewModel {

    public static final String ADDRESS_DEF_MSG = "Insert private key first";

    private  MutableLiveData<String> userName = new MutableLiveData<String>("");
    private  MutableLiveData<String> password = new MutableLiveData<String>("");
    private  MutableLiveData<String> passwordAgain = new MutableLiveData<String>("");
    private  MutableLiveData<String> encryptionKey = new MutableLiveData<String>("");
    private  MutableLiveData<String> encryptionKeyAgain = new MutableLiveData<String>("");
    private  MutableLiveData<String> privateKey = new MutableLiveData<String>("");
    private  MutableLiveData<String> publicKey = new MutableLiveData<String>("");
    private  MutableLiveData<String> email = new MutableLiveData<String>("");
    private  MutableLiveData<String> address = new MutableLiveData<String>(ADDRESS_DEF_MSG);      //ma sa vypocitat z private key


    // pri uspesnej registracii nastavim hodnoty response, potom observujem
    private MutableLiveData<RegSuccessResponse> successResponse = new MutableLiveData<RegSuccessResponse>();
    private MutableLiveData<RegFailResponse> failResponse = new MutableLiveData<RegFailResponse>();



    private UsersDao usersDao = UserDatabase.getInstance(getApplication()).usersDao();

    public RegistrationViewModel(@NonNull Application application) {
        super(application);
    }


    public MutableLiveData<String> getUserName() {
        return userName;
    }

    public MutableLiveData<String> getPassword() {
        return password;
    }

    public MutableLiveData<String> getPasswordAgain() {
        return passwordAgain;
    }

    public MutableLiveData<String> getPrivateKey() {
        return privateKey;
    }

    public MutableLiveData<String> getPublicKey() {
        return publicKey;
    }

    public MutableLiveData<String> getAddress() {
        return address;
    }

    public MutableLiveData<String> getEmail() {
        return email;
    }

    public MutableLiveData<RegSuccessResponse> getSuccessResponse() {
        return successResponse;
    }

    public MutableLiveData<RegFailResponse> getFailResponse() {
        return failResponse;
    }

    public MutableLiveData<String> getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(MutableLiveData<String> encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public MutableLiveData<String> getEncryptionKeyAgain() {
        return encryptionKeyAgain;
    }

    public void setEncryptionKeyAgain(MutableLiveData<String> encryptionKeyAgain) {
        this.encryptionKeyAgain = encryptionKeyAgain;
    }

    public void register(){

        //register to portal

        if (checkCredentials() && (getEncryptionKey().getValue() != null) && (getPrivateKey().getValue() != null)){

            String ethPrivate = getPrivateKey().getValue();
            String encryptionKey = getEncryptionKey().getValue();

            RegisterPattern regData = new RegisterPattern();
            regData.setUserName(this.userName.getValue());
            regData.setPassword(this.password.getValue());
            regData.setEmail(this.email.getValue());
            regData.setEthAddress(this.address.getValue());

            ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
            Call<RegistrationResponse> call = apiInterface.postRegistration(regData);

            call.enqueue(new Callback<RegistrationResponse>() {
                @Override
                public void onResponse(Call<RegistrationResponse> call, Response<RegistrationResponse> response) {

                    RegistrationResponse regResponse = response.body();

                    if (response.code() == 500){

                        Log.d("connection error", "smt went wrong");
                        RegFailResponse fr = new RegFailResponse();
                        fr.setServerError(true);
                        fr.setMessage(getApplication().getResources().getString(R.string.reg_server_msg));
                        failResponse.postValue(fr);

                        //vypis chybu response.errorBody().string() - treba v try catch IO exception
                    }
                    else if (response.code() == 403){

                        RegFailResponse fr = new RegFailResponse();
                        fr.setServerError(true);
                        fr.setMessage(getApplication().getResources().getString(R.string.already_reg_lable));
                        failResponse.postValue(fr);

                    }
                    else if (response.code() == 400){

                        RegFailResponse fr = new RegFailResponse();
                        fr.setServerError(true);
                        fr.setMessage(getApplication().getResources().getString(R.string.val_incorrect_fields));
                        failResponse.postValue(fr);
                    }
                    else if(response.code() == 200){

                        //switchActivity(login) - uspesna registracia try catch tiez
                            Log.d("message", regResponse.getMessage());
                            Log.d("user_id", regResponse.getData().getUser_id());
                            Log.d("username", regResponse.getData().getUsername());
                            Log.d("email", regResponse.getData().getEmail());


                        String userId= regResponse.getData().getUser_id();
                        UserRegistration user = new UserRegistration();
                        user.setUserId(userId);
                        user.setLogged(false);
                        user.setUsername(regData.getUserName());
                        user.setEmail(regData.getEmail());
                        user.setToken("0000");
                        user.setEncryptionKey(CipherHelper.hashPassword(encryptionKey));

                        UserCredentials credentials = new UserCredentials();
                        credentials.setUserId(userId);
                        credentials.setAddress(regData.getEthAddress());
                        credentials.setPublicKey(publicKey.getValue());

                        //ukladanie i bez tychto atributov
                        try {
                            user.setPassword(CipherHelper.hashPassword(regData.getPassword()));
                            credentials.setPrivateHashed(CipherHelper.encryptByAes(encryptionKey, ethPrivate));
                        } catch (Exception e) {
                            Log.e("Encrypting private key:","AES encryption error");
                        }

                        if (saveUserLocally(user, credentials)){

                            //presmeruj na login
                            RegSuccessResponse sr = new RegSuccessResponse();
                            sr.setMessage(getApplication().getResources().getString(R.string.reg_success_msg));
                            sr.setUserName(user.username);
                            sr.setUserId(user.userId);
                            successResponse.postValue(sr);  // post value - observujem vo fragmente


                        }
                        else {
                            // failed to save to local db
                            RegFailResponse fr = new RegFailResponse();
                            fr.setDbFailed(true);
                            fr.setMessage(getApplication().getResources().getString(R.string.reg_roomdb_msg));
                            failResponse.postValue(fr);
                        }
                    }
                    else {
                        Log.d("connection error", "smt went wrong");
                        RegFailResponse fr = new RegFailResponse();
                        fr.setServerError(true);
                        fr.setMessage(getApplication().getResources().getString(R.string.reg_server_msg));
                        failResponse.postValue(fr);
                    }

                }

                @Override
                public void onFailure(Call<RegistrationResponse> call, Throwable t) {

                    //chyba spojenia toast
                    Log.d("connection error", "smt went wrong");
                    RegFailResponse fr = new RegFailResponse();
                    fr.setServerError(true);
                    fr.setMessage(getApplication().getResources().getString(R.string.reg_server_msg));

                    failResponse.postValue(fr);
                }
            });
        }
        else{

            RegFailResponse fr = new RegFailResponse();
            fr.setValidationError(true);
            fr.setMessage(getApplication().getResources().getString(R.string.val_incorrect_fields));

            failResponse.postValue(fr);
            Log.e("field validation error:","some fields not valid");
        }

    }


    private Boolean saveUserLocally(UserRegistration user, UserCredentials credentials){      //save user to database

        try {

            UserDatabase.getInstance(getApplication()).getTransactionExecutor().execute(

                    () -> {

                        usersDao.removeUserByUsername(user.getUsername());
                        usersDao.deleteCredentialsByAddress(credentials.getAddress());

                        usersDao.insertUser(user);
                        usersDao.insertCredentials(credentials);
                    }

            );
            return true;
        }catch (Exception e){

            return false;
        }
    }


    private Boolean checkCredentials(){     //skontroluj ci su korektne vstupy

        return (isLoginValid() && isPasswordValid(this.password, this.passwordAgain)
                && isPasswordValid(this.encryptionKey, this.encryptionKeyAgain)
                &&isPrivKeyValid() && isAddressValid() && isEmailValid());
    }

    private Boolean isPasswordValid(MutableLiveData<String> password, MutableLiveData<String> passwordAgain){

        String pattern = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}"; //8 znakov, male, velke pismeno, bez medzery

        try{

            if (!password.getValue().matches(pattern)){ // nesplna regex

                // oznac error do pola

                passwordAgain.setValue("");     //opakovane heslo vymaz
                return false;
            }

            if (!password.getValue().equals(passwordAgain.getValue())){

                // nezhodne hesla
                passwordAgain.setValue(""); //vymaz

                //oznac error


                return false;
            }

            return true;    // splna regex a heslo + heslo znova zhodne

        }catch (NullPointerException ex){
            password.postValue("");
            passwordAgain.postValue("");
            return false;
        }
    }


    private Boolean isLoginValid(){


        try{

            String pattern = "^\\d*[a-zA-Z][a-zA-Z0-9]*$";  //aspon jeden pismenovy character, pismena, cisla
            if (!userName.getValue().matches(pattern)){

                return false;
            }

            return true;

        }catch (NullPointerException ex){

            return false;
        }

    }

    private Boolean isPrivKeyValid(){

        if (!WalletUtils.isValidPrivateKey(privateKey.getValue())){

            //error - private key wrong format
            privateKey.setValue("");
            address.setValue(ADDRESS_DEF_MSG);
            return false;
        }

        updateCredentials();
        return true;
    }

    private Boolean isAddressValid(){

        return WalletUtils.isValidAddress(address.getValue());
    }

    private Boolean isEmailValid(){


        try{

            String pattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";

            if (!email.getValue().matches(pattern)){

                //error to textview
                return false;
            }

            return true;

        }catch (NullPointerException ex){

            return false;
        }


    }



    public void updateCredentials(){      //vyrata adresu z private kluca (HEX String), public key, nastavi

        String privateKey = this.privateKey.getValue(); // ziskam obsah pola z mutablelivedata, 2way databinding

//        try {
//
//
//        }catch (NullPointerException ex){
//
//            return; //no privkey
//        }


        if (!WalletUtils.isValidPrivateKey(privateKey)){

            Log.e("Private key", "invalid private key input");
            this.address.postValue(ADDRESS_DEF_MSG);
            return;     //nespravny private key, error msg to user
        }

        Credentials cred = Credentials.create(privateKey);

        String privKey = cred.getEcKeyPair().getPrivateKey().toString(16);      //16 - hexadecimal base
        String publicKey = cred.getEcKeyPair().getPublicKey().toString(16);
        String address = cred.getAddress();

        if (WalletUtils.isValidAddress(address)){        //ak je adresa validna, ukaz uzivatelovi

            Log.d("private key", privKey);
            Log.d("public key", publicKey);
            Log.d("address", address);
            this.publicKey.postValue(publicKey);
            this.address.postValue(address);
            System.out.println();
        }

       // return "";
    }
}
