package com.example.orienteering.notification;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.orienteering.MainActivity;
import com.example.orienteering.R;
import com.example.orienteering.dbWork.registration.UserDatabase;
import com.example.orienteering.dbWork.registration.UserRegistration;
import com.example.orienteering.retrofit.ApiClient;
import com.example.orienteering.retrofit.ApiInterface;
import com.example.orienteering.retrofit.patternClasses.NotifPattern;
import com.example.orienteering.retrofit.responseClasses.notifications.NotifResponse;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotifWorker extends Worker {

    private static final String TAG = "OrienteeringNotifWorker";
    private static final String NOTIF_SCHEDULED_CHANNEL_ID = "ORIENTEERING_SCHEDULED_NOTIF_CHANNEL";
    public static final int SCHEDULED_NOTIF_ID = 45678;
    public static final int SCHEDULED_REQ_CODE = 456789;

    public NotifWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {



            String loggedUserId = getLoggedUserId();

            if (loggedUserId!=null){

                return fetchAndNotify();

            }
            else {

                return Result.failure();
            }

    }

    private String getLoggedUserId(){

        UserRegistration user = UserDatabase.getInstance(getApplicationContext()).usersDao().getLoggedUser();

        if (user != null) {
            return user.getUserId();
        }
        else return null;
    }

    private Result fetchAndNotify(){

        String userId = getLoggedUserId();

        if (userId != null){

            Log.d(TAG,"fetching notifs for " + userId);

            ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
            Call<NotifResponse> call = apiInterface.getUserNotifications(userId);


            try {

                Response<NotifResponse> response =  call.execute();

                if (!response.isSuccessful()){

                    try {
                        Log.e(TAG, response.errorBody().string());

                    } catch (IOException e) {
                        Log.e(TAG,"Wrong err msg form");
                        return Result.failure();
                    }
                    return Result.failure();
                }
                else if (response.code() == 200){

                    for (NotifPattern notifItem : response.body().getData()){

                        notifyUser(notifItem);
                    }

                    Log.d(TAG,"Server successfully notified!");
                    return Result.success();
                }



            }catch (IOException ex){

                Log.d(TAG,"Network problem!");
                return Result.failure();
            }catch (NullPointerException ex){

                Log.d(TAG,"Null ptr exception!");
                return Result.failure();
            }
            catch (NumberFormatException ex){

                Log.d(TAG,"Notif id not int- parse exception!");
                return Result.failure();
            }
        }
        return Result.failure();
    }


    private void notifyUser(NotifPattern notifData){

        //vytvor notifikaciu s info
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            NotificationChannel notificationChannel =
                    new NotificationChannel(NOTIF_SCHEDULED_CHANNEL_ID, "OrienteeringSchedNotif.", NotificationManager.IMPORTANCE_DEFAULT);


            notificationManager.createNotificationChannel(notificationChannel);

        }

        Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent  = PendingIntent.getActivity(getApplicationContext(), SCHEDULED_REQ_CODE, intent1,0);

        // vytvor notifikaciu, ze dosiahol chechpoint
        Notification notification = new NotificationCompat.Builder(getApplicationContext(), NOTIF_SCHEDULED_CHANNEL_ID)
                .setContentTitle(notifData.getNotifHeading())
                .setContentText(notifData.getNotifText())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent).build();

        notificationManager.notify(Integer.parseInt(notifData.getNotifId()), notification);
    }
}
