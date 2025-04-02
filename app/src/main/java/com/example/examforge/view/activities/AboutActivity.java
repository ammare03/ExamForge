package com.example.examforge.view.activities;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.examforge.R;

public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView tvAbout = findViewById(R.id.tvAbout);
        tvAbout.setText("ExamForge is an innovative application that generates question papers based on study material provided in PDF format. " +
                "It leverages advanced AI models to create diverse and tailored questions according to user-defined parameters. " +
                "The app also offers features such as user profile management and a history of generated papers for easy access.");
    }
}