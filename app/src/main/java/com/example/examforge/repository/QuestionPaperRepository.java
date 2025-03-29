package com.example.examforge.repository;

import com.example.examforge.model.ChatCompletionRequest;
import com.example.examforge.model.ChatCompletionResponse;
import com.example.examforge.model.Message;
import com.example.examforge.network.ChatGPTApiService;
import com.example.examforge.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class QuestionPaperRepository {

    private ChatGPTApiService apiService;

    public QuestionPaperRepository() {
        apiService = RetrofitClient.getClient().create(ChatGPTApiService.class);
    }

    /**
     * Generates a question paper using ChatGPT.
     * @param extractedText The text extracted from the uploaded PDF.
     * @param marks Total marks.
     * @param questionType Selected question type.
     * @param additionalParams Additional parameters from the user.
     * @param callback Callback to deliver the generated text.
     */
    public void generateQuestionPaper(String extractedText, String marks, String questionType, String additionalParams, GenerationCallback callback) {
        // Construct a prompt that combines the PDF content and the user-specified parameters.
        String prompt = "Generate a question paper using the following study material:\n" + extractedText +
                "\nTotal Marks: " + marks +
                "\nQuestion Type: " + questionType +
                "\nAdditional Parameters: " + additionalParams;

        // Create a message with role 'user'
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("user", prompt));

        // Create the request using a specific model (e.g., gpt-3.5-turbo)
        ChatCompletionRequest request = new ChatCompletionRequest("gpt-3.5-turbo", messages);

        Call<ChatCompletionResponse> call = apiService.getChatCompletion(request);
        call.enqueue(new Callback<ChatCompletionResponse>() {
            @Override
            public void onResponse(Call<ChatCompletionResponse> call, Response<ChatCompletionResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().getChoices().isEmpty()) {
                    String generatedText = response.body().getChoices().get(0).getMessage().getContent();
                    callback.onSuccess(generatedText);
                } else {
                    callback.onFailure("API Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ChatCompletionResponse> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

    public interface GenerationCallback {
        void onSuccess(String generatedText);
        void onFailure(String error);
    }
}