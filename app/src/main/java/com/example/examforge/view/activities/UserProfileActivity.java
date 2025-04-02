package com.example.examforge.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.examforge.R;
import com.google.firebase.auth.FirebaseAuth;

public class UserProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText etName, etEmail;
    private ImageView ivProfilePicture;
    private Button btnSave, btnEdit, btnLogout;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        btnSave = findViewById(R.id.btnSave);
        btnEdit = findViewById(R.id.btnEdit);
        btnLogout = findViewById(R.id.btnLogout);

        // Load user data (Assuming it was saved in SharedPreferences or Firebase)
        loadUserData();

        // Edit button functionality
        btnEdit.setOnClickListener(v -> {
            etName.setEnabled(true);
            etEmail.setEnabled(true);
            btnSave.setVisibility(View.VISIBLE);
            btnEdit.setVisibility(View.GONE);
        });

        // Save button functionality
        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();

            if (!name.isEmpty() && !email.isEmpty()) {
                // Save updated user data (can use SharedPreferences or Firebase)
                saveUserData(name, email);
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                etName.setEnabled(false);
                etEmail.setEnabled(false);
                btnSave.setVisibility(View.GONE);
                btnEdit.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });

        // Logout functionality
        btnLogout.setOnClickListener(v -> {
            // Sign out from Firebase Authentication
            mAuth.signOut();
            // Redirect to LoginActivity
            startActivity(new Intent(UserProfileActivity.this, LoginActivity.class));
            finish(); // Finish the UserProfileActivity so the user cannot go back to it
        });

        // Profile picture click listener
        ivProfilePicture.setOnClickListener(v -> openImageChooser());
    }

    // Load user data from SharedPreferences (or Firebase)
    private void loadUserData() {
        String name = "User Name"; // Replace with saved name (e.g., SharedPreferences or Firebase)
        String email = "user@example.com"; // Replace with saved email (e.g., SharedPreferences or Firebase)
        etName.setText(name);
        etEmail.setText(email);
    }

    // Save user data (e.g., in SharedPreferences or Firebase)
    private void saveUserData(String name, String email) {
        // Save data to SharedPreferences or Firebase if required
    }

    // Open image chooser for profile picture
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Handle result of image picker
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            ivProfilePicture.setImageURI(data.getData()); // Set profile picture
        }
    }
}