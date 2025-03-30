package com.example.examforge.network;

import com.example.examforge.model.ChatGPTRequest;
import com.example.examforge.model.ChatGPTResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ChatGPTApiService {
    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    Call<ChatGPTResponse> getChatCompletion(@Body ChatGPTRequest request);
}