package com.example.orienteering.retrofit.responseClasses.registration;

import com.example.orienteering.retrofit.responseClasses.CommonResponse;
import com.example.orienteering.retrofit.responseClasses.registration.RegistrationData;
import com.google.gson.annotations.SerializedName;

public class RegistrationResponse extends CommonResponse {

    @SerializedName("data")
    private RegistrationData data;

    public RegistrationData getData() {
        return data;
    }

    public void setData(RegistrationData data) {
        this.data = data;
    }
}
