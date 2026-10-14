package com.example.realtimedeliveryandoperationstracker;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.Call;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
public class ApiClient {
    private static final String BASE_URL = "https://api.example.com/";
    private static Retrofit retrofit = null;

    public interface JobApiService{
        @POST("jobs/create")
        Call<Job> createJob(@Body Job job);
    }
    public static Retrofit getClient() {
        if (retrofit == null) {
        OkHttpClient okhttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30,TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request originalRequest = chain.request();
                    Request requestWithHeaders = originalRequest.newBuilder()
                            .header("Content-Type", "application/json")
                            .header("X-Request-ID", UUID.randomUUID().toString())
                            .build();

                    int tryCount = 0;
                    int maxRetries = 3;
                    Response response = null;
                    boolean responseOk = false;

                    while(tryCount < maxRetries && !responseOk){
                        try{
                        response = chain.proceed(requestWithHeaders);
                        responseOk = response.isSuccessful();
                        }catch(IOException e){
                            tryCount++;
                            if(tryCount >= maxRetries) throw e;
                        }
                        }
                    return response;
                    }).build();
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okhttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
                     return retrofit;
                    }
                }

