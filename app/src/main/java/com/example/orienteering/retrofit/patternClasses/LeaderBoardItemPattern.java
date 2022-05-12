package com.example.orienteering.retrofit.patternClasses;

import com.google.gson.annotations.SerializedName;

import org.web3j.abi.datatypes.Int;

public class LeaderBoardItemPattern extends CompetitorPattern{

    @SerializedName("recordId")
    String id;

//    @SerializedName("competitionId")
//    String competitionId;

    @SerializedName("competitorId")
    String competitorId;

    @SerializedName("waypointId")
    String waypointId;

    @SerializedName("arrivalTime")
    private String arrivalTime;         //pouvazovat na zmenu na timestamp rovno tu

    @SerializedName("standing")
    private String standing;

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getStanding() {
        return standing;
    }

    public void setStanding(String standing) {
        this.standing = standing;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWaypointId() {
        return waypointId;
    }

    public void setWaypointId(String waypointId) {
        this.waypointId = waypointId;
    }

//    public Long getArrivalTimeMillis(){
//
//
//    }

}
