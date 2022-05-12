package com.example.orienteering.userAccess.onboarding.encKeySetup;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.orienteering.R;
import com.example.orienteering.dbWork.registration.UserDatabase;
import com.example.orienteering.dbWork.registration.UserRegistration;
import com.example.orienteering.helpers.CipherHelper;
import com.example.orienteering.helpers.ValidationHelper;
import com.example.orienteering.retrofit.responseClasses.CommonResponse;

public class EncKeySetupViewmodel extends AndroidViewModel {


    private MutableLiveData<String> encKey = new MutableLiveData<String>("");
    private MutableLiveData<String> encKeyAgain = new MutableLiveData<String>("");
    private MutableLiveData<CommonResponse> errorResponse = new MutableLiveData<CommonResponse>();
    private MutableLiveData<CommonResponse> successResponse = new MutableLiveData<CommonResponse>();

    public EncKeySetupViewmodel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<String> getEncKey() {
        return encKey;
    }

    public MutableLiveData<String> getEncKeyAgain() {
        return encKeyAgain;
    }

    public MutableLiveData<CommonResponse> getErrorResponse() {
        return errorResponse;
    }

    public MutableLiveData<CommonResponse> getSuccessResponse() {
        return successResponse;
    }

    public void persistEncKey(){

        if ((encKeyAgain.getValue() == null) || (encKey.getValue() == null)){
            Log.e("enc key setup", "null enc key value");
            return;
        }

        if (!ValidationHelper.isPasswordValid(encKey.getValue())){
            setErrResponse(getApplication().getString(R.string.enckey_invalid));
            encKey.postValue("");
            encKeyAgain.postValue("");
            return;
        }

        if (!encKey.getValue().equals(encKeyAgain.getValue())){
            setErrResponse(getApplication().getString(R.string.enckey_not_matching));
            encKey.postValue("");
            encKeyAgain.postValue("");
            return;
        }

        //uloz
        UserDatabase.getInstance(getApplication()).getTransactionExecutor().execute(
            () -> {

                try {

                    UserRegistration user = UserDatabase.getInstance(getApplication()).usersDao().getLoggedUser();

                    if (user.getEncryptionKey() == null){   // este nevyplnene

                        user.setEncryptionKey(CipherHelper.hashPassword(encKey.getValue()));

                        UserDatabase.getInstance(getApplication()).usersDao().updateUser(user);

                        CommonResponse res = new CommonResponse();
                        res.setSuccess(true);
                        res.setMessage(getApplication().getString(R.string.enckey_set));
                        successResponse.postValue(res);
                    }
                    else {
                        //nekonzistentna DB
                        setErrResponse(getApplication().getString(R.string.enckey_already_set_errr));
                    }


                }catch (Exception ex){
                    Log.e("acc adding  ex", "executor ex");
                }

            });


    }

    private void setErrResponse(String message){

        CommonResponse res = new CommonResponse();
        res.setSuccess(false);
        res.setMessage(message);

        errorResponse.postValue(res);
    }

}
