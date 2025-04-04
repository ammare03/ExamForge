package com.example.examforge;

import android.app.Application;
import com.example.examforge.utils.ThemeManager;

public class ExamForgeApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Apply saved theme settings when app starts
        ThemeManager.applyTheme(this);
    }
} 