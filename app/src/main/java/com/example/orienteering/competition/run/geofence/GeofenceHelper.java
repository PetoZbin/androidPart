package com.example.orienteering.competition.run.geofence;


import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.model.LatLng;

public class GeofenceHelper extends ContextWrapper {

    // podla zdroja: https://www.youtube.com/watch?v=nmAtMqljH9M

    public static final String TAG = "GeofenceHelper";
    public static final int PEN_REQ_CODE = 12365;
    public PendingIntent pendingIntent;

    public GeofenceHelper(Context base) {
        super(base);
    }


    public GeofencingRequest getGeofencingRequest(Geofence geofence){

        return new GeofencingRequest.Builder()
                .addGeofence(geofence)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER) // ked vstupi uzivatel do oblasti - iniciuj event
                .build();
    }

    public Geofence getGeofence(String id, LatLng pos, float radius, int transitionType){

        //.setLoiteringDelay(5000)  //ca straveny v bode (start - dwell - exit)
        //na zaklade dokumentacie:  https://developer.android.com/training/location/geofencing

        return new Geofence.Builder()
                .setCircularRegion(pos.latitude, pos.longitude, radius)
                .setRequestId(id)
                .setTransitionTypes(transitionType)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();


    }

    public PendingIntent getPendingIntent(){

        if(this.pendingIntent != null){

            return this.pendingIntent;
        }

        //FLAG_UPDATE_CURRENT - rovnaky pending intent dostanem (od GeofenceClient) pre pridavanie, zrusenie geofence (getGeofence)
        //ak pen. intent existuje, nemaz, ponechaj stary a aktualizuj ho
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        this.pendingIntent = PendingIntent
                .getBroadcast(this, PEN_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return this.pendingIntent;
    }

    public String getErrorString(Exception ex){

        if (ex instanceof ApiException){

            ApiException apiException = (ApiException) ex;

            switch (apiException.getStatusCode()){

                case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                    return "GEOFENCE_NOT_AVAILABLE";

                case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                    return "GEOFENCE_TOO_MANY_GEOFENCES";

                case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                    return "GEOFENCE_TOO_MANY_PENDING_INTENTS";
            }

        }
        return ex.getLocalizedMessage();
    }

}
