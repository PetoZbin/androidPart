package com.example.orienteering.retrofit;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASEURL = "http://167.71.3.36/";//"http://192.168.127.1:4000/";//"http://192.168.56.1:4000/";

    private static Retrofit retrofit = null;    // retrfit 2.0 kniznica

    public static Retrofit getApiClient(){  //vytvori a vrati instanciu retrofit

        if(retrofit == null){


            final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .readTimeout(40, TimeUnit.SECONDS)
                    .connectTimeout(40, TimeUnit.SECONDS)
                    .build();


            GsonBuilder gsonBuilder = new GsonBuilder();    //parser
            gsonBuilder.setLenient();
            gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss");       //yyyy-MM-dd HH:mm:ss" podla potreby

            Gson gson = gsonBuilder.create();
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASEURL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(okHttpClient)
                    .build();
        }

        return retrofit;
    }

}
