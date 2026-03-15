package com.example.welfarewave;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminAddSchemeActivity extends BaseActivity {

    public static final String EXTRA_SCHEME_ID = "extra_scheme_id";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_DESCRIPTION = "extra_description";
    public static final String EXTRA_BENEFICIARY_TYPE = "extra_beneficiary_type";
    public static final String EXTRA_ELIGIBILITY = "extra_eligibility";
    public static final String EXTRA_BENEFITS = "extra_benefits";
    public static final String EXTRA_APPLICATION_URL = "extra_application_url";

    private FirebaseFirestore db;

    private EditText etTitle;
    private EditText etDescription;
    private Spinner spCategory;
    private EditText etEligibility;
    private EditText etBenefits;
    private EditText etApplicationUrl;

    private String editingSchemeId;
    private MaterialButton btnAdd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_scheme);

        db = FirebaseFirestore.getInstance();

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        spCategory = findViewById(R.id.spCategory);
        etEligibility = findViewById(R.id.etEligibility);
        etBenefits = findViewById(R.id.etBenefits);
        etApplicationUrl = findViewById(R.id.etApplicationUrl);
        btnAdd = findViewById(R.id.btnAddScheme);

        // Force consistent category strings via dropdown (prevents mismatches like
        // "Student" vs "Students")
        ArrayAdapter<CharSequence> catAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.scheme_categories,
                android.R.layout.simple_spinner_item);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(catAdapter);

        // If launched from Admin dashboard "Edit", prefill fields and switch to update
        // mode.
        editingSchemeId = getIntent().getStringExtra(EXTRA_SCHEME_ID);
        if (!TextUtils.isEmpty(editingSchemeId)) {
            etTitle.setText(getIntent().getStringExtra(EXTRA_TITLE));
            etDescription.setText(getIntent().getStringExtra(EXTRA_DESCRIPTION));
            String existingCategory = getIntent().getStringExtra(EXTRA_BENEFICIARY_TYPE);
            setSpinnerToValue(spCategory, existingCategory);
            etEligibility.setText(getIntent().getStringExtra(EXTRA_ELIGIBILITY));
            etBenefits.setText(getIntent().getStringExtra(EXTRA_BENEFITS));
            etApplicationUrl.setText(getIntent().getStringExtra(EXTRA_APPLICATION_URL));
            btnAdd.setText(getString(R.string.update_scheme));
        } else {
            btnAdd.setText(getString(R.string.add_scheme));
        }

        btnAdd.setOnClickListener(v -> saveScheme());
    }

    private void saveScheme() {
        String title = text(etTitle);
        String description = text(etDescription);
        int selectedPos = spCategory.getSelectedItemPosition();
        String category = spCategory.getSelectedItem() == null ? "" : spCategory.getSelectedItem().toString();
        String eligibilityRules = text(etEligibility);
        String benefits = text(etBenefits);
        String applicationUrl = text(etApplicationUrl);

        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Title is required");
            etTitle.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(description)) {
            etDescription.setError("Description is required");
            etDescription.requestFocus();
            return;
        }
        if (selectedPos <= 0 || TextUtils.isEmpty(category) || "Select Category".equalsIgnoreCase(category)) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_LONG).show();
            spCategory.requestFocus();
            return;
        }

        Map<String, Object> scheme = new HashMap<>();
        scheme.put("title", title);
        scheme.put("description", description);
        // Write both keys to stay compatible with older reads while standardizing on
        // "category"
        scheme.put("category", category);
        scheme.put("beneficiaryType", category);
        scheme.put("eligibilityRules", eligibilityRules);
        scheme.put("benefits", benefits);
        scheme.put("applicationUrl", applicationUrl);

        if (!TextUtils.isEmpty(editingSchemeId)) {
            scheme.put("updatedAt", FieldValue.serverTimestamp());
            // Update timestamp so it shows up as "new" logic if desired, OR keep original
            // creation time.
            // User wants "New scheme" logic. If we want "Latest" to reflect added time, we
            // use createdAt.
            // If we want "Latest" to reflect Updated time, we update this.
            // Given the request "when i create a new one it doesnt showup", it implies
            // sticking to creation time usually,
            // BUT the user might want bumped schemes to show up.
            // However, standard "Latest" usually means "Newly Added".
            // The issue was "Latest Schemes" not updating. The fix is ensuring we SORT by
            // such a field.
            // I will ensure 'timestamp' is set on creation.
            // For updates, I will NOT update 'timestamp' to avoid re-shuffling old schemes
            // to the top unless intended.
            // actually, the previous code had: scheme.put("timestamp",
            // FieldValue.serverTimestamp()); inside the UPDATE block too.
            // I will keep that behavior if it was intended to bump, OR better: adhere to
            // "Newly Added".
            // detailed request: "when i create a new one it doesnt showup".
            // So creation is key. I will set timestamp on creation.

            db.collection("welfare_schemes")
                    .document(editingSchemeId)
                    .update(scheme) // Use update instead of set to avoid overwriting other fields if any
                    .addOnSuccessListener(ref -> {
                        Toast.makeText(this, "Scheme updated", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast
                            .makeText(this, "Failed to update scheme: " + e.getMessage(), Toast.LENGTH_LONG).show());
        } else {
            scheme.put("createdAt", FieldValue.serverTimestamp());
            scheme.put("timestamp", FieldValue.serverTimestamp()); // This is the sort key

            db.collection("welfare_schemes")
                    .add(scheme)
                    .addOnSuccessListener(ref -> {
                        Toast.makeText(this, "Scheme added", Toast.LENGTH_SHORT).show();
                        clear();
                    })
                    .addOnFailureListener(e -> Toast
                            .makeText(this, "Failed to add scheme: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }

    private static String text(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private static void setSpinnerToValue(Spinner spinner, String value) {
        if (value == null)
            return;
        for (int i = 0; i < spinner.getCount(); i++) {
            Object item = spinner.getItemAtPosition(i);
            if (item != null && value.equalsIgnoreCase(item.toString())) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private void clear() {
        etTitle.setText("");
        etDescription.setText("");
        spCategory.setSelection(0);
        etEligibility.setText("");
        etBenefits.setText("");
        etApplicationUrl.setText("");
        etTitle.requestFocus();
    }
}
