package com.example.examforge.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "question_papers")
public class QuestionPaper {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String filePath;
    private long createdAt;
    private String userId;  

    
    public QuestionPaper(String title, String filePath, long createdAt, String userId) {
        this.title = title;
        this.filePath = filePath;
        this.createdAt = createdAt;
        this.userId = userId;
    }

    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getFilePath() {
        return filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    public long getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
}