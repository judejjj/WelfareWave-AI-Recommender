package com.example.welfarewave;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminHelpCenterActivity extends BaseActivity {

    private FirebaseFirestore db;
    private EditText etName, etLocation, etPhone;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_help_center);

        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etCenterName);
        etLocation = findViewById(R.id.etCenterLocation);
        etPhone = findViewById(R.id.etCenterPhone);
        MaterialButton btnAdd = findViewById(R.id.btnAddCenter);

        btnAdd.setOnClickListener(v -> saveCenter());
    }

    private void saveCenter() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String location = etLocation.getText() != null ? etLocation.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            etName.setError("Required");
            return;
        }
        if (TextUtils.isEmpty(location)) {
            etLocation.setError("Required");
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Required");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("location", location);
        data.put("phone", phone);

        db.collection("help_centers")
                .add(data)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Help Center added!", Toast.LENGTH_SHORT).show();
                    etName.setText("");
                    etLocation.setText("");
                    etPhone.setText("");
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
