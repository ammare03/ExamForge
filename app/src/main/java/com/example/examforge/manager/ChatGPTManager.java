package com.example.examforge.manager;

import android.util.Log;
import com.example.examforge.model.ChatGPTRequest;
import com.example.examforge.model.ChatGPTResponse;
import com.example.examforge.model.ChatMessage;
import com.example.examforge.network.ChatGPTRetrofitClient;
import com.example.examforge.network.ChatGPTApiService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatGPTManager {

    private static final String TAG = "ChatGPTManager";
    private ChatGPTApiService apiService;
    private List<ChatMessage> conversationHistory;

    public interface ChatGPTCallback {
        void onComplete(String combinedResponse);
        void onError(String error);
    }

    public ChatGPTManager() {
        apiService = ChatGPTRetrofitClient.getClient().create(ChatGPTApiService.class);
        conversationHistory = new ArrayList<>();
    }

    
    public void addUserMessage(String content) {
        conversationHistory.add(new ChatMessage("user", content));
    }

    public void addAssistantMessage(String content) {
        conversationHistory.add(new ChatMessage("assistant", content));
    }

    /**
     * Splits the extracted text into chunks and sends each chunk to the ChatGPT API with the desired prompt format.
     * The prompt for each chunk is built as:
     * "Create a question paper with the following content <chunk> that has <marks> marks, is of <questionType> type
     * and has these as the additional parameters: <additionalParams>"
     *
     * @param extractedText   The complete text extracted from the PDF.
     * @param chunkSize       The maximum number of characters per chunk.
     * @param marks           The total marks provided by the user.
     * @param questionType    The selected question type.
     * @param additionalParams Additional parameters entered by the user.
     * @param callback        Callback to deliver the combined generated text.
     */
    public void generateQuestionPaper(String extractedText, int chunkSize, String marks, String questionType, String additionalParams, ChatGPTCallback callback) {
        List<String> chunks = splitText(extractedText, chunkSize);
        List<String> responses = new ArrayList<>();
        processChunk(chunks, 0, responses, marks, questionType, additionalParams, callback);
    }

    
    private void processChunk(List<String> chunks, int index, List<String> responses, String marks, String questionType, String additionalParams, ChatGPTCallback callback) {
        if (index >= chunks.size()) {
            StringBuilder combined = new StringBuilder();
            for (String resp : responses) {
                combined.append(resp).append("\n\n");
            }
            callback.onComplete(combined.toString());
            
            conversationHistory.clear();
            return;
        }

        
        String currentChunk = chunks.get(index);
        
        String prompt = "Create a question paper with the following content " + currentChunk +
                " that has " + marks + " marks, is of " + questionType + " type and has these as the additional parameters: " + additionalParams;

        
        Log.d(TAG, "Processing chunk " + index + " with prompt: " + prompt);

        
        addUserMessage(prompt);

        
        ChatGPTRequest request = new ChatGPTRequest(
                "gpt-4o-mini", 
                new ArrayList<>(conversationHistory),
                300,   
                0.7    
        );

        apiService.getChatCompletion(request).enqueue(new Callback<ChatGPTResponse>() {
            @Override
            public void onResponse(Call<ChatGPTResponse> call, Response<ChatGPTResponse> response) {
                if (response.isSuccessful() && response.body() != null &&
                        response.body().getChoices() != null &&
                        !response.body().getChoices().isEmpty()) {
                    String generated = response.body().getChoices().get(0).getMessage().getContent();
                    
                    addAssistantMessage(generated);
                    responses.add(generated);
                    
                    processChunk(chunks, index + 1, responses, marks, questionType, additionalParams, callback);
                } else {
                    String errorMsg = "Error on chunk " + index + ": " + response.message();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ChatGPTResponse> call, Throwable t) {
                String errorMsg = "Failure on chunk " + index + ": " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    
    private List<String> splitText(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        int length = text.length();
        for (int i = 0; i < length; i += chunkSize) {
            int end = Math.min(length, i + chunkSize);
            chunks.add(text.substring(i, end));
        }
        return chunks;
    }
}