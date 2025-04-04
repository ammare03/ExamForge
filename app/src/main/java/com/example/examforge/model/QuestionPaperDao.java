package com.example.examforge.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface QuestionPaperDao {
    @Insert
    long insert(QuestionPaper questionPaper);

    @Query("SELECT * FROM question_papers ORDER BY createdAt DESC")
    LiveData<List<QuestionPaper>> getAllQuestionPapers();

    // New query: fetch only question papers for a given userId
    @Query("SELECT * FROM question_papers WHERE userId = :userId ORDER BY createdAt DESC")
    LiveData<List<QuestionPaper>> getQuestionPapersForUser(String userId);

    @Delete
    void delete(QuestionPaper questionPaper);
}