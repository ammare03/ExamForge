package com.example.examforge.view.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.examforge.R;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;
import java.io.InputStream;

public class CreateQuestionPaperActivity extends AppCompatActivity {

    private static final int PICK_PDF_REQUEST = 1;
    private Button btnChoosePDF, btnSubmit;
    private TextView tvFileName;
    private EditText etMarks, etAdditionalParams;
    private Spinner spinnerQuestionType;
    private Uri pdfUri;
    private String extractedText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_question_paper);

        // Initialize views
        btnChoosePDF = findViewById(R.id.btnChoosePDF);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvFileName = findViewById(R.id.tvFileName);
        etMarks = findViewById(R.id.etMarks);
        etAdditionalParams = findViewById(R.id.etAdditionalParams);
        spinnerQuestionType = findViewById(R.id.spinnerQuestionType);

        // Set up click listener to open PDF chooser
        btnChoosePDF.setOnClickListener(v -> openFileChooser());

        // Handle submission: validate inputs, then trigger API call (to be implemented)
        btnSubmit.setOnClickListener(v -> {
            if (pdfUri == null) {
                Toast.makeText(this, "Please select a PDF file", Toast.LENGTH_SHORT).show();
                return;
            }
            String marks = etMarks.getText().toString().trim();
            if (marks.isEmpty()) {
                Toast.makeText(this, "Enter total marks", Toast.LENGTH_SHORT).show();
                return;
            }
            // Collect additional parameters and the question type from the spinner if needed.
            // At this point, the extractedText (from the PDF) is available.
            Toast.makeText(this, "Generating question paper...", Toast.LENGTH_SHORT).show();

            // TODO: Use the extractedText, marks, spinner selection, and additional parameters to call your generative AI API.
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

    // Helper method to retrieve the file name from the Uri
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

    // Method to extract text from the selected PDF using PdfBox-Android
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