package com.example.examforge;

import android.app.Application;
import com.example.examforge.utils.ThemeManager;

public class ExamForgeApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        ThemeManager.applyTheme(this);
    }
} 