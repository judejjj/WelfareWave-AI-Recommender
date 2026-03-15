package com.example.welfarewave;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDashboardActivity extends BaseActivity {

    private FirebaseFirestore db;
    private final List<Scheme> schemes = new ArrayList<>();
    private AdminSchemeAdapter adapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        db = FirebaseFirestore.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvEmpty = findViewById(R.id.tvEmpty);
        MaterialButton btnGenerateSample = findViewById(R.id.btnGenerateSample);
        RecyclerView rv = findViewById(R.id.rvAdminSchemes);
        FloatingActionButton fab = findViewById(R.id.fabAdd);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminSchemeAdapter(schemes, new AdminSchemeAdapter.Listener() {
            @Override
            public void onEdit(Scheme scheme) {
                Intent i = new Intent(AdminDashboardActivity.this, AdminAddSchemeActivity.class);
                i.putExtra(AdminAddSchemeActivity.EXTRA_SCHEME_ID, scheme.getId());
                i.putExtra(AdminAddSchemeActivity.EXTRA_TITLE, scheme.getTitle());
                i.putExtra(AdminAddSchemeActivity.EXTRA_DESCRIPTION, scheme.getDescription());
                i.putExtra(AdminAddSchemeActivity.EXTRA_BENEFICIARY_TYPE, scheme.getBeneficiaryType());
                i.putExtra(AdminAddSchemeActivity.EXTRA_ELIGIBILITY, scheme.getEligibilityRules());
                i.putExtra(AdminAddSchemeActivity.EXTRA_BENEFITS, scheme.getBenefits());
                i.putExtra(AdminAddSchemeActivity.EXTRA_APPLICATION_URL, scheme.getApplicationUrl());
                startActivity(i);
            }

            @Override
            public void onDelete(Scheme scheme) {
                confirmDelete(scheme);
            }

            @Override
            public void onOpen(Scheme scheme) {
                Intent i = new Intent(AdminDashboardActivity.this, SchemeDetailActivity.class);
                i.putExtra(SchemeDetailActivity.EXTRA_TITLE, scheme.getTitle());
                i.putExtra(SchemeDetailActivity.EXTRA_DESCRIPTION, scheme.getDescription());
                i.putExtra(SchemeDetailActivity.EXTRA_ELIGIBILITY, scheme.getEligibilityRules());
                i.putExtra(SchemeDetailActivity.EXTRA_BENEFITS, scheme.getBenefits());
                i.putExtra(SchemeDetailActivity.EXTRA_APPLICATION_URL, scheme.getApplicationUrl());
                startActivity(i);
            }
        });
        rv.setAdapter(adapter);

        fab.setOnClickListener(v -> startActivity(new Intent(this, AdminAddSchemeActivity.class)));
        btnGenerateSample.setOnClickListener(v -> generateSampleData());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSchemes();
    }

    private void loadSchemes() {
        db.collection("welfare_schemes")
                .get()
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

    private void confirmDelete(Scheme scheme) {
        new AlertDialog.Builder(this)
                .setTitle("Delete scheme?")
                .setMessage("Are you sure? This cannot be undone.")
                .setPositiveButton("Delete", (d, which) -> {
                    if (scheme.getId() == null || scheme.getId().trim().isEmpty()) {
                        Toast.makeText(this, "Missing scheme id", Toast.LENGTH_LONG).show();
                        return;
                    }
                    db.collection("welfare_schemes")
                            .document(scheme.getId())
                            .delete()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                                loadSchemes();
                            })
                            .addOnFailureListener(e -> Toast
                                    .makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .show();
    }

    private void generateSampleData() {
        // Create-once sample docs (no overwrites) so Admin can immediately test
        // Edit/Delete.
        db.runTransaction(transaction -> {
            createIfMissing(transaction, "sample_pm_kisan", schemeMap(
                    "Pradhan Mantri Kisan Samman Nidhi",
                    "Farmers",
                    "₹6,000/year",
                    "Income support for eligible small and marginal farmer families.",
                    "Must be a farmer family; landholding as per scheme rules; Aadhaar linked bank account required.",
                    "₹2,000 installment every 4 months (₹6,000 per year).",
                    "https://pmkisan.gov.in"));

            createIfMissing(transaction, "sample_national_merit_scholarship", schemeMap(
                    "National Merit Scholarship",
                    "Students",
                    "Tuition fee waiver",
                    "Merit-based scholarship for students pursuing higher education.",
                    "Must meet academic merit criteria and income threshold as per state/central guidelines.",
                    "Tuition fee waiver and/or monthly stipend depending on eligibility.",
                    "https://scholarships.gov.in"));

            createIfMissing(transaction, "sample_atal_pension", schemeMap(
                    "Atal Pension Yojana",
                    "Senior Citizens",
                    "Guaranteed Pension",
                    "Pension scheme focused on unorganized sector workers to receive a guaranteed pension after 60.",
                    "Age 18–40 at entry; must have a bank account; regular contributions required.",
                    "Guaranteed pension (₹1,000–₹5,000/month) based on contribution.",
                    "https://www.npscra.nsdl.co.in/scheme-details.php"));

            createIfMissing(transaction, "sample_maternity_benefit", schemeMap(
                    "Maternity Benefit Scheme",
                    "Women",
                    "Paid leave & medical bonus",
                    "Support for pregnant and lactating women through cash incentive and healthcare encouragement.",
                    "Pregnant/lactating women meeting scheme criteria; documentation required.",
                    "Cash incentive for nutrition and medical support; encourages institutional delivery.",
                    "https://wcd.nic.in"));

            createIfMissing(transaction, "sample_disability_assistance", schemeMap(
                    "Disability Assistance Program",
                    "Disabled",
                    "Monthly assistance",
                    "Financial assistance to persons with disabilities for basic needs and mobility support.",
                    "Valid disability certificate; income criteria may apply.",
                    "Monthly assistance and support services depending on state/central guidelines.",
                    "https://www.swavlambancard.gov.in"));

            createIfMissing(transaction, "sample_health_seniors", schemeMap(
                    "Senior Health & Wellness Card",
                    "Senior Citizens",
                    "Free check-ups",
                    "Healthcare support for seniors including periodic health check-ups and discounts at partner clinics.",
                    "Age 60+; valid ID proof required.",
                    "Free health screenings and discounted consultations/medicines at participating centers.",
                    "https://www.mohfw.gov.in"));

            return null;
        }).addOnSuccessListener(unused -> {
            Toast.makeText(this, "Sample data generated", Toast.LENGTH_SHORT).show();
            loadSchemes();
        }).addOnFailureListener(
                e -> Toast.makeText(this, "Sample data failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void createIfMissing(Transaction transaction, String docId, Map<String, Object> data)
            throws FirebaseFirestoreException {
        DocumentSnapshot snap = transaction.get(db.collection("welfare_schemes").document(docId));
        if (!snap.exists()) {
            transaction.set(db.collection("welfare_schemes").document(docId), data);
        }
    }

    private Map<String, Object> schemeMap(String title, String category, String quickBenefit,
            String description, String eligibilityRules, String benefits, String applicationUrl) {
        Map<String, Object> m = new HashMap<>();
        m.put("title", title);
        // keep both keys for compatibility (app currently uses beneficiaryType in
        // several places)
        m.put("beneficiaryType", category);
        m.put("category", category);
        m.put("description", description);
        m.put("eligibilityRules", eligibilityRules);
        m.put("benefits", benefits);
        m.put("applicationUrl", applicationUrl);
        m.put("quickBenefit", quickBenefit);
        m.put("createdAt", FieldValue.serverTimestamp());
        m.put("timestamp", FieldValue.serverTimestamp());
        return m;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_manage_help_centers) {
            startActivity(new Intent(this, AdminHelpCenterActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
