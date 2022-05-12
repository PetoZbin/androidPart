package com.example.orienteering.retrofit.patternClasses;

import com.google.gson.annotations.SerializedName;

public class NotifPattern {

    @SerializedName("notifId")
    private String notifId;

    @SerializedName("userId")
    private String userId;

    @SerializedName("notifHeading")
    private String notifHeading;

    @SerializedName("notifText")
    private String notifText;

    @SerializedName("notifUrl")
    private String notifUrl;

    @SerializedName("timeCreated")      //sql datetime format
    private String timeCreated;

    public String getNotifId() {
        return notifId;
    }

    public void setNotifId(String notifId) {
        this.notifId = notifId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNotifHeading() {
        return notifHeading;
    }

    public void setNotifHeading(String notifHeading) {
        this.notifHeading = notifHeading;
    }

    public String getNotifText() {
        return notifText;
    }

    public void setNotifText(String notifText) {
        this.notifText = notifText;
    }

    public String getNotifUrl() {
        return notifUrl;
    }

    public void setNotifUrl(String notifUrl) {
        this.notifUrl = notifUrl;
    }

    public String getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(String timeCreated) {
        this.timeCreated = timeCreated;
    }
}
