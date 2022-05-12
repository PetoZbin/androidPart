package com.example.orienteering.crypto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Web3JClient {

    //URL uzla do mumbai testnet - polygon
    private static final String SPEEDY_NODE_URL = "https://speedy-nodes-nyc.moralis.io/7e552ee0a5c1d070a33196ac/polygon/mumbai";

    private static Web3j web3jClient = null;    // retrfit 2.0 kniznica

    public static Web3j getWeb3jClient() {  //vytvori a vrati instanciu retrofit

        if (web3jClient == null) {

            // url uzla (endpoint) na pristup ku sieti - pouzivam moralis uzol pre mumbai testnet - polygon
            web3jClient = Web3j.build(new HttpService(SPEEDY_NODE_URL));  // inicializacia web3j klienta - komunikacia s uzlom cez http
           // Web3ClientVersion web3ClientVersion = null;

        }
        return web3jClient;
    }
}
