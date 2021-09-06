/*
package com.example.chatapp.config;

import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class NetworkProvider {
    private static volatile NetworkProvider instance = null;

    private Retrofit retrofit;

    private NetworkProvider(){
        retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080")
                .client(new OkHttpClient.Builder().build())
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().create()))
                .build();
    }

    public static NetworkProvider getInstance(){
        if(instance == null)
            instance = new NetworkProvider();
        return instance;
    }

    private <T> T getService(Class<T> serviceClass){
        return retrofit.create(serviceClass);
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }
}
*/
