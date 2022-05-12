package com.example.orienteering.retrofit.patternClasses;

import com.google.gson.annotations.SerializedName;

public class RegisterPattern {

    @SerializedName("username")
    private String userName;
    @SerializedName("password")
    private String password;
    @SerializedName("address")
    private String ethAddress;
    @SerializedName("email")
    private String email;


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEthAddress() {
        return ethAddress;
    }

    public void setEthAddress(String ethAddress) {
        this.ethAddress = ethAddress;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
