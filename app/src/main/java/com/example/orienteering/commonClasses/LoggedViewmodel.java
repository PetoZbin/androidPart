package com.example.orienteering.commonClasses;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.orienteering.R;
import com.example.orienteering.dbWork.registration.UserDatabase;
import com.example.orienteering.dbWork.registration.UserRegistration;
import com.example.orienteering.dbWork.registration.UsersDao;

import java.util.List;

public abstract class LoggedViewmodel extends AndroidViewModel {

    //protected String userId;
    //protected String pickedAddress;
    protected MutableLiveData<String> userId = new MutableLiveData<String>();
    protected MutableLiveData<String> pickedAddress = new MutableLiveData<String>();

    public LoggedViewmodel(@NonNull Application application) {
        super(application);
        //checkLogged(UserDatabase.getInstance(application)); // ak je lognuty uzivatel - nacitaj userId, nacitaj zvolenu adresu
    }

    public MutableLiveData<String> getUserId() {
        return userId;
    }

    public MutableLiveData<String> getPickedAddress() {
        return pickedAddress;
    }

    public void checkLogged(UserDatabase dbInstance){

        //zisti, ci je nejaky uzivatel uz prihlaseny
        dbInstance.getTransactionExecutor().execute(

                () -> {

                    String userId = getLoggedUserId(dbInstance);

                    if (userId != null){

                        this.userId.postValue(userId);
                        checkPicked(dbInstance, userId); // vyhladaj uzivatelov  zvoleny ucet (da sa volit z viacero uctov)
                    }

                    });
    }




    public String getLoggedUserId(UserDatabase dbInstance){

        List<UserRegistration> loggedUsers = dbInstance.usersDao().getLoggedUsers();

        if (loggedUsers.size() != 1){   //ak nastala chyba a nahodou su viaceri prihlaseni

            dbInstance.usersDao().logAllOut();   //odhlas vsetkych
            return null;
        }
        else {
            //mam prihlaseneho uzivatela - jedneho
            return loggedUsers.get(0).getUserId();
        }
    }

    public String getLoggedUserAccount(UserDatabase dbInstance, String userId){


        int num_addr_picked = dbInstance.usersDao().countAccountsPicked(userId);

        if (num_addr_picked != 1){   //viac uctov picked

            dbInstance.usersDao().unpickAllUsersAccounts(userId);   //chyba - viac uctov vybratych - odstran priznaky
            return null;
        }
        else {

            //mam jeden vybraty ucet
            return dbInstance.usersDao().getPickedUsersAccount(userId);
        }
    }

    public void checkPicked(UserDatabase dbInstance, String userId){

        dbInstance.getTransactionExecutor().execute(

            () -> {

                String account = getLoggedUserAccount(dbInstance, userId);

                if (account != null){

                    this.pickedAddress.postValue(account);
                }
            });
    }

}
