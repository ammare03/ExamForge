package com.example.examforge.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.examforge.R;
import com.example.examforge.model.QuestionPaper;
import com.example.examforge.repository.QuestionPaperHistoryRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private QuestionPaperAdapter adapter;
    private QuestionPaperHistoryRepository historyRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);

        adapter = new QuestionPaperAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        historyRepository = new QuestionPaperHistoryRepository(this);
        // Observe changes in the database
        historyRepository.getAllQuestionPapers().observe(this, new Observer<List<QuestionPaper>>() {
            @Override
            public void onChanged(List<QuestionPaper> questionPapers) {
                adapter.setData(questionPapers);
            }
        });

        fabAdd.setOnClickListener(v -> {
            // Navigate to the PDF upload and parameter screen
            startActivity(new Intent(HomeActivity.this, CreateQuestionPaperActivity.class));
        });
    }

    // Adapter for RecyclerView
    private class QuestionPaperAdapter extends RecyclerView.Adapter<QuestionPaperAdapter.PaperViewHolder> {

        private List<QuestionPaper> paperList;

        public QuestionPaperAdapter(List<QuestionPaper> paperList) {
            this.paperList = paperList;
        }

        public void setData(List<QuestionPaper> papers) {
            this.paperList = papers;
            notifyDataSetChanged();
        }

        @Override
        public PaperViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_question_paper, parent, false);
            return new PaperViewHolder(view);
        }

        @Override
        public void onBindViewHolder(PaperViewHolder holder, int position) {
            QuestionPaper paper = paperList.get(position);
            holder.tvPaperTitle.setText(paper.getTitle());
            // Allow deletion on long press
            holder.itemView.setOnLongClickListener(v -> {
                historyRepository.delete(paper);
                Toast.makeText(HomeActivity.this, "Question paper deleted", Toast.LENGTH_SHORT).show();
                return true;
            });
            // Optionally, you can add a click listener to view the PDF using PreviewActivity.
        }

        @Override
        public int getItemCount() {
            return paperList == null ? 0 : paperList.size();
        }

        class PaperViewHolder extends RecyclerView.ViewHolder {
            TextView tvPaperTitle;
            public PaperViewHolder(View itemView) {
                super(itemView);
                tvPaperTitle = itemView.findViewById(R.id.tvPaperTitle);
            }
        }
    }
}