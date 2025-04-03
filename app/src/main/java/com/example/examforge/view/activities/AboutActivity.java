package com.example.examforge.view.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.examforge.R;

public class AboutActivity extends AppCompatActivity {

    private Button btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        btnClose = findViewById(R.id.btnClose);

        btnClose.setOnClickListener(v -> {
            finish();  // Close the AboutActivity
        });
    }
}