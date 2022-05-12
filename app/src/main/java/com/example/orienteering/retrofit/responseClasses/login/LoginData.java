package com.example.orienteering.retrofit.responseClasses.login;

import com.example.orienteering.retrofit.responseClasses.registration.RegistrationData;
import com.google.gson.annotations.SerializedName;

public class LoginData extends RegistrationData {

    @SerializedName("token")    //auth jwt token
    private String token;


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
