package com.example.examforge.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.example.examforge.model.AppDatabase;
import com.example.examforge.model.QuestionPaper;
import com.example.examforge.model.QuestionPaperDao;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class QuestionPaperHistoryRepository {
    private QuestionPaperDao questionPaperDao;
    private Executor executor = Executors.newSingleThreadExecutor();

    public QuestionPaperHistoryRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        questionPaperDao = db.questionPaperDao();
    }

    public void insert(QuestionPaper questionPaper) {
        executor.execute(() -> questionPaperDao.insert(questionPaper));
    }

    public void delete(QuestionPaper questionPaper) {
        executor.execute(() -> questionPaperDao.delete(questionPaper));
    }

    public LiveData<List<QuestionPaper>> getAllQuestionPapers() {
        return questionPaperDao.getAllQuestionPapers();
    }
}