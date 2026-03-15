package com.example.welfarewave;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompleteProfileActivity extends BaseActivity {

    private TextInputEditText etAge, etIncome;
    private Spinner spinnerCategory;
    private MaterialButton btnSubmitProfile;

    private final List<String> categories = Arrays.asList(
            "General", "Students", "Farmers", "Senior Citizens", "Women", "Disabled");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);

        MaterialToolbar toolbar = findViewById(R.id.toolbarProfile);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        etAge = findViewById(R.id.etAge);
        etIncome = findViewById(R.id.etIncome);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSubmitProfile = findViewById(R.id.btnSubmitProfile);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(adapter);

        btnSubmitProfile.setOnClickListener(v -> saveProfile());

        // Pre-fill existing data from Firestore
        loadExistingProfile();
    }

    /**
     * Fetches the current user's Firestore profile and populates the form fields.
     * If data exists, also changes the button label to "Update Profile".
     */
    private void loadExistingProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("user_profiles")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(this::populateFields)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Could not load current profile data", Toast.LENGTH_SHORT).show());
    }

    private void populateFields(DocumentSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) return;

        // Pre-fill Age
        Long age = snapshot.getLong("age");
        if (age != null && age > 0) {
            etAge.setText(String.valueOf(age));
        }

        // Pre-fill Income
        Double income = snapshot.getDouble("income");
        if (income != null && income > 0) {
            // Show as a whole number if no decimal part, otherwise show full value
            if (income == Math.floor(income)) {
                etIncome.setText(String.valueOf(income.longValue()));
            } else {
                etIncome.setText(String.valueOf(income));
            }
        }

        // Pre-select Category spinner
        String category = snapshot.getString("category");
        if (category != null && !category.trim().isEmpty()) {
            int index = categories.indexOf(category.trim());
            if (index >= 0) {
                spinnerCategory.setSelection(index);
            }
        }

        // If at least age exists, this is an edit — update button label
        if (age != null && age > 0) {
            btnSubmitProfile.setText("Update Profile");
        }
    }

    private void saveProfile() {
        String ageStr = etAge.getText().toString().trim();
        String incomeStr = etIncome.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();

        if (TextUtils.isEmpty(ageStr) || TextUtils.isEmpty(incomeStr)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int age;
        double income;
        try {
            age = Integer.parseInt(ageStr);
            income = Double.parseDouble(incomeStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid numbers entered", Toast.LENGTH_SHORT).show();
            return;
        }

        if (age < 1 || age > 120) {
            Toast.makeText(this, "Please enter a valid age (1–120)", Toast.LENGTH_SHORT).show();
            return;
        }
        if (income < 0) {
            Toast.makeText(this, "Income cannot be negative", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("age", age);
        updates.put("income", income);
        updates.put("category", category);

        btnSubmitProfile.setEnabled(false);

        // Use set with merge: works for both new and existing documents
        FirebaseFirestore.getInstance()
                .collection("user_profiles")
                .document(user.getUid())
                .set(updates, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSubmitProfile.setEnabled(true);
                    Toast.makeText(this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
