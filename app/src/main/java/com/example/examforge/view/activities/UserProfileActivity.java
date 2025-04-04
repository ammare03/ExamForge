package com.example.examforge.view.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.examforge.R;
import com.example.examforge.model.User;
import com.example.examforge.repository.UserRepository;
import com.example.examforge.utils.FirebaseManager;
import com.example.examforge.utils.ImageStorageManager;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.imageview.ShapeableImageView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;

public class UserProfileActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 123;
    private static final String TAG = "UserProfileActivity";

    private EditText etName, etEmail;
    private ShapeableImageView ivProfilePicture;
    private Button btnSave, btnEdit, btnLogout, btnChangePhoto;

    private FirebaseAuth mAuth;
    private Uri selectedImageUri;
    private String userId;
    private User localUserProfile;
    private UserRepository userRepository;
    
    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                selectedImageUri = result.getData().getData();
                if (selectedImageUri != null) {
                    // Don't directly set the URI to the ImageView, as it might be a content:// URI
                    // that requires permissions to access directly
                    Glide.with(this)
                        .load(selectedImageUri)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(ivProfilePicture);
                    
                    saveProfilePicture();
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
        setContentView(R.layout.activity_user_profile);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            startActivity(new Intent(UserProfileActivity.this, LoginActivity.class));
            finish();
            return;
        }

        userId = firebaseUser.getUid();
        userRepository = new UserRepository(this);

        // Initialize UI elements
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        btnSave = findViewById(R.id.btnSave);
        btnEdit = findViewById(R.id.btnEdit);
        btnLogout = findViewById(R.id.btnLogout);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);

        etEmail.setText(firebaseUser.getEmail());
        etEmail.setEnabled(false);

        loadUserData();
        
        ivProfilePicture.setOnClickListener(v -> checkAndRequestPermissions());
        
        if (btnChangePhoto != null) {
            btnChangePhoto.setOnClickListener(v -> checkAndRequestPermissions());
        }

        btnEdit.setOnClickListener(view -> {
            etName.setEnabled(true);
            btnSave.setVisibility(View.VISIBLE);
            btnEdit.setVisibility(View.GONE);
        });

        btnSave.setOnClickListener(view -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(UserProfileActivity.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            saveUserData(name);
        });

        btnLogout.setOnClickListener(view -> {
            mAuth.signOut();
            startActivity(new Intent(UserProfileActivity.this, LoginActivity.class));
            finish();
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload user data when returning to this activity
        loadUserData();
    }
    
    private void loadUserData() {
        // Load name from Firebase
        FirebaseManager.getUserName(userId, new FirebaseManager.OnUserDataCallback() {
            @Override
            public void onUserNameLoaded(String name) {
                if (name != null) {
                    etName.setText(name);
                }
            }

            @Override
            public void onUserNameLoadFailed(String errorMessage) {
                Toast.makeText(UserProfileActivity.this, "Failed to load name: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
        
        // Load profile picture from Room
        userRepository.getUserById(userId).observe(this, user -> {
            localUserProfile = user;
            if (user != null) {
                if (user.getProfilePicPath() != null) {
                    // Check if file exists
                    File imageFile = new File(user.getProfilePicPath());
                    if (imageFile.exists()) {
                        // Clear any previous image loading
                        Glide.with(UserProfileActivity.this).clear(ivProfilePicture);
                        
                        // Load the image with cache disabled
                        Glide.with(UserProfileActivity.this)
                            .load(imageFile)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .error(R.drawable.ic_launcher_foreground)
                            .into(ivProfilePicture);
                    } else {
                        ivProfilePicture.setImageResource(R.drawable.ic_launcher_foreground);
                    }
                } else {
                    ivProfilePicture.setImageResource(R.drawable.ic_launcher_foreground);
                }
            } else {
                ivProfilePicture.setImageResource(R.drawable.ic_launcher_foreground);
            }
        });
    }
    
    private void saveUserData(String name) {
        // Save name to Firebase
        FirebaseManager.saveUserName(
            userId, 
            name,
            aVoid -> {
                etName.setEnabled(false);
                btnSave.setVisibility(View.GONE);
                btnEdit.setVisibility(View.VISIBLE);
                Toast.makeText(UserProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
            },
            e -> Toast.makeText(UserProfileActivity.this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
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
    
    private void saveProfilePicture() {
        if (selectedImageUri == null) return;
        
        Toast.makeText(UserProfileActivity.this, "Saving profile picture...", Toast.LENGTH_SHORT).show();
        
        // Delete old profile picture if it exists
        if (localUserProfile != null && localUserProfile.getProfilePicPath() != null) {
            ImageStorageManager.deleteImageFromInternalStorage(localUserProfile.getProfilePicPath());
        }
        
        // Save new image to local storage
        String imagePath = ImageStorageManager.saveImageToInternalStorage(this, selectedImageUri, userId);
        
        if (imagePath != null) {
            // Update user with new profile picture path in Room
            if (localUserProfile == null) {
                localUserProfile = new User(userId, imagePath);
                userRepository.insert(localUserProfile);
            } else {
                localUserProfile.setProfilePicPath(imagePath);
                userRepository.update(localUserProfile);
            }
            
            // Verify the file exists before loading
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                // Clear any previous image loading
                Glide.with(UserProfileActivity.this).clear(ivProfilePicture);
                
                // Immediately update the ImageView with the new image
                Glide.with(UserProfileActivity.this)
                    .load(imageFile)  // Load from File instead of path String
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .skipMemoryCache(true)  // Skip memory cache to ensure fresh loading
                    .diskCacheStrategy(DiskCacheStrategy.NONE)  // Skip disk cache too
                    .into(ivProfilePicture);
                
                // Force Home activity to refresh next time it's visible
                Intent refreshIntent = new Intent("com.example.examforge.PROFILE_UPDATED");
                LocalBroadcastManager.getInstance(UserProfileActivity.this).sendBroadcast(refreshIntent);
                
                Toast.makeText(UserProfileActivity.this, "Profile picture updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(UserProfileActivity.this, "Error: Saved file not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(UserProfileActivity.this, "Failed to save profile picture", Toast.LENGTH_SHORT).show();
        }
    }
}