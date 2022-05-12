package com.example.orienteering.notification;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.util.Log;

import com.example.orienteering.dbWork.registration.UserDatabase;
import com.example.orienteering.dbWork.registration.UserRegistration;
import com.example.orienteering.retrofit.ApiClient;
import com.example.orienteering.retrofit.ApiInterface;
import com.example.orienteering.retrofit.responseClasses.notifications.NotifResponse;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("SpecifyJobSchedulerIdRange")
public class NotifJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

//    private static final String TAG = "OrienteeringNotifService";
//    private boolean jobCanceled = false;
//
//    @Override
//    public boolean onStartJob(JobParameters params) {
//
//
//
//        return true;    //true pouzijem, ked spustam dlhotrvajuce procesy (neprejde do spanku) - siet - server
//    }
//
//    private void doBackgroundJob(JobParameters parameters){
//
//        BasicConte
//
//    }
//
//    @Override
//    public boolean onStopJob(JobParameters params) {
//        return false;
//    }
//
//    class BasicContextRunnable implements Runnable {
//
//        private Context context;
//        private JobParameters params;
//
//        public BasicContextRunnable(Context context, JobParameters params) {
//            this.context = context;
//        }
//
//        public Context getContext() {
//            return context;
//        }
//
//        @Override
//        public void run() {
//
//            if (jobCanceled){
//
//                return;
//            }
//
//            fetchData(params);
//
//        }
//    }
//
//    private String getLoggedUserId(){
//
//        UserRegistration user = UserDatabase.getInstance(getApplication()).usersDao().getLoggedUser();
//
//        if (user != null) {
//            return user.getUserId();
//        }
//        else return null;
//    }
//
//    private void fetchData( final JobParameters  params){
//
//        String userId = getLoggedUserId();
//
//        if (userId != null){
//
//            Log.d(TAG,"fetching notifs for " + userId);
//
//            ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
//            Call<NotifResponse> call = apiInterface.getUserNotifications(userId);
//
//
//            call.enqueue(new Callback<NotifResponse>() {
//
//                @Override
//                public void onResponse(Call<NotifResponse> call, Response<NotifResponse> response) {
//
//
//                    if (!response.isSuccessful()){
//                        try {
//                            Log.e("Server update on left err: ", response.errorBody().string());
//
//                        } catch (IOException e) {
//                            Log.e("server left comp err message: ","Wrong err msg form");
//                        }
//
//                    }
//                    else if(response.code() == 200){
//
//                        Log.d("server left comp success: ","Server successfully notified!");
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<NotifResponse> call, Throwable t) {
//
//                    if (t instanceof IOException){
//
//                        Log.e(TAG, "Connection error");
//                    }
//
//                    Log.e(TAG, "other app error");
//                }
//            });
//
//        }
//
//    }
}
