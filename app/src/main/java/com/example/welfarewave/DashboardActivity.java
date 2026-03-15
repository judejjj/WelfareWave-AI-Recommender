package com.example.welfarewave;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends BaseActivity {

    public static final String EXTRA_GUEST = "extra_guest";
    public static final String CATEGORY_FILTER = "CATEGORY_FILTER";

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private final List<Scheme> latestSchemes = new ArrayList<>();
    private SchemeHorizontalAdapter latestAdapter;
    private TextView btnLanguageToggle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView tvMarquee = findViewById(R.id.tvMarquee);
        tvMarquee.setSelected(true);

        btnLanguageToggle = findViewById(R.id.btnLanguageToggle);
        updateLanguageToggleText();
        if (btnLanguageToggle != null) {
            btnLanguageToggle.setOnClickListener(v -> toggleLanguage());
        }

        ImageButton btnEditProfile = findViewById(R.id.btnEditProfile);
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v ->
                    startActivity(new Intent(this, CompleteProfileActivity.class)));
        }

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        TextView tvEmpty = findViewById(R.id.tvEmpty);
        RecyclerView rvCategories = findViewById(R.id.rvCategories);
        RecyclerView rvLatest = findViewById(R.id.rvLatestHorizontal);
        MaterialButton btnBrowseAll = findViewById(R.id.btnBrowseAll);

        androidx.cardview.widget.CardView cardHelpCenters = findViewById(R.id.cardHelpCenters);
        if (cardHelpCenters != null) {
            cardHelpCenters.setOnClickListener(v -> {
                startActivity(new Intent(this, HelpCenterListActivity.class));
            });
        }

        androidx.cardview.widget.CardView cardAiRecommendations = findViewById(R.id.cardAiRecommendations);
        if (cardAiRecommendations != null) {
            cardAiRecommendations.setOnClickListener(v -> {
                startActivity(new Intent(DashboardActivity.this, RecommendationActivity.class));
            });
        }

        // Categories grid
        List<CategoryAdapter.CategoryItem> categories = new ArrayList<>();
        categories.add(
                new CategoryAdapter.CategoryItem(getString(R.string.cat_students), "Students", android.R.drawable.ic_menu_edit));
        categories.add(
                new CategoryAdapter.CategoryItem(getString(R.string.cat_farmers), "Farmers", android.R.drawable.ic_menu_compass));
        categories.add(
                new CategoryAdapter.CategoryItem(getString(R.string.cat_women), "Women", android.R.drawable.ic_menu_myplaces));
        categories.add(new CategoryAdapter.CategoryItem(getString(R.string.cat_senior_citizens), "Senior Citizens",
                android.R.drawable.ic_menu_today));
        categories.add(new CategoryAdapter.CategoryItem(getString(R.string.cat_disabled), "Disabled",
                android.R.drawable.ic_menu_info_details));

        // Use staggered grid layout for premium tile look
        rvCategories.setLayoutManager(new androidx.recyclerview.widget.StaggeredGridLayoutManager(2,
                androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL));
        rvCategories.setAdapter(new CategoryAdapter(categories, item -> {
            Intent i = new Intent(this, SchemeListActivity.class);
            i.putExtra(CATEGORY_FILTER, item.canonicalName);
            startActivity(i);
        }));

        // Latest schemes horizontal carousel
        rvLatest.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        latestAdapter = new SchemeHorizontalAdapter(latestSchemes, scheme -> {
            Intent i = new Intent(this, SchemeDetailActivity.class);
            i.putExtra(SchemeDetailActivity.EXTRA_TITLE, scheme.getTitle());
            i.putExtra(SchemeDetailActivity.EXTRA_DESCRIPTION, scheme.getDescription());
            i.putExtra(SchemeDetailActivity.EXTRA_ELIGIBILITY, scheme.getEligibilityRules());
            i.putExtra(SchemeDetailActivity.EXTRA_BENEFITS, scheme.getBenefits());
            i.putExtra(SchemeDetailActivity.EXTRA_APPLICATION_URL, scheme.getApplicationUrl());
            startActivity(i);
        });
        rvLatest.setAdapter(latestAdapter);

        btnBrowseAll.setOnClickListener(v -> startActivity(new Intent(this, SchemeListActivity.class)));

        FirebaseUser user = auth.getCurrentUser();
        boolean isGuest = getIntent().getBooleanExtra(EXTRA_GUEST, false);

        if (user == null && !isGuest) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        if (isGuest || user == null) {
            tvWelcome.setText(getString(R.string.welcome, "Guest"));
            androidx.cardview.widget.CardView cardBanner = findViewById(R.id.cardProfileBanner);
            if (cardBanner != null) cardBanner.setVisibility(View.GONE);
        } else {
            db.collection("user_profiles")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        String name = snapshot != null ? snapshot.getString("name") : null;
                        if (name == null || name.trim().isEmpty()) {
                            name = user.getEmail() != null ? user.getEmail() : "User";
                        }
                        tvWelcome.setText(getString(R.string.welcome, name));

                        // Progressive Profiling Check
                        androidx.cardview.widget.CardView cardProfileBanner = findViewById(R.id.cardProfileBanner);
                        TextView tvCompleteNow = findViewById(R.id.tvCompleteNow);
                        
                        if (snapshot != null && snapshot.exists() && snapshot.contains("age") && snapshot.get("age") != null && snapshot.getDouble("age") > 0) {
                            if (cardProfileBanner != null) cardProfileBanner.setVisibility(View.GONE);
                        } else {
                            if (cardProfileBanner != null) {
                                cardProfileBanner.setVisibility(View.VISIBLE);
                                if (tvCompleteNow != null) {
                                    tvCompleteNow.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, CompleteProfileActivity.class)));
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        tvWelcome.setText(getString(R.string.welcome, "User"));
                        Toast.makeText(this, "Could not load profile", Toast.LENGTH_SHORT).show();
                    });
        }

        if ("ml".equals(LocaleHelper.getLanguage(this))) {
            TranslationManager.getInstance().downloadModelIfNeeded(
                    () -> android.util.Log.d("DashboardActivity", "Model ready"),
                    () -> android.util.Log.e("DashboardActivity", "Model download failed"));
        }

        loadLatestSchemes(tvEmpty);
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView tvEmpty = findViewById(R.id.tvEmpty);
        loadLatestSchemes(tvEmpty);
    }

    private void loadLatestSchemes(TextView tvEmpty) {
        // Query ordered by timestamp descending to get the newest first
        Query query = db.collection("welfare_schemes")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(5);

        query.get()
                .addOnSuccessListener(result -> {
                    latestSchemes.clear();
                    for (DocumentSnapshot doc : result.getDocuments()) {
                        Scheme s = doc.toObject(Scheme.class);
                        if (s == null)
                            continue;
                        s.setId(doc.getId());
                        latestSchemes.add(s);
                    }
                    latestAdapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(latestSchemes.isEmpty() ? View.VISIBLE : View.GONE);

                    // Update Marquee with the NEWEST scheme only
                    TextView tvMarquee = findViewById(R.id.tvMarquee);
                    if (!latestSchemes.isEmpty()) {
                        Scheme newest = latestSchemes.get(0);
                        // Marquee text: "NEW: <Title> - <Description>"
                        String marqueeText = newest.getTitle() + " - " + newest.getDescription(); // Removed hardcoded
                                                                                                  // prefix, user said
                                                                                                  // "give maqree for
                                                                                                  // only the new
                                                                                                  // scheme"
                        tvMarquee.setText(marqueeText);
                        tvMarquee.setSelected(true);
                    } else {
                        tvMarquee.setText("Welcome to WelfareWave. Check back soon for new schemes.");
                        tvMarquee.setSelected(true);
                    }
                })
                .addOnFailureListener(e -> {
                    // Fallback to simpler query if index is missing or other failure
                    db.collection("welfare_schemes")
                            .limit(5)
                            .get()
                            .addOnSuccessListener(result -> {
                                latestSchemes.clear();
                                for (DocumentSnapshot doc : result.getDocuments()) {
                                    Scheme s = doc.toObject(Scheme.class);
                                    if (s == null)
                                        continue;
                                    s.setId(doc.getId());
                                    latestSchemes.add(s);
                                }
                                latestAdapter.notifyDataSetChanged();
                                tvEmpty.setVisibility(latestSchemes.isEmpty() ? View.VISIBLE : View.GONE);

                                if (!latestSchemes.isEmpty()) {
                                    TextView tvMarquee = findViewById(R.id.tvMarquee);
                                    tvMarquee.setText(latestSchemes.get(0).getTitle());
                                    tvMarquee.setSelected(true);
                                }
                            })
                            .addOnFailureListener(ex -> {
                                tvEmpty.setVisibility(View.VISIBLE);
                                Toast.makeText(this, "Failed to load schemes: " + ex.getMessage(), Toast.LENGTH_SHORT)
                                        .show();
                            });
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            auth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateLanguageToggleText() {
        if (btnLanguageToggle != null) {
            String currentLang = LocaleHelper.getLanguage(this);
            btnLanguageToggle.setText(currentLang.equals("ml") ? "English" : "മലയാളം");
        }
    }

    private void toggleLanguage() {
        String currentLang = LocaleHelper.getLanguage(this);
        String newLang = currentLang.equals("ml") ? "en" : "ml";
        LocaleHelper.setLocale(this, newLang);
        recreate();
    }
}
