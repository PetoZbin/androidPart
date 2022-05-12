package com.example.orienteering.userAccess.accountAdding;

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
import com.example.orienteering.helpers.CipherHelper;
import com.example.orienteering.retrofit.ApiClient;
import com.example.orienteering.retrofit.ApiInterface;
import com.example.orienteering.retrofit.patternClasses.AddressPattern;
import com.example.orienteering.retrofit.responseClasses.CommonResponse;
import com.example.orienteering.userAccess.accountAdding.responses.AddingSysResponse;
import com.example.orienteering.userAccess.onboarding.Validator;
import com.example.orienteering.userAccess.onboarding.responses.LoginFailResponse;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountAddingViewModel extends LoggedViewmodel {

    private MutableLiveData<CommonResponse> errorResponse = new MutableLiveData<CommonResponse>();

    private MutableLiveData<String> privateKey = new MutableLiveData<String>("");
    private MutableLiveData<String> address = new MutableLiveData<String>(getApplication().getString(R.string.reg_pubkey_hint));
    private MutableLiveData<String> publicKey = new MutableLiveData<String>("");
    private MutableLiveData<String> password = new MutableLiveData<String>("");

    private final UsersDao usersDao = UserDatabase.getInstance(getApplication()).usersDao();

    //response classes
    //observujem ↓
    private MutableLiveData<AddingSysResponse> addingResponse = new MutableLiveData<AddingSysResponse>();

    public AccountAddingViewModel(@NonNull Application application) {
        super(application);
        super.checkLogged(UserDatabase.getInstance(getApplication())); // hned aj nacitaj prihlaseneho uzivatela
    }

    public MutableLiveData<String> getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(MutableLiveData<String> privateKey) {
        this.privateKey = privateKey;
    }

    public MutableLiveData<String> getAddress() {
        return address;
    }

    public void setAddress(MutableLiveData<String> address) {
        this.address = address;
    }

    public MutableLiveData<AddingSysResponse> getAddingResponse() {
        return addingResponse;
    }

    public MutableLiveData<String> getPassword() {
        return password;
    }

    public void setPassword(MutableLiveData<String> password) {
        this.password = password;
    }

    public MutableLiveData<CommonResponse> getErrorResponse() {
        return errorResponse;
    }

    public void addAccount(){       // pridaj ucet do serverovej aj local db

        String userId= this.userId.getValue();
        String privateKey = this.privateKey.getValue();
        String address = this.address.getValue();
        String encKey = this.password.getValue();

        if ((privateKey == null) ||(userId == null) ||  !Validator.isPrivKeyValid(privateKey) || !Validator.isAddressValid(address) || (encKey == null)){

            AddingSysResponse failResponse = new AddingSysResponse();
            failResponse.setPivateKeyInvalid(true);     //uz zabrany kluc
            failResponse.setKeyMessage(getApplication().getResources().getString(R.string.acc_adding_invalidKey));
            addingResponse.postValue(failResponse);
            return;
        }

        UserDatabase.getInstance(getApplication()).getTransactionExecutor().execute(
                () -> {

                    try {

                        UserRegistration user = UserDatabase.getInstance(getApplication()).usersDao().getLoggedUser();

                        if (user.getEncryptionKey() !=null){

                            //overenie spravnosti zadania sifrovacieho kluca
                            if (CipherHelper.verifyPassword(encKey, user.getEncryptionKey())){

                                saveAccountServer(this.userId.getValue(), address, privateKey, encKey);

                            }
                            else {
                                //NESPRAVNE HESLO
                                setErrResponse(getApplication().getString(R.string.val_incorrect_enc_key));
                            }

                        }
                        else {
                            //nema v db enc key
                            setErrResponse(getApplication().getString(R.string.acc_adding_no_enc_key));
                        }




                    }catch (Exception ex){
                        Log.e("acc adding  ex", "executor ex");
                    }

                });


        //UserRegistration currentUser = usersDao.getLoggedUser();



    }

    public void updateCredentials(){      //vyrata adresu z private kluca (HEX String), public key, nastavi

        String privateKey = this.privateKey.getValue(); // ziskam obsah pola z mutablelivedata, 2way databinding


        if (!WalletUtils.isValidPrivateKey(privateKey)){

            Log.e("Private key", "invalid private key input");
            this.address.postValue(getApplication().getString(R.string.reg_pubkey_hint));
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

    private void saveAccountServer(String userId, String address, String privateKey, String encKey){

        AddressPattern addressPattern = new AddressPattern();
        addressPattern.setUserId(userId);
        addressPattern.setAddress(address);

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<CommonResponse> call = apiInterface.postAddress(addressPattern);

        call.enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {

                CommonResponse res = response.body();

                if (response.code() == 500){

                    //TODO parsovanie chyb
                    Log.e("connection error", "smt went wrong");
                    AddingSysResponse failResponse = new AddingSysResponse();
                    failResponse.setServerError(true);
                    failResponse.setServerMessage(response.message());

                    addingResponse.postValue(failResponse);
                }
                else if (response.code() == 400){
                    //validacia - zle vstupy
                    setErrResponse("Check your inputs");

                }
                else if(response.code() ==403){
                    //forbidden - ucet uz existuje
                    setErrResponse(getApplication().getResources().getString(R.string.acc_adding_adress_used));
                }
                else if(response.code() == 200){

                    //switchActivity(login) - uspesna registracia try catch tiez
                    Log.d("message", res.getMessage());

                    //uloz  ucet do db ak este neexistuje
                    try {
                        saveAccountLocal(privateKey, userId, encKey);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
                    Log.d("connection error", "smt went wrong");

                    AddingSysResponse failResponse = new AddingSysResponse();
                    failResponse.setServerError(true);
                    failResponse.setServerMessage(getApplication().getResources().getString(R.string.common_con_error));
                    addingResponse.postValue(failResponse);

                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {

                //chyba spojenia
                AddingSysResponse failResponse = new AddingSysResponse();
                failResponse.setServerError(true);
                failResponse.setServerMessage(getApplication().getResources().getString(R.string.common_con_error));
                addingResponse.postValue(failResponse);
            }
        });

    }

    private void saveAccountLocal(String privateKey, String userId, String encKey) throws Exception {

        //encrypted private

        Credentials cred = Credentials.create(privateKey);
        UserCredentials credentials = new UserCredentials();
        credentials.setUserId(userId);
        credentials.setPrivateHashed(CipherHelper.encryptByAes(encKey,privateKey));
        credentials.setPublicKey(cred.getEcKeyPair().getPublicKey().toString(16));
        credentials.setAddress(cred.getAddress());


        UserDatabase.getInstance(getApplication()).getTransactionExecutor().execute(
                () -> {

                    //server pridal uspesne - vymazanie potencialnej duplicity credentials uctov pre inych uzivatelov
                    UserDatabase.getInstance(getApplication()).usersDao().deleteCredentialsByAddress(credentials.address);

                    //pridaj k danemu uzivatelovi
                    UserDatabase.getInstance(getApplication()).usersDao().insertCredentials(credentials);
                    AddingSysResponse successResponse = new AddingSysResponse();
                    successResponse.setMessage(getApplication().getString(R.string.acc_adding_success));
                    addingResponse.postValue(successResponse);

//stara implementacia
//                    if (!usersDao.checkAddressUsed(credentials.address)){   //ak adresa este nie je pouzivana
//
//                        usersDao.insertCredentials(credentials);
//                        AddingSysResponse successResponse = new AddingSysResponse();
//                        successResponse.setMessage(getApplication().getString(R.string.acc_adding_success));
//                        addingResponse.postValue(successResponse);
//
//                    }
//                    else {
//                        AddingSysResponse failResponse = new AddingSysResponse();
//                        failResponse.setPivateKeyInvalid(true);     //uz zabrany kluc
//                        failResponse.setKeyMessage(getApplication().getResources().getString(R.string.acc_adding_adress_used));
//                        addingResponse.postValue(failResponse);
//                    }
                }
        );
    }

    private void setErrResponse(String message){

        CommonResponse res = new CommonResponse();
        res.setSuccess(false);
        res.setMessage(message);

        errorResponse.postValue(res);
    }
}
