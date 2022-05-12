package com.example.orienteering.retrofit.responseClasses.registration;

import com.google.gson.annotations.SerializedName;

public class RegistrationData {

    @SerializedName("username")
    private String username;

    @SerializedName("email")
    private String email;

    @SerializedName("userId")
    private String user_id;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
