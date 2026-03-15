package com.example.welfarewave;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends BaseActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private EditText etName;
    private EditText etEmail;
    private EditText etMobile;
    private EditText etPassword;
    private EditText etConfirmPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etMobile = findViewById(R.id.etMobile);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        MaterialButton btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> doRegister());
    }

    private void doRegister() {
        String name = etName.getText() == null ? "" : etName.getText().toString().trim();
        String email = etEmail.getText() == null ? "" : etEmail.getText().toString().trim();
        String mobile = etMobile.getText() == null ? "" : etMobile.getText().toString().trim();
        String password = etPassword.getText() == null ? "" : etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText() == null ? ""
                : etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(mobile) || mobile.length() != 10) {
            etMobile.setError("A valid 10-digit mobile number is required");
            etMobile.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) {
                        Toast.makeText(this, "Registration successful. Please login.", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                        return;
                    }

                    Map<String, Object> profile = new HashMap<>();
                    profile.put("name", name);
                    profile.put("email", email);
                    profile.put("mobile", mobile);

                    db.collection("user_profiles")
                            .document(user.getUid())
                            .set(profile)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, LoginActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Profile save failed: " + e.getMessage(), Toast.LENGTH_LONG)
                                        .show();
                                startActivity(new Intent(this, LoginActivity.class));
                                finish();
                            });
                })
                .addOnFailureListener(
                        e -> Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
