package com.example.examforge.view.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.example.examforge.R;
import java.io.File;

public class PreviewActivity extends AppCompatActivity {

    private Button btnViewPDF, btnBackToHistory;
    private TextView tvPreviewLabel;
    private String pdfFilePath;
    private String pdfTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        btnViewPDF = findViewById(R.id.btnViewPDF);
        btnBackToHistory = findViewById(R.id.btnBackToHistory);
        tvPreviewLabel = findViewById(R.id.tvPreviewLabel);
        

        pdfFilePath = getIntent().getStringExtra("filePath");
        pdfTitle = getIntent().getStringExtra("title");
        
        if (pdfTitle != null) {
            tvPreviewLabel.setText("Your Question Paper \"" + pdfTitle + "\" is Ready!");
        }

        btnViewPDF.setOnClickListener(v -> {
            File pdfFile = new File(pdfFilePath);
            if (pdfFile.exists()) {
                Uri contentUri = FileProvider.getUriForFile(
                        PreviewActivity.this,
                        getPackageName() + ".provider",
                        pdfFile
                );
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(contentUri, "application/pdf");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(PreviewActivity.this, "No PDF viewer found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(PreviewActivity.this, "PDF file not found", Toast.LENGTH_SHORT).show();
            }
        });

        btnBackToHistory.setOnClickListener(v -> {
            Intent intent = new Intent(PreviewActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }
}