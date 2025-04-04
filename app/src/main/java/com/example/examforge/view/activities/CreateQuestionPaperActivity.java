package com.example.examforge.view.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.examforge.R;
import com.example.examforge.manager.ChatGPTManager;
import com.example.examforge.model.QuestionPaper;
import com.example.examforge.repository.QuestionPaperHistoryRepository;
import com.example.examforge.utils.PDFGenerator;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;
import java.io.File;
import java.io.InputStream;

public class CreateQuestionPaperActivity extends AppCompatActivity {

    private static final int PICK_PDF_REQUEST = 1;
    private Button btnChoosePDF, btnSubmit;
    private TextView tvFileName;
    private EditText etMarks, etAdditionalParams, etFileName;
    private MaterialAutoCompleteTextView spinnerQuestionType;
    private Uri pdfUri;
    private String extractedText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        PDFBoxResourceLoader.init(getApplicationContext());
        setContentView(R.layout.activity_create_question_paper);

        btnChoosePDF = findViewById(R.id.btnChoosePDF);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvFileName = findViewById(R.id.tvFileName);
        etMarks = findViewById(R.id.etMarks);
        etAdditionalParams = findViewById(R.id.etAdditionalParams);
        etFileName = findViewById(R.id.etFileName);
        spinnerQuestionType = findViewById(R.id.spinnerQuestionType);

        
        String[] questionTypes = {"MCQ", "Subjective", "Mixed"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                questionTypes);
        spinnerQuestionType.setAdapter(adapter);
        
        
        spinnerQuestionType.setText(questionTypes[0], false);

        btnChoosePDF.setOnClickListener(v -> openFileChooser());

        btnSubmit.setOnClickListener(v -> {
            if (pdfUri == null) {
                Toast.makeText(CreateQuestionPaperActivity.this, "Please select a PDF file", Toast.LENGTH_SHORT).show();
                return;
            }
            String marks = etMarks.getText().toString().trim();
            if (marks.isEmpty()) {
                Toast.makeText(CreateQuestionPaperActivity.this, "Enter total marks", Toast.LENGTH_SHORT).show();
                return;
            }
            String additionalParams = etAdditionalParams.getText().toString().trim();
            String questionType = spinnerQuestionType.getText().toString();
            if (questionType.isEmpty()) {
                Toast.makeText(CreateQuestionPaperActivity.this, "Please select a question type", Toast.LENGTH_SHORT).show();
                return;
            }

            
            String userFileName = etFileName.getText().toString().trim();
            if (userFileName.isEmpty()) {
                userFileName = "GeneratedQuestionPaper"; 
            }
            userFileName = userFileName + ".pdf"; 

            Toast.makeText(CreateQuestionPaperActivity.this, "Generating question paper...", Toast.LENGTH_SHORT).show();

            ChatGPTManager chatGPTManager = new ChatGPTManager();
            String finalUserFileName = userFileName;
            chatGPTManager.generateQuestionPaper(extractedText, 33000, marks, questionType, additionalParams, new ChatGPTManager.ChatGPTCallback() {
                @Override
                public void onComplete(String combinedResponse) {
                    String finalOutput = "Total Marks: " + marks + "\n\n" + combinedResponse;
                    runOnUiThread(() -> {
                        try {
                            File pdfFile = PDFGenerator.generatePDF(CreateQuestionPaperActivity.this, finalOutput, finalUserFileName);
                            
                            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            
                            QuestionPaper paper = new QuestionPaper(finalUserFileName, pdfFile.getAbsolutePath(), System.currentTimeMillis(), uid);
                            
                            QuestionPaperHistoryRepository repository = new QuestionPaperHistoryRepository(CreateQuestionPaperActivity.this);
                            repository.insert(paper);

                            
                            Intent intent = new Intent(CreateQuestionPaperActivity.this, PreviewActivity.class);
                            intent.putExtra("filePath", pdfFile.getAbsolutePath());
                            intent.putExtra("title", finalUserFileName);
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(CreateQuestionPaperActivity.this, "Error generating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() ->
                            Toast.makeText(CreateQuestionPaperActivity.this, "API Error: " + error, Toast.LENGTH_LONG).show());
                }
            });
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), PICK_PDF_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            pdfUri = data.getData();
            String fileName = getFileName(pdfUri);
            tvFileName.setText(fileName);
            extractTextFromPDF(pdfUri);
        }
    }

    private String getFileName(Uri uri) {
        String result = "Selected File";
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                result = cursor.getString(nameIndex);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    private void extractTextFromPDF(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            PDDocument document = PDDocument.load(inputStream);
            PDFTextStripper stripper = new PDFTextStripper();
            extractedText = stripper.getText(document);
            document.close();
            Toast.makeText(this, "PDF text extracted successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error extracting PDF text", Toast.LENGTH_SHORT).show();
        }
    }
}