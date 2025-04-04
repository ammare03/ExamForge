package com.example.examforge.view.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.examforge.R;
import com.example.examforge.model.User;
import com.example.examforge.repository.UserRepository;
import com.example.examforge.utils.FirebaseManager;
import com.example.examforge.utils.ImageStorageManager;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;

public class SignUpActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnSignUp;
    private ShapeableImageView ivProfilePicture;
    private FirebaseAuth mAuth;
    private TextView loginLink;
    private Uri selectedImageUri;
    private UserRepository userRepository;
    
    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                selectedImageUri = result.getData().getData();
                if (selectedImageUri != null) {
                    // Use Glide to load the image to avoid permission issues
                    Glide.with(this)
                        .load(selectedImageUri)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(ivProfilePicture);
                }
            }
        }
    );
    
    private final ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(
        new ActivityResultContracts.RequestMultiplePermissions(),
        permissions -> {
            boolean allGranted = true;
            for (Boolean granted : permissions.values()) {
                if (!granted) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted) {
                openGalleryWithPermissions();
            } else {
                Toast.makeText(this, "Permission denied. Cannot access images.", Toast.LENGTH_SHORT).show();
            }
        }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        userRepository = new UserRepository(this);

        etName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        loginLink = findViewById(R.id.tvLoginLink);
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        
        ivProfilePicture.setOnClickListener(v -> checkAndRequestPermissions());

        btnSignUp.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                Toast.makeText(SignUpActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Store user name in Firebase
                                FirebaseManager.saveUserName(
                                    user.getUid(), 
                                    name,
                                    aVoid -> {
                                        // Save profile picture locally if selected
                                        if (selectedImageUri != null) {
                                            saveUserProfilePicture(user.getUid());
                                        } else {
                                            // Navigate to login screen
                                            Toast.makeText(SignUpActivity.this, "User created", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                                            finish();
                                        }
                                    },
                                    e -> {
                                        Toast.makeText(SignUpActivity.this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                );
                            }
                        } else {
                            Toast.makeText(SignUpActivity.this, "Sign Up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        loginLink.setOnClickListener(view -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
    
    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses READ_MEDIA_IMAGES 
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(new String[]{Manifest.permission.READ_MEDIA_IMAGES});
            } else {
                openGalleryWithPermissions();
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10-12 doesn't need READ_EXTERNAL_STORAGE for MediaStore
            openGalleryWithPermissions();
        } else {
            // Android 9 and below needs READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE});
            } else {
                openGalleryWithPermissions();
            }
        }
    }
    
    private void openGalleryWithPermissions() {
        // Use ACTION_GET_CONTENT instead of Photo Picker for better compatibility
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        
        try {
            pickImageLauncher.launch(Intent.createChooser(intent, "Select Picture"));
        } catch (Exception e) {
            Toast.makeText(this, "Error opening gallery: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void saveUserProfilePicture(String userId) {
        // Show progress
        Toast.makeText(SignUpActivity.this, "Saving profile picture...", Toast.LENGTH_SHORT).show();
        
        // Save image to local storage
        String imagePath = ImageStorageManager.saveImageToInternalStorage(this, selectedImageUri, userId);
        
        if (imagePath != null) {
            // Create and save user profile picture path in Room
            User user = new User(userId, imagePath);
            userRepository.insert(user);
            
            Toast.makeText(SignUpActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        } else {
            // Failed to save image, proceed anyway
            Toast.makeText(this, "Could not save profile picture, but account was created", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        }
    }
}