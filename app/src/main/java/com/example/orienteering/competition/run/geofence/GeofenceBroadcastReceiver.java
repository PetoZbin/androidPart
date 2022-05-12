package com.example.orienteering.competition.run.geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Toast.makeText(context,"Waypoint reached", Toast.LENGTH_SHORT).show();

        GeofencingEvent event = GeofencingEvent.fromIntent(intent);

        if (event.hasError()){

            Log.e("Broadcast receiver: ", "Geofencing event error");
            return;
        }

        List<Geofence> triggeredGeofences = event.getTriggeringGeofences();

        int transitionType = event.getGeofenceTransition(); //typ - ENTER, EXIT , DWELL

        switch (transitionType){

            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Toast.makeText(context,"Entered waypoint", Toast.LENGTH_SHORT).show();
                Log.e("Broadcast receiver: ", "Entered waypoint");
                notifyOnServer();
                break;

            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Toast.makeText(context,"inside waypoint", Toast.LENGTH_SHORT).show();
                Log.e("Broadcast receiver: ", "inside waypoint");
                break;

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Toast.makeText(context,"Left waypoint", Toast.LENGTH_SHORT).show();
                Log.e("Broadcast receiver: ", "Left waypoint");
                break;
        }

    }


    private void notifyOnServer(){  // nitifikuj server, ze uzivatel dorazil na waypoint

        //competitionId, competitorId, waypointNum, current time


    }
}