package com.example.orienteering.retrofit.responseClasses.competition;

import com.example.orienteering.nfts.nftToCompetition.CustomWaypointDesc;
import com.google.gson.annotations.SerializedName;

public class WaypointResponse {

    @SerializedName("data")
    private CustomWaypointDesc waypoint;


    public CustomWaypointDesc getWaypoint() {
        return waypoint;
    }

    public void setWaypoint(CustomWaypointDesc waypoint) {
        this.waypoint = waypoint;
    }


}
