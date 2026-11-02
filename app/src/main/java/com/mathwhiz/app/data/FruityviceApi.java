package com.mathwhiz.app.data;

import com.mathwhiz.app.model.FruitDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/** Fruityvice public API (no key). Base URL: https://www.fruityvice.com/api/ */
public interface FruityviceApi {
    @GET("fruit/all")
    Call<List<FruitDto>> getAll();

    @GET("fruit/{name}")
    Call<FruitDto> getFruit(@Path("name") String name);
}
