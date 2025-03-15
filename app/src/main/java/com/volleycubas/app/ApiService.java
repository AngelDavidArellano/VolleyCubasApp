package com.volleycubas.app;

import com.volleycubas.app.Clasificacion;
import com.volleycubas.app.Liga;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("getLigas")
    Call<List<Liga>> getLigas();

    @GET("getClasificacion")
    Call<List<Clasificacion>> getClasificacion(@Query("grupoId") String grupoId);
}
