package com.example.examforge.network;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;
import com.example.examforge.BuildConfig;

public class ChatGPTRetrofitClient {
    private static Retrofit retrofit = null;
    private static final String BASE_URL = "https://api.openai.com/";

    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request original = chain.request();
                            // Ensure your API key is stored securely in BuildConfig.CHATGPT_API_KEY
                            Request request = original.newBuilder()
                                    .header("Authorization", "Bearer " + BuildConfig.CHATGPT_API_KEY)
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