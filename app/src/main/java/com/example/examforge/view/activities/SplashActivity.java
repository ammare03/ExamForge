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

    private static final long SPLASH_DELAY = 3000; 
    private static final long LOGO_DISPLAY_TIME = 2000; 
    private static final long PROGRESS_DISPLAY_TIME = 1000; 
    
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

        
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        
        ivLogo.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        
        
        tvAppName.startAnimation(fadeIn);

        
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            
            ivLogo.startAnimation(fadeOut);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) { }
                @Override
                public void onAnimationEnd(Animation animation) {
                    
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.startAnimation(fadeIn);
                }
                @Override
                public void onAnimationRepeat(Animation animation) { }
            });
        }, LOGO_DISPLAY_TIME);

        
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