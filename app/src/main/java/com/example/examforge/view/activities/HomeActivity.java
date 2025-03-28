package com.example.examforge.view.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.examforge.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private QuestionPaperAdapter adapter;
    private List<String> questionPaperList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);

        // For demonstration, we are using dummy data.
        questionPaperList = new ArrayList<>();
        questionPaperList.add("Maths - Algebra Questions");
        questionPaperList.add("Physics - Mechanics Questions");
        questionPaperList.add("Chemistry - Organic Chemistry Questions");

        adapter = new QuestionPaperAdapter(questionPaperList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> {
            Toast.makeText(HomeActivity.this, "Create new Question Paper", Toast.LENGTH_SHORT).show();
            // TODO: Redirect to the PDF upload and parameters screen.
        });
    }

    // Inner Adapter class for RecyclerView
    private class QuestionPaperAdapter extends RecyclerView.Adapter<QuestionPaperAdapter.PaperViewHolder> {

        private List<String> paperList;

        public QuestionPaperAdapter(List<String> paperList) {
            this.paperList = paperList;
        }

        @Override
        public PaperViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_question_paper, parent, false);
            return new PaperViewHolder(view);
        }

        @Override
        public void onBindViewHolder(PaperViewHolder holder, int position) {
            String title = paperList.get(position);
            holder.bind(title);
        }

        @Override
        public int getItemCount() {
            return paperList.size();
        }

        class PaperViewHolder extends RecyclerView.ViewHolder {
            private TextView tvPaperTitle;

            public PaperViewHolder(View itemView) {
                super(itemView);
                tvPaperTitle = itemView.findViewById(R.id.tvPaperTitle);
            }

            public void bind(String title) {
                tvPaperTitle.setText(title);
            }
        }
    }
}