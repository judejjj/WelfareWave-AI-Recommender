package com.example.welfarewave;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class RecommendationActivity extends BaseActivity {

    private static final String TAG = "AI_DEBUG";

    private final List<Scheme> schemes = new ArrayList<>();
    private SchemeAdapter adapter;
    // tvEmpty is a LinearLayout in XML — typed as View so setVisibility() works
    private View tvEmpty;
    private RecyclerView rv;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendation);

        Log.d(TAG, "RecommendationActivity: onCreate.");

        tvEmpty = findViewById(R.id.tvEmpty);
        rv = findViewById(R.id.rvSchemes);
        progressBar = findViewById(R.id.progressBar);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SchemeAdapter(schemes, scheme -> {
            Log.d(TAG, "Scheme clicked: " + scheme.getTitle());
            Intent i = new Intent(this, SchemeDetailActivity.class);
            i.putExtra(SchemeDetailActivity.EXTRA_TITLE, scheme.getTitle());
            i.putExtra(SchemeDetailActivity.EXTRA_DESCRIPTION, scheme.getDescription());
            i.putExtra(SchemeDetailActivity.EXTRA_ELIGIBILITY, scheme.getEligibilityRules());
            i.putExtra(SchemeDetailActivity.EXTRA_BENEFITS, scheme.getBenefits());
            i.putExtra(SchemeDetailActivity.EXTRA_APPLICATION_URL, scheme.getApplicationUrl());
            i.putExtra(SchemeDetailActivity.EXTRA_SCHEME_ID, scheme.getId());
            startActivity(i);
        });
        rv.setAdapter(adapter);

        loadRecommendations();
    }

    // ---- UI State Helpers ----

    private void showLoading() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (rv != null) rv.setVisibility(View.GONE);
        if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
        Log.d(TAG, "showLoading");
    }

    private void hideLoading() {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        Log.d(TAG, "hideLoading");
    }

    private void showContent() {
        if (rv != null) rv.setVisibility(View.VISIBLE);
        if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
        Log.d(TAG, "showContent: " + schemes.size() + " schemes");
    }

    private void showEmptyState(String reason) {
        // Message is static inside the LinearLayout XML — just make it visible
        if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
        if (rv != null) rv.setVisibility(View.GONE);
        Log.d(TAG, "showEmptyState: " + reason);
    }

    // ---- Data Loading ----

    private void loadRecommendations() {
        showLoading();
        Log.d(TAG, "Fetching User Profile...");

        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fUser == null) {
            Log.e(TAG, "No authenticated user.");
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            hideLoading();
            finish();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("user_profiles")
                .document(fUser.getUid())
                .get()
                .addOnSuccessListener(profileSnap -> {
                    UserProfile userProfile = profileSnap.toObject(UserProfile.class);
                    if (userProfile == null || userProfile.getAge() <= 0) {
                        Log.w(TAG, "Profile incomplete.");
                        Toast.makeText(this, "Please complete your profile first", Toast.LENGTH_LONG).show();
                        hideLoading();
                        finish();
                        return;
                    }
                    Log.d(TAG, "Profile OK. Age=" + userProfile.getAge()
                            + " Income=" + userProfile.getIncome()
                            + " Cat=" + userProfile.getCategory());
                    fetchWelfareSchemes(userProfile);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firebase Error fetching profile: ", e);
                    hideLoading();
                    showEmptyState("profile_load_failed");
                    Toast.makeText(this, "Failed to load profile. Check your connection.", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void fetchWelfareSchemes(UserProfile userProfile) {
        Log.d(TAG, "Fetching Schemes...");
        FirebaseFirestore.getInstance()
                .collection("welfare_schemes")
                .get()
                .addOnSuccessListener(result -> {
                    Log.d(TAG, "Schemes fetched. Count: " + result.size());
                    List<Scheme> allSchemes = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : result.getDocuments()) {
                        Scheme s = doc.toObject(Scheme.class);
                        if (s == null) continue;
                        s.setId(doc.getId());
                        allSchemes.add(s);
                    }
                    processRecommendations(userProfile, allSchemes);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firebase Error fetching schemes: ", e);
                    hideLoading();
                    showEmptyState("schemes_load_failed");
                    Toast.makeText(this, "Failed to load data.", Toast.LENGTH_LONG).show();
                });
    }

    private void processRecommendations(UserProfile userProfile, List<Scheme> allSchemes) {
        Log.d(TAG, "Running AI engine on " + allSchemes.size() + " schemes...");
        try {
            List<Scheme> sorted = RecommendationEngine.getPredictions(this, userProfile, allSchemes);
            schemes.clear();
            schemes.addAll(sorted);
            adapter.notifyDataSetChanged();
            hideLoading();

            if (schemes.isEmpty()) {
                showEmptyState("no_results");
            } else {
                showContent();
                Log.d(TAG, "Top: " + schemes.get(0).getTitle());
            }
        } catch (Exception e) {
            Log.e(TAG, "TFLite Engine Crash: ", e);
            hideLoading();
            showEmptyState("engine_error");
            Toast.makeText(this, "Error processing recommendations.", Toast.LENGTH_LONG).show();
        }
    }
}
