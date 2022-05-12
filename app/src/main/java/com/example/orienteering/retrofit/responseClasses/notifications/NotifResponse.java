package com.example.orienteering.retrofit.responseClasses.notifications;

import com.example.orienteering.retrofit.patternClasses.NotifPattern;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NotifResponse {

    @SerializedName("data")
    private List<NotifPattern> data;

    public List<NotifPattern> getData() {
        return data;
    }

    public void setData(List<NotifPattern> data) {
        this.data = data;
    }
}
