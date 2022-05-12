package com.example.orienteering.retrofit.responseClasses.login;

import com.example.orienteering.retrofit.responseClasses.CommonResponse;
import com.example.orienteering.retrofit.responseClasses.registration.RegistrationResponse;
import com.google.gson.annotations.SerializedName;

public class LoginResponse extends CommonResponse {


    @SerializedName("data")
    private LoginData data;

    public LoginData getData() {
        return data;
    }

    public void setData(LoginData data) {
        this.data = data;
    }

}
