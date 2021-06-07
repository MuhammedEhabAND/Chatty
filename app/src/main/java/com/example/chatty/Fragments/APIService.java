package com.example.chatty.Fragments;

import com.example.chatty.Notification.MyResponse;
import com.example.chatty.Notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:AAAAFuuS8ig:APA91bFVkvLsB8HtpLzBBb1eFPlmHX6TCzO0UbxXekyev0RCP6arFVlx6kdC6ZE-aBhWg5EVuQhtwaKZNPHqIaCHVTPgXeExabRQmXAeQiqVVuv7nJAG1yls9S_sh_UaTczSBMZzddWj"


            }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);

}
