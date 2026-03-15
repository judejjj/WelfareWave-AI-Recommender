package com.example.welfarewave;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Locale;

public class SchemeDetailActivity extends BaseActivity {

    private static final String TAG = "SchemeDetail";

    public static final String EXTRA_TITLE       = "extra_title";
    public static final String EXTRA_DESCRIPTION = "extra_description";
    public static final String EXTRA_ELIGIBILITY = "extra_eligibility";
    public static final String EXTRA_BENEFITS    = "extra_benefits";
    public static final String EXTRA_APPLICATION_URL = "extra_application_url";

    private TextToSpeech textToSpeech;
    private boolean isSpeaking = false;
    private FloatingActionButton fabSpeak;

    // Hold references so TTS can read live (possibly translated) text
    private TextView tvTitle;
    private TextView tvDescription;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheme_detail);

        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbarDetail);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvTitle           = findViewById(R.id.tvTitle);
        tvDescription     = findViewById(R.id.tvDescription);
        TextView tvEligibility = findViewById(R.id.tvEligibility);
        TextView tvBenefits    = findViewById(R.id.tvBenefits);
        MaterialButton btnOpenLink = findViewById(R.id.btnOpenLink);

        String title          = getIntent().getStringExtra(EXTRA_TITLE);
        String description    = getIntent().getStringExtra(EXTRA_DESCRIPTION);
        String eligibility    = getIntent().getStringExtra(EXTRA_ELIGIBILITY);
        String benefits       = getIntent().getStringExtra(EXTRA_BENEFITS);
        String applicationUrl = getIntent().getStringExtra(EXTRA_APPLICATION_URL);

        // 1. Set English content first
        tvTitle.setText(safe(title));
        tvDescription.setText(safeOrNA(description));
        tvEligibility.setText(safeOrNA(eligibility));
        tvBenefits.setText(safeOrNA(benefits));

        // 2. If Malayalam mode is active, translate the ENTIRE view tree —
        //    this covers both the dynamic (Firebase) content AND the hardcoded
        //    section headers ("Overview", "Eligibility", "Benefits") in the XML.
        if ("ml".equals(LocaleHelper.getLanguage(this))) {
            Log.d(TAG, "Malayalam active — running translateView on full content tree.");
            View contentRoot = findViewById(android.R.id.content);
            TranslationManager.getInstance().translateView(contentRoot);
        }

        // 3. Set up TTS — explicitly locked to Malayalam
        fabSpeak = findViewById(R.id.fabSpeak);
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(new Locale("ml", "IN"));
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Malayalam TTS not supported on this device.");
                    fabSpeak.setEnabled(false);
                } else {
                    Log.d(TAG, "TTS ready in Malayalam.");
                    fabSpeak.setEnabled(true);
                }
            } else {
                Log.e(TAG, "TTS initialization failed.");
                fabSpeak.setEnabled(false);
            }
        });

        // 4. TTS reads the CURRENT live text (which may already be translated Malayalam)
        fabSpeak.setOnClickListener(v -> {
            if (isSpeaking) {
                textToSpeech.stop();
                isSpeaking = false;
                fabSpeak.setImageResource(android.R.drawable.ic_lock_silent_mode_off);
            } else {
                // Read what is CURRENTLY on screen — Title + Description
                // (If translateView() already ran, this text is Malayalam)
                String textToSpeak = tvTitle.getText().toString()
                        + ". "
                        + tvDescription.getText().toString();

                // Re-assert locale right before speaking (in case device TTS state changed)
                textToSpeech.setLanguage(new Locale("ml", "IN"));
                textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "TTS_SCHEME");
                isSpeaking = true;
                fabSpeak.setImageResource(android.R.drawable.ic_media_pause);
                Log.d(TAG, "Speaking: " + textToSpeak.substring(0, Math.min(60, textToSpeak.length())) + "...");
            }
        });

        // 5. Apply / Open Link button
        btnOpenLink.setOnClickListener(v -> {
            if (TextUtils.isEmpty(applicationUrl)) {
                Toast.makeText(this, "No application link provided", Toast.LENGTH_LONG).show();
                return;
            }
            String url = applicationUrl.trim();
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } catch (Exception e) {
                Toast.makeText(this, "Could not open link", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static String safeOrNA(String s) {
        if (s == null) return "Not available";
        String t = s.trim();
        return t.isEmpty() ? "Not available" : t;
    }
}
