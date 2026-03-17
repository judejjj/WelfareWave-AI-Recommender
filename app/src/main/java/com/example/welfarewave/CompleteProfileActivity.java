package com.example.welfarewave;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompleteProfileActivity extends BaseActivity {

    private TextInputEditText etAge, etIncome, etDob, etEducation, etFamilyMembers, etFatherOccupation, etMotherOccupation;
    private Spinner spinnerCategory, spinnerRelationship, spinnerCaste;
    private RadioGroup rgSex, rgGovtEmployee;
    private RadioButton rbMale, rbFemale, rbGovtYes, rbGovtNo;
    private MaterialButton btnSubmitProfile;

    private final List<String> categories = Arrays.asList(
            "General", "Students", "Farmers", "Senior Citizens", "Women", "Disabled");
    
    private final List<String> relationships = Arrays.asList(
            "Unmarried", "Married", "Divorced");

    private final List<String> castes = Arrays.asList(
            "General", "OBC", "OEC", "SC", "ST");

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
        
        etDob = findViewById(R.id.etDob);
        etEducation = findViewById(R.id.etEducation);
        etFamilyMembers = findViewById(R.id.etFamilyMembers);
        etFatherOccupation = findViewById(R.id.etFatherOccupation);
        etMotherOccupation = findViewById(R.id.etMotherOccupation);
        spinnerRelationship = findViewById(R.id.spinnerRelationship);
        spinnerCaste = findViewById(R.id.spinnerCaste);
        rgSex = findViewById(R.id.rgSex);
        rgGovtEmployee = findViewById(R.id.rgGovtEmployee);
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);
        rbGovtYes = findViewById(R.id.rbGovtYes);
        rbGovtNo = findViewById(R.id.rbGovtNo);
        
        btnSubmitProfile = findViewById(R.id.btnSubmitProfile);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(adapter);

        ArrayAdapter<String> relAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, relationships);
        spinnerRelationship.setAdapter(relAdapter);

        ArrayAdapter<String> casteAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, castes);
        spinnerCaste.setAdapter(casteAdapter);
        
        etDob.setOnClickListener(v -> showDatePicker());

        btnSubmitProfile.setOnClickListener(v -> confirmAndSaveProfile());

        // Pre-fill existing data from Firestore
        loadExistingProfile();
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(CompleteProfileActivity.this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year1);
                    etDob.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
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
        
        String dob = snapshot.getString("dob");
        if (dob != null) etDob.setText(dob);
        
        String education = snapshot.getString("education");
        if (education != null) etEducation.setText(education);
        
        Long familyMembers = snapshot.getLong("familyMembers");
        if (familyMembers != null) etFamilyMembers.setText(String.valueOf(familyMembers));
        
        String fatherOccupation = snapshot.getString("fatherOccupation");
        if (fatherOccupation != null) etFatherOccupation.setText(fatherOccupation);
        
        String motherOccupation = snapshot.getString("motherOccupation");
        if (motherOccupation != null) etMotherOccupation.setText(motherOccupation);
        
        String sex = snapshot.getString("sex");
        if ("Male".equals(sex)) rbMale.setChecked(true);
        else if ("Female".equals(sex)) rbFemale.setChecked(true);
        
        Boolean hasGovtEmployee = snapshot.getBoolean("hasGovtEmployee");
        if (hasGovtEmployee != null) {
            if (hasGovtEmployee) rbGovtYes.setChecked(true);
            else rbGovtNo.setChecked(true);
        }
        
        String relStatus = snapshot.getString("relationshipStatus");
        if (relStatus != null) {
            int idx = relationships.indexOf(relStatus.trim());
            if (idx >= 0) spinnerRelationship.setSelection(idx);
        }
        
        String caste = snapshot.getString("caste");
        if (caste != null) {
            int idx = castes.indexOf(caste.trim());
            if (idx >= 0) spinnerCaste.setSelection(idx);
        }

        // If at least age exists, this is an edit — update button label
        if (age != null && age > 0) {
            btnSubmitProfile.setText("Update Profile");
        }
    }

    private void confirmAndSaveProfile() {
        String ageStr = etAge.getText().toString().trim();
        String incomeStr = etIncome.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        
        String dob = etDob.getText().toString().trim();
        String education = etEducation.getText().toString().trim();
        String relationshipStatus = spinnerRelationship.getSelectedItem().toString();
        String caste = spinnerCaste.getSelectedItem().toString();
        String familyMembersStr = etFamilyMembers.getText().toString().trim();
        String fatherOccupation = etFatherOccupation.getText().toString().trim();
        String motherOccupation = etMotherOccupation.getText().toString().trim();
        
        int selectedSexId = rgSex.getCheckedRadioButtonId();
        int selectedGovtId = rgGovtEmployee.getCheckedRadioButtonId();

        if (TextUtils.isEmpty(ageStr) || TextUtils.isEmpty(incomeStr) || TextUtils.isEmpty(dob) ||
            TextUtils.isEmpty(education) || TextUtils.isEmpty(familyMembersStr) || 
            TextUtils.isEmpty(fatherOccupation) || TextUtils.isEmpty(motherOccupation) ||
            selectedSexId == -1 || selectedGovtId == -1) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int age;
        double income;
        int familyMembersCount;
        try {
            age = Integer.parseInt(ageStr);
            income = Double.parseDouble(incomeStr);
            familyMembersCount = Integer.parseInt(familyMembersStr);
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
        if (familyMembersCount < 1) {
            Toast.makeText(this, "Family members must be at least 1", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String sex = ((RadioButton) findViewById(selectedSexId)).getText().toString();
        boolean hasGovtEmployee = selectedGovtId == R.id.rbGovtYes;

        Map<String, Object> updates = new HashMap<>();
        updates.put("age", age);
        updates.put("income", income);
        updates.put("category", category);
        updates.put("dob", dob);
        updates.put("education", education);
        updates.put("relationshipStatus", relationshipStatus);
        updates.put("caste", caste);
        updates.put("familyMembers", familyMembersCount);
        updates.put("fatherOccupation", fatherOccupation);
        updates.put("motherOccupation", motherOccupation);
        updates.put("sex", sex);
        updates.put("hasGovtEmployee", hasGovtEmployee);

        new AlertDialog.Builder(this)
                .setTitle("Confirm Submission")
                .setMessage("Are you sure you want to submit your profile information?")
                .setPositiveButton("Yes", (dialog, which) -> executeSave(user.getUid(), updates))
                .setNegativeButton("No", null)
                .show();
    }

    private void executeSave(String uid, Map<String, Object> updates) {

        btnSubmitProfile.setEnabled(false);

        FirebaseFirestore.getInstance()
                .collection("user_profiles")
                .document(uid)
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
