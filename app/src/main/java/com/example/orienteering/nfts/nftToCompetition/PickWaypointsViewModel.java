package com.example.orienteering.nfts.nftToCompetition;

import android.app.Application;
import android.location.Address;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.orienteering.commonClasses.LoggedViewmodel;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class PickWaypointsViewModel extends LoggedViewmodel {

    public static final int NO_POS = -1;
    private MutableLiveData<Location> current_location = new MutableLiveData<Location>();
    private MutableLiveData<List<LatLng>> wayPoints = new MutableLiveData<List<LatLng>>( new ArrayList<LatLng>());

    public PickWaypointsViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<Location> getCurrent_location() {
        return current_location;
    }

    public void setCurrent_location(MutableLiveData<Location> current_location) {
        this.current_location = current_location;
    }

    public MutableLiveData<List<LatLng>> getWayPoints() {
        return wayPoints;
    }

    public void setWayPoints(MutableLiveData<List<LatLng>> wayPoints) {
        this.wayPoints = wayPoints;
    }

    public Boolean addWaypoint(LatLng waypoint){

        List<LatLng> waypoints = this.wayPoints.getValue();

        if ((waypoints != null) && (getWayPointIndex(waypoint) == NO_POS)){ //este nie je rovnkaky waypoint

            waypoints.add(waypoint);
            this.wayPoints.postValue(waypoints);
            return true;
        }

        return false;
    }

    public Boolean removeWaypoint(LatLng waypoint){

        List<LatLng> waypoints = this.wayPoints.getValue();

        if ((waypoints != null)){

            int toRemoveIndex = getWayPointIndex(waypoint);

            if (toRemoveIndex != NO_POS){
                waypoints.remove(toRemoveIndex);
                this.wayPoints.postValue(waypoints);
                return true;
            }
        }

        return false;
    }

    public int getWayPointIndex(LatLng newWaypoint){

        if (this.wayPoints.getValue() != null){

            for (int i=0; i<this.wayPoints.getValue().size(); i++){

                LatLng wayPoint = this.wayPoints.getValue().get(i);

                if ((wayPoint.latitude == newWaypoint.latitude) && (wayPoint.longitude == newWaypoint.longitude)){

                    return i;
                }
            }
        }
        return NO_POS;
    }


    public List<LatLng> getWaypoints(){

        return this.wayPoints.getValue();
    }

}
