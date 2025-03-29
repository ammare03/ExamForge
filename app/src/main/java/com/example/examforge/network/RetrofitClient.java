package com.example.examforge.network;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;

public class RetrofitClient {
    private static Retrofit retrofit = null;
    // OpenAI API base URL
    private static final String BASE_URL = "https://api.openai.com/v1/";

    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request original = chain.request();
                            Request request = original.newBuilder()
                                    .header("Authorization", "Bearer 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")
                                    .method(original.method(), original.body())
                                    .build();
                            return chain.proceed(request);
                        }
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}