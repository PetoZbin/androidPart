package com.example.orienteering.wrappers;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.orienteering.R;
import com.example.orienteering.commonClasses.LoggedViewmodel;
import com.example.orienteering.dbWork.registration.UserDatabase;
import com.example.orienteering.dbWork.registration.UserRegistration;
import com.example.orienteering.dbWork.registration.UsersDao;

import java.util.List;

public class WrappingViewModel extends LoggedViewmodel {

    private MutableLiveData<Boolean> isLoggedOut = new MutableLiveData<Boolean>();

    public MutableLiveData<Boolean> getIsLoggedOut() {
        return isLoggedOut;
    }

    public void setIsLoggedOut(MutableLiveData<Boolean> isLoggedOut) {
        this.isLoggedOut = isLoggedOut;
    }

    public UsersDao getUsersDao() {
        return usersDao;
    }

    public WrappingViewModel(@NonNull Application application) {
        super(application);
        super.checkLogged(UserDatabase.getInstance(getApplication()));
    }

    private final UsersDao usersDao = UserDatabase.getInstance(getApplication()).usersDao();

    //logout, refresh


    public void logOut(){

        UserDatabase.getInstance(getApplication()).getTransactionExecutor().execute(

                () -> {
                    usersDao.logAllOut();   //odhlas vsetkych
                    isLoggedOut.postValue(true);
                }
        );


    }

}
