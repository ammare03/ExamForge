package com.example.examforge.view.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.examforge.R;
import java.io.File;

public class PreviewActivity extends AppCompatActivity {

    private Button btnViewPDF;
    private String pdfFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        btnViewPDF = findViewById(R.id.btnViewPDF);
        pdfFilePath = getIntent().getStringExtra("pdfFilePath");

        btnViewPDF.setOnClickListener(v -> {
            File pdfFile = new File(pdfFilePath);
            if (pdfFile.exists()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                // For Android 7.0 and above, consider using FileProvider.
                intent.setDataAndType(Uri.fromFile(pdfFile), "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(PreviewActivity.this, "No PDF viewer found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(PreviewActivity.this, "PDF file not found", Toast.LENGTH_SHORT).show();
            }
        });
    }
}