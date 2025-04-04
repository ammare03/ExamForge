package com.example.examforge.repository;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import com.example.examforge.model.AppDatabase;
import com.example.examforge.model.User;
import com.example.examforge.model.UserDao;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private UserDao userDao;
    private Executor executor = Executors.newSingleThreadExecutor();
    private AppDatabase db;

    public UserRepository(Context context) {
        db = AppDatabase.getDatabase(context);
        userDao = db.userDao();
    }

    public void insert(User user) {
        Log.d(TAG, "Inserting user with ID: " + user.getUserId() + ", path: " + user.getProfilePicPath());
        executor.execute(() -> userDao.insert(user));
    }

    public void update(User user) {
        Log.d(TAG, "Updating user with ID: " + user.getUserId() + ", path: " + user.getProfilePicPath());
        executor.execute(() -> {
            userDao.update(user);
            // Invalidate LiveData to force observers to refresh
            try {
                db.runInTransaction(() -> {
                    // Just call the method to trigger a refresh, we don't need the result
                    userDao.invalidateUserById(user.getUserId());
                });
                Log.d(TAG, "Successfully invalidated cache for user: " + user.getUserId());
            } catch (Exception e) {
                Log.e(TAG, "Error invalidating user: " + e.getMessage());
            }
        });
    }

    public LiveData<User> getUserById(String userId) {
        Log.d(TAG, "Getting user by ID (LiveData): " + userId);
        return userDao.getUserById(userId);
    }

    public User getUserByIdSync(String userId) {
        Log.d(TAG, "Getting user by ID (Sync): " + userId);
        return userDao.getUserByIdSync(userId);
    }

    // This method will run on background thread to get sync data
    public interface UserCallback {
        void onUserLoaded(User user);
    }

    public void getUserByIdAsync(String userId, UserCallback callback) {
        executor.execute(() -> {
            User user = userDao.getUserByIdSync(userId);
            callback.onUserLoaded(user);
        });
    }
} 