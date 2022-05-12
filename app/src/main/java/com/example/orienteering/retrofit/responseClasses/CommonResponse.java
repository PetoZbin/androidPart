package com.example.orienteering.retrofit.responseClasses;

import com.google.gson.annotations.SerializedName;

public class CommonResponse {

    @SerializedName("isSuccess")
    private Boolean isSuccess;

    @SerializedName("message")
    private String message;


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getSuccess() {
        return isSuccess;
    }

    public void setSuccess(Boolean success) {
        isSuccess = success;
    }
}
