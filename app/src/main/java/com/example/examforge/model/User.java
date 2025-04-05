package com.example.examforge.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_profiles")
public class User {
    @PrimaryKey
    @NonNull
    private String userId;
    private String profilePicPath; 

    public User(@NonNull String userId, String profilePicPath) {
        this.userId = userId;
        this.profilePicPath = profilePicPath;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    public String getProfilePicPath() {
        return profilePicPath;
    }

    public void setProfilePicPath(String profilePicPath) {
        this.profilePicPath = profilePicPath;
    }
} 