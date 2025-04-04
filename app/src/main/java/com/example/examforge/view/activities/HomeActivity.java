package com.example.examforge.view.activities;

import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.examforge.R;
import com.example.examforge.model.QuestionPaper;
import com.example.examforge.repository.QuestionPaperHistoryRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private QuestionPaperAdapter adapter;
    private QuestionPaperHistoryRepository historyRepository;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Set info button click listener
        ImageView ivInfo = toolbar.findViewById(R.id.ivInfo);
        if (ivInfo != null) {
            ivInfo.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, AboutActivity.class)));
        }

        navigationView.setNavigationItemSelectedListener(this);

        loadUserProfileInNavHeader();

        adapter = new QuestionPaperAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        historyRepository = new QuestionPaperHistoryRepository(this);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            historyRepository.getQuestionPapersForUser(uid).observe(this, new Observer<List<QuestionPaper>>() {
                @Override
                public void onChanged(List<QuestionPaper> questionPapers) {
                    adapter.setData(questionPapers);
                }
            });
        }

        fabAdd.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, CreateQuestionPaperActivity.class)));

        // Enable swipe-to-delete functionality
        enableSwipeToDelete();
    }

    private void enableSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                QuestionPaper questionPaper = adapter.getQuestionPapers().get(position);
                historyRepository.delete(questionPaper);
                Toast.makeText(HomeActivity.this, "Question paper deleted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void loadUserProfileInNavHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView tvNavUserName = headerView.findViewById(R.id.tvNavUserName);
        // No profile picture is needed; only the name is displayed.
        String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (uid != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        tvNavUserName.setText(name != null ? name : "User Name");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(HomeActivity.this, "Error loading user data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_profile) {
            startActivity(new Intent(HomeActivity.this, UserProfileActivity.class));
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(HomeActivity.this, AboutActivity.class));
        } else if (id == R.id.nav_logout) {
            mAuth.signOut();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        }
        drawerLayout.closeDrawers();
        return true;
    }

    // RecyclerView Adapter for displaying history.
    private class QuestionPaperAdapter extends RecyclerView.Adapter<QuestionPaperAdapter.PaperViewHolder> {

        private List<QuestionPaper> paperList;

        public QuestionPaperAdapter(List<QuestionPaper> paperList) {
            this.paperList = paperList;
        }

        public void setData(List<QuestionPaper> papers) {
            this.paperList = papers;
            notifyDataSetChanged();
        }

        public List<QuestionPaper> getQuestionPapers() {
            return paperList;
        }

        @NonNull
        @Override
        public PaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_question_paper, parent, false);
            return new PaperViewHolder(view);
        }

        @Override
        public void onBindViewHolder(PaperViewHolder holder, int position) {
            QuestionPaper paper = paperList.get(position);
            holder.tvPaperTitle.setText(paper.getTitle());
            holder.tvPaperDate.setText(DateFormat.format("yyyy-MM-dd HH:mm", paper.getCreatedAt()));

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, PreviewActivity.class);
                intent.putExtra("pdfFilePath", paper.getFilePath());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return paperList == null ? 0 : paperList.size();
        }

        class PaperViewHolder extends RecyclerView.ViewHolder {
            TextView tvPaperTitle, tvPaperDate;

            public PaperViewHolder(View itemView) {
                super(itemView);
                tvPaperTitle = itemView.findViewById(R.id.tvPaperTitle);
                tvPaperDate = itemView.findViewById(R.id.tvPaperDate);
            }
        }
    }
}