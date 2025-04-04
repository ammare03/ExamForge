package com.example.examforge.utils;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Utility class to manage Firebase Database operations
 */
public class FirebaseManager {
    
    private static final String USERS_NODE = "users";
    
    /**
     * Get a reference to a specific user in the database
     */
    private static DatabaseReference getUserReference(String userId) {
        return FirebaseDatabase.getInstance().getReference(USERS_NODE).child(userId);
    }
    
    /**
     * Save user name to Firebase database
     */
    public static void saveUserName(String userId, String name, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        getUserReference(userId).child("name").setValue(name)
            .addOnSuccessListener(successListener)
            .addOnFailureListener(failureListener);
    }
    
    /**
     * Get user name from Firebase database
     */
    public static void getUserName(String userId, OnUserDataCallback callback) {
        getUserReference(userId).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.exists() ? snapshot.getValue(String.class) : null;
                callback.onUserNameLoaded(name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onUserNameLoadFailed(error.getMessage());
            }
        });
    }
    
    /**
     * Callback interface for user data operations
     */
    public interface OnUserDataCallback {
        void onUserNameLoaded(String name);
        void onUserNameLoadFailed(String errorMessage);
    }
} 