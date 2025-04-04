package com.example.examforge.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);

    @Update
    void update(User user);

    @Query("SELECT * FROM user_profiles WHERE userId = :userId")
    LiveData<User> getUserById(String userId);

    @Query("SELECT * FROM user_profiles WHERE userId = :userId")
    User getUserByIdSync(String userId);
    
    // This query forces Room to refresh its cache for this user
    @Query("SELECT * FROM user_profiles WHERE userId = :userId LIMIT 0")
    List<User> invalidateUserById(String userId);
} 