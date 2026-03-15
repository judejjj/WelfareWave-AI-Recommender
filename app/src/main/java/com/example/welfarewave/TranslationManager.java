package com.example.welfarewave;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

public class TranslationManager {
    private static final String TAG = "TranslationManager";
    private static TranslationManager instance;
    private final Translator englishMalayalamTranslator;

    private TranslationManager() {
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage("ml")
                .build();
        englishMalayalamTranslator = Translation.getClient(options);
    }

    public static synchronized TranslationManager getInstance() {
        if (instance == null) {
            instance = new TranslationManager();
        }
        return instance;
    }

    public void downloadModelIfNeeded(Runnable onSuccess, Runnable onFailure) {
        DownloadConditions conditions = new DownloadConditions.Builder().build();
        englishMalayalamTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Malayalam language model ready.");
                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to download Malayalam model.", e);
                    if (onFailure != null) onFailure.run();
                });
    }

    public void translateText(String text, OnTranslationCompleteListener listener) {
        if (text == null || text.trim().isEmpty()) {
            if (listener != null) listener.onTranslationSuccess("");
            return;
        }
        englishMalayalamTranslator.translate(text)
                .addOnSuccessListener(listener::onTranslationSuccess)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Translation failed for: " + text, e);
                    if (listener != null) listener.onTranslationFailure(e);
                });
    }

    /**
     * Recursively walks through a View hierarchy and translates:
     * - TextView text (if non-empty)
     * - EditText hints (if non-empty)
     * - ViewGroup children are traversed recursively
     *
     * Safe to call on the root layout of any Activity or Fragment.
     * Skips views that are GONE to avoid unnecessary API calls.
     */
    public void translateView(View view) {
        if (view == null || view.getVisibility() == View.GONE) return;

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                translateView(group.getChildAt(i));
            }
        } else if (view instanceof EditText) {
            EditText et = (EditText) view;
            // Translate hint text
            CharSequence hint = et.getHint();
            if (hint != null && hint.length() > 0) {
                translateText(hint.toString(), new OnTranslationCompleteListener() {
                    @Override public void onTranslationSuccess(String t) { et.setHint(t); }
                    @Override public void onTranslationFailure(Exception e) { }
                });
            }
        } else if (view instanceof TextView) {
            TextView tv = (TextView) view;
            CharSequence text = tv.getText();
            if (text != null && text.length() > 0) {
                String original = text.toString().trim();
                if (!original.isEmpty()) {
                    translateText(original, new OnTranslationCompleteListener() {
                        @Override public void onTranslationSuccess(String t) { tv.setText(t); }
                        @Override public void onTranslationFailure(Exception e) { }
                    });
                }
            }
        }
    }

    public interface OnTranslationCompleteListener {
        void onTranslationSuccess(String translatedText);
        void onTranslationFailure(Exception e);
    }
}
