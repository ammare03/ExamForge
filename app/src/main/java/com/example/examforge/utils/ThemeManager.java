package com.example.examforge.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Utility class to manage app theme settings
 */
public class ThemeManager {
    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_DARK_MODE = "dark_mode";

    /**
     * Set the app theme based on saved preferences
     * @param context Application context
     */
    public static void applyTheme(Context context) {
        boolean isDarkMode = getDarkModeState(context);
        
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    /**
     * Toggle between light and dark theme
     * @param context Application context
     * @return The new dark mode state
     */
    public static boolean toggleDarkMode(Context context) {
        boolean currentState = getDarkModeState(context);
        boolean newState = !currentState;
        
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_DARK_MODE, newState).apply();
        
        if (newState) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        
        return newState;
    }

    /**
     * Get the current dark mode state
     * @param context Application context
     * @return True if dark mode is enabled, false otherwise
     */
    public static boolean getDarkModeState(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }
} 