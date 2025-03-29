package com.example.examforge.network;

import com.example.examforge.model.ChatCompletionRequest;
import com.example.examforge.model.ChatCompletionResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ChatGPTApiService {
    @Headers("Content-Type: application/json")
    @POST("chat/completions")
    Call<ChatCompletionResponse> getChatCompletion(@Body ChatCompletionRequest request);
}