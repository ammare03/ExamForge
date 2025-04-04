package com.example.examforge.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.example.examforge.R;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 3000; // 3 seconds total delay
    private static final long LOGO_DISPLAY_TIME = 2000; // 2 seconds for logo display
    private static final long PROGRESS_DISPLAY_TIME = 1000; // 1 second for progress bar
    
    private View ivLogo;
    private View progressBar;
    private View tvAppName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ivLogo = findViewById(R.id.ivLogo);
        progressBar = findViewById(R.id.progressBar);
        tvAppName = findViewById(R.id.tvAppName);

        // Load fade out and fade in animations from res/anim/fade_out.xml and fade_in.xml
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        // Start with logo visible and progress bar invisible
        ivLogo.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        
        // Animate the app name fade-in
        tvAppName.startAnimation(fadeIn);

        // Show the logo for LOGO_DISPLAY_TIME before showing progress bar
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Start fade-out on the logo
            ivLogo.startAnimation(fadeOut);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) { }
                @Override
                public void onAnimationEnd(Animation animation) {
                    // Logo fades out but stays visible with lower opacity
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.startAnimation(fadeIn);
                }
                @Override
                public void onAnimationRepeat(Animation animation) { }
            });
        }, LOGO_DISPLAY_TIME);

        // After the delay, check authentication and navigate accordingly
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                startActivity(new Intent(SplashActivity.this, HomeActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();
        }, SPLASH_DELAY);
    }
}