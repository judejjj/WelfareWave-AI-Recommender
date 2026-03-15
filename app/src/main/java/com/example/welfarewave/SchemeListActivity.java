package com.example.welfarewave;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class SchemeListActivity extends BaseActivity {

    public static final String EXTRA_FILTER_CATEGORY = "extra_filter_category";
    public static final String CATEGORY_FILTER = "CATEGORY_FILTER";

    private final List<Scheme> schemes = new ArrayList<>();
    private SchemeAdapter adapter;
    private TextView btnLanguageToggle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheme_list);

        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvSubtitle = findViewById(R.id.tvSubtitle);
        TextView tvEmpty = findViewById(R.id.tvEmpty);
        RecyclerView rv = findViewById(R.id.rvSchemes);

        btnLanguageToggle = findViewById(R.id.btnLanguageToggle);
        updateLanguageToggleText();
        if (btnLanguageToggle != null) {
            btnLanguageToggle.setOnClickListener(v -> toggleLanguage());
        }

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SchemeAdapter(schemes, scheme -> {
            Intent i = new Intent(this, SchemeDetailActivity.class);
            i.putExtra(SchemeDetailActivity.EXTRA_TITLE, scheme.getTitle());
            i.putExtra(SchemeDetailActivity.EXTRA_DESCRIPTION, scheme.getDescription());
            i.putExtra(SchemeDetailActivity.EXTRA_ELIGIBILITY, scheme.getEligibilityRules());
            i.putExtra(SchemeDetailActivity.EXTRA_BENEFITS, scheme.getBenefits());
            i.putExtra(SchemeDetailActivity.EXTRA_APPLICATION_URL, scheme.getApplicationUrl());
            startActivity(i);
        });
        rv.setAdapter(adapter);

        String category = getIntent().getStringExtra(CATEGORY_FILTER);
        if (category == null) {
            // Backward compatibility with older callers
            category = getIntent().getStringExtra(EXTRA_FILTER_CATEGORY);
        }
        
        String displayCategory = category;
        if ("Students".equals(category)) displayCategory = getString(R.string.cat_students);
        else if ("Farmers".equals(category)) displayCategory = getString(R.string.cat_farmers);
        else if ("Women".equals(category)) displayCategory = getString(R.string.cat_women);
        else if ("Senior Citizens".equals(category)) displayCategory = getString(R.string.cat_senior_citizens);
        else if ("Disabled".equals(category)) displayCategory = getString(R.string.cat_disabled);

        tvTitle.setText(getString(R.string.all_schemes));
        tvSubtitle.setText(category == null || category.trim().isEmpty() ? "" : ("Category: " + displayCategory));

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("welfare_schemes");
        if (category != null && !category.trim().isEmpty()) {
            // Canonical key is "category" (admin dropdown writes consistent values).
            query = query.whereEqualTo("category", category);
        }

        query.get()
                .addOnSuccessListener(result -> {
                    schemes.clear();
                    for (DocumentSnapshot doc : result.getDocuments()) {
                        Scheme s = doc.toObject(Scheme.class);
                        if (s == null)
                            continue;
                        s.setId(doc.getId());
                        schemes.add(s);
                    }
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(schemes.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    tvEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Failed to load schemes: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
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
