package com.example.examforge.view.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
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
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.examforge.R;
import com.example.examforge.model.QuestionPaper;
import com.example.examforge.model.User;
import com.example.examforge.repository.QuestionPaperHistoryRepository;
import com.example.examforge.repository.UserRepository;
import com.example.examforge.utils.FirebaseManager;
import com.example.examforge.utils.ThemeManager;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "HomeActivity";
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private ExtendedFloatingActionButton fabAdd;
    private QuestionPaperAdapter adapter;
    private QuestionPaperHistoryRepository historyRepository;
    private UserRepository userRepository;
    private FirebaseAuth mAuth;
    private ShapeableImageView ivNavUserPhoto;
    private BroadcastReceiver profileUpdateReceiver;
    private ImageView ivThemeToggle;
    private boolean isDarkMode;
    private SearchView searchView;
    private View noResultsContainer;
    private TextView tvNoResultsQuery;
    private List<QuestionPaper> allQuestionPapers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Apply saved theme before setting content view
        ThemeManager.applyTheme(this);
        
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        userRepository = new UserRepository(this);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);
        searchView = findViewById(R.id.searchView);
        noResultsContainer = findViewById(R.id.noResultsContainer);
        tvNoResultsQuery = findViewById(R.id.tvNoResultsQuery);

        // Initialize the profile picture reference
        View headerView = navigationView.getHeaderView(0);
        ivNavUserPhoto = headerView.findViewById(R.id.ivNavUserPhoto);

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
        
        // Setup theme toggle button - must be after setSupportActionBar
        setupThemeToggle();

        navigationView.setNavigationItemSelectedListener(this);

        loadUserProfileInNavHeader();

        // Register for profile updates
        profileUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "Received profile update broadcast");
                loadUserProfileInNavHeader();
            }
        };

        // Register with LocalBroadcastManager instead of system broadcast
        LocalBroadcastManager.getInstance(this).registerReceiver(
            profileUpdateReceiver, 
            new IntentFilter("com.example.examforge.PROFILE_UPDATED")
        );

        // Initialize adapter with empty list
        allQuestionPapers = new ArrayList<>();
        adapter = new QuestionPaperAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Setup search view
        setupSearchView();

        historyRepository = new QuestionPaperHistoryRepository(this);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Get only this user's question papers
            historyRepository.getQuestionPapersForUser(currentUser.getUid()).observe(this, questionPapers -> {
                if (questionPapers != null && !questionPapers.isEmpty()) {
                    allQuestionPapers = new ArrayList<>(questionPapers);
                    adapter.updateData(allQuestionPapers);
                    recyclerView.setVisibility(View.VISIBLE);
                    findViewById(R.id.emptyStateContainer).setVisibility(View.GONE);
                    noResultsContainer.setVisibility(View.GONE);
                } else {
                    recyclerView.setVisibility(View.GONE);
                    findViewById(R.id.emptyStateContainer).setVisibility(View.VISIBLE);
                    noResultsContainer.setVisibility(View.GONE);
                }
            });
        }

        // Setup swipe to delete
        setupItemTouchHelper();

        fabAdd.setOnClickListener(v -> {
            // Navigate to create question paper screen
            startActivity(new Intent(HomeActivity.this, CreateQuestionPaperActivity.class));
        });
    }

            @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister broadcast receiver
        if (profileUpdateReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(profileUpdateReceiver);
        }
            }

            @Override
    protected void onResume() {
        super.onResume();
        // Refresh the profile picture when returning to this activity
        loadUserProfileInNavHeader();
    }

    private void loadUserProfileInNavHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView tvNavUserName = headerView.findViewById(R.id.tvNavUserName);
        TextView tvNavUserEmail = headerView.findViewById(R.id.tvNavUserEmail);
        ShapeableImageView ivNavUserPhoto = headerView.findViewById(R.id.ivNavUserPhoto);
        
        // Ensure the ShapeableImageView has a circular shape
        ivNavUserPhoto.setShapeAppearanceModel(
            ShapeAppearanceModel.builder()
                .setAllCornerSizes(ShapeAppearanceModel.PILL)
                .build()
        );
        
        String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (uid != null) {
            // Load user email
            String email = mAuth.getCurrentUser().getEmail();
            if (email != null && !email.isEmpty()) {
                tvNavUserEmail.setText(email);
            } else {
                tvNavUserEmail.setText("ExamForge User");
            }
            
            // Load user name from Firebase
            FirebaseManager.getUserName(uid, new FirebaseManager.OnUserDataCallback() {
                @Override
                public void onUserNameLoaded(String name) {
                        tvNavUserName.setText(name != null ? name : "User Name");
                }

                @Override
                public void onUserNameLoadFailed(String errorMessage) {
                    tvNavUserName.setText("User Name");
                    Toast.makeText(HomeActivity.this, "Error loading user data", Toast.LENGTH_SHORT).show();
                }
            });
            
            // Load profile picture from Room
            userRepository.getUserById(uid).observe(this, user -> {
                if (user != null && user.getProfilePicPath() != null) {
                    File imageFile = new File(user.getProfilePicPath());
                    if (imageFile.exists()) {
                        Glide.with(HomeActivity.this)
                            .load(imageFile)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .circleCrop() // Apply circle crop transformation
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .error(R.drawable.ic_launcher_foreground)
                            .into(ivNavUserPhoto);
                    } else {
                        // Set default icon if file doesn't exist
                        ivNavUserPhoto.setImageResource(R.drawable.ic_launcher_foreground);
                    }
                } else {
                    // Set default icon
                    ivNavUserPhoto.setImageResource(R.drawable.ic_launcher_foreground);
                }
            });
        } else {
            // Not logged in
            tvNavUserName.setText("Guest User");
            tvNavUserEmail.setText("Not logged in");
            ivNavUserPhoto.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_profile) {
            startActivity(new Intent(HomeActivity.this, UserProfileActivity.class));
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(HomeActivity.this, AboutActivity.class));
        } else if (id == R.id.nav_share) {
            shareApp();
        } else if (id == R.id.nav_logout) {
            mAuth.signOut();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        }
        drawerLayout.closeDrawers();
        return true;
    }

    private void shareApp() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "ExamForge");
            
            String shareMessage = "Check out ExamForge, an app to create and share question papers easily!\n\n";
            shareMessage = shareMessage + "https://play.google.com/store/search?q=examforge";
            
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        } catch (Exception e) {
            Toast.makeText(this, "Error sharing app", Toast.LENGTH_SHORT).show();
        }
    }

    // RecyclerView Adapter for displaying history.
    private class QuestionPaperAdapter extends RecyclerView.Adapter<QuestionPaperAdapter.QuestionPaperViewHolder> {
        private List<QuestionPaper> questionPapers;

        public QuestionPaperAdapter(List<QuestionPaper> questionPapers) {
            this.questionPapers = questionPapers;
        }

        public void updateData(List<QuestionPaper> newData) {
            this.questionPapers = newData;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public QuestionPaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_question_paper, parent, false);
            return new QuestionPaperViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull QuestionPaperViewHolder holder, int position) {
            QuestionPaper paper = questionPapers.get(position);
            holder.tvTitle.setText(paper.getTitle());
            
            // Format the date as a readable string
            String date = DateFormat.format("MMM dd, yyyy", paper.getCreatedAt()).toString();
            holder.tvDate.setText(date);
            
            // Set click listener to open the PDF
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, PreviewActivity.class);
                intent.putExtra("filePath", paper.getFilePath());
                intent.putExtra("title", paper.getTitle());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return questionPapers.size();
        }

        public QuestionPaper getItemAt(int position) {
            if (position >= 0 && position < questionPapers.size()) {
                return questionPapers.get(position);
            }
            return null;
        }

        class QuestionPaperViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDate;

            QuestionPaperViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvPaperTitle);
                tvDate = itemView.findViewById(R.id.tvPaperDate);
            }
        }
    }
    
    private void setupItemTouchHelper() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                QuestionPaper questionPaper = adapter.getItemAt(position);
                if (questionPaper != null) {
                    historyRepository.delete(questionPaper);
                    Toast.makeText(HomeActivity.this, "Question paper deleted", Toast.LENGTH_SHORT).show();
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
    
    private void setupThemeToggle() {
        // Get current theme state
        isDarkMode = ThemeManager.getDarkModeState(this);
        
        // Find the theme toggle button in the toolbar
        ivThemeToggle = toolbar.findViewById(R.id.ivThemeToggle);
        
        // Ensure the toggle is visible and configured properly
        if (ivThemeToggle != null) {
            ivThemeToggle.setVisibility(View.VISIBLE);
            
            // Make sure the icon has the right tint color
            ivThemeToggle.setColorFilter(getResources().getColor(R.color.white, getTheme()));
            
            // Update icon based on current theme - this sets the correct icon
            updateThemeIcon();
            
            // Set click listener for theme toggle
            ivThemeToggle.setOnClickListener(v -> {
                // Toggle theme
                isDarkMode = ThemeManager.toggleDarkMode(this);
                
                // Update icon immediately
                updateThemeIcon();
                
                // Show feedback to user
                String message = isDarkMode ? "Dark mode enabled" : "Light mode enabled";
                Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show();
                
                // Recreate activity to apply theme changes
                recreate();
            });
        } else {
            Log.e(TAG, "Theme toggle button not found in toolbar");
        }
    }
    
    private void updateThemeIcon() {
        if (ivThemeToggle != null) {
            ivThemeToggle.setImageResource(isDarkMode 
                ? R.drawable.ic_light_mode  // Show light mode icon when in dark mode
                : R.drawable.ic_dark_mode); // Show dark mode icon when in light mode
                
            // Ensure visibility again
            ivThemeToggle.setVisibility(View.VISIBLE);
        }
    }

    private void setupSearchView() {
        // Configure search view appearance
        searchView.setMaxWidth(Integer.MAX_VALUE);
        
        // Make sure the search view is clickable and focusable
        searchView.setClickable(true);
        searchView.setFocusable(true);
        searchView.setFocusableInTouchMode(true);
        
        // Find the search text view to adjust its appearance
        View searchPlate = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        if (searchPlate != null && searchPlate instanceof TextView) {
            TextView searchTextView = (TextView) searchPlate;
            searchTextView.setTextSize(14f);
        }
        
        // Set query listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterQuestionPapers(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterQuestionPapers(newText);
                return true;
            }
        });
        
        // Fix for clicking issue - use click listener
        View.OnClickListener clickListener = v -> {
            searchView.onActionViewExpanded();
            searchView.requestFocus();
        };
        
        // Attach the click listener to both the search view and its parent card
        searchView.setOnClickListener(clickListener);
        View searchCardView = findViewById(R.id.searchCardView);
        if (searchCardView != null) {
            searchCardView.setOnClickListener(clickListener);
        }
        
        // Add clear listener to reset to all papers
        searchView.setOnCloseListener(() -> {
            adapter.updateData(allQuestionPapers);
            recyclerView.setVisibility(View.VISIBLE);
            noResultsContainer.setVisibility(View.GONE);
            return false;
        });
    }
    
    private void filterQuestionPapers(String query) {
        if (allQuestionPapers == null || allQuestionPapers.isEmpty()) {
            return;
        }
        
        if (query == null || query.trim().isEmpty()) {
            // If query is empty, show all papers
            adapter.updateData(allQuestionPapers);
            recyclerView.setVisibility(View.VISIBLE);
            noResultsContainer.setVisibility(View.GONE);
            return;
        }
        
        String lowerCaseQuery = query.toLowerCase().trim();
        List<QuestionPaper> filteredList = new ArrayList<>();
        
        for (QuestionPaper paper : allQuestionPapers) {
            if (paper.getTitle().toLowerCase().contains(lowerCaseQuery)) {
                filteredList.add(paper);
            }
        }
        
        if (filteredList.isEmpty()) {
            // Show "no results" view with the query
            recyclerView.setVisibility(View.GONE);
            noResultsContainer.setVisibility(View.VISIBLE);
            tvNoResultsQuery.setText("No results found for \"" + query + "\"");
        } else {
            // Update adapter with filtered data
            adapter.updateData(filteredList);
            recyclerView.setVisibility(View.VISIBLE);
            noResultsContainer.setVisibility(View.GONE);
        }
    }
}