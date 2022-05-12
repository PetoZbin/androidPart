package com.example.orienteering.retrofit.patternClasses;

import com.google.gson.annotations.SerializedName;

public class AddressPattern {

    @SerializedName("userId")
    private String userId;
    @SerializedName("address")
    private String address;


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
