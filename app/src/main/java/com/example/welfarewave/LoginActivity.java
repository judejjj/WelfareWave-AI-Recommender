package com.example.welfarewave;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import android.widget.TextView;

public class LoginActivity extends BaseActivity {

    private FirebaseAuth auth;
    private TextView btnLanguageToggle;

    private EditText etEmail;
    private EditText etPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        MaterialButton btnGuest = findViewById(R.id.btnGuest);
        TextView tvRegister = findViewById(R.id.tvRegister);
        TextView tvAdminLogin = findViewById(R.id.tvAdminLogin);

        btnLanguageToggle = findViewById(R.id.btnLanguageToggle);
        updateLanguageToggleText();
        if (btnLanguageToggle != null) {
            btnLanguageToggle.setOnClickListener(v -> toggleLanguage());
        }

        tvRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        tvAdminLogin.setOnClickListener(v -> startActivity(new Intent(this, AdminLoginActivity.class)));

        btnLogin.setOnClickListener(v -> doLogin());
        btnGuest.setOnClickListener(v -> {
            Intent i = new Intent(this, DashboardActivity.class);
            i.putExtra(DashboardActivity.EXTRA_GUEST, true);
            startActivity(i);
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        }
    }

    private void doLogin() {
        String email = etEmail.getText() == null ? "" : etEmail.getText().toString().trim();
        String password = etPassword.getText() == null ? "" : etPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(this, DashboardActivity.class);
                    i.putExtra(DashboardActivity.EXTRA_GUEST, false);
                    startActivity(i);
                    finish();
                })
                .addOnFailureListener(
                        e -> Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
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
