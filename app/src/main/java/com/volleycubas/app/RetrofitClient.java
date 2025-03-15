package com.volleycubas.app;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.volleycubas.app.ApiService;



public class RetrofitClient {
    private static final String BASE_URL = "https://us-central1-volley-cubas-app.cloudfunctions.net/";

    private static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public static ApiService getApiService() {
        return retrofit.create(ApiService.class);
    }
}
