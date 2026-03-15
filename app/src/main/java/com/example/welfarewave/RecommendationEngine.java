package com.example.welfarewave;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommendationEngine {

    private static final String TAG = "AI_DEBUG";
    private static final String MODEL_PATH = "scheme_recommender.tflite";

    /**
     * Loads the TFLite model file from assets into a MappedByteBuffer for efficient memory access.
     */
    private static MappedByteBuffer loadModelFile(Context context) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(MODEL_PATH);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        inputStream.close();
        return buffer;
    }

    /**
     * Maps a human-readable category string to the integer used during model training.
     * Falls back to 0 (General) for any unknown or null string.
     */
    private static int mapCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            Log.w(TAG, "mapCategory: null or empty category, defaulting to 0 (General)");
            return 0;
        }
        switch (category.trim().toLowerCase()) {
            case "students":       return 1;
            case "farmers":        return 2;
            case "senior citizens":return 3;
            case "women":          return 4;
            case "disabled":       return 5;
            case "general":        return 0;
            default:
                Log.w(TAG, "mapCategory: Unknown category '" + category + "', defaulting to 0 (General)");
                return 0;
        }
    }

    /**
     * Runs TFLite inference to rank all schemes for a user.
     * Returns the list sorted by predicted probability (highest first).
     * On any failure, returns the original unsorted list so the app does NOT crash.
     */
    public static List<Scheme> getPredictions(Context context, UserProfile user, List<Scheme> allSchemes) {
        if (allSchemes == null || allSchemes.isEmpty()) {
            Log.w(TAG, "getPredictions: allSchemes is null or empty, returning as-is.");
            return allSchemes;
        }
        if (user == null) {
            Log.w(TAG, "getPredictions: UserProfile is null, returning unsorted list.");
            return allSchemes;
        }

        try {
            Log.d(TAG, "getPredictions: Loading TFLite model from assets...");
            MappedByteBuffer modelBuffer = loadModelFile(context);
            Interpreter interpreter = new Interpreter(modelBuffer);
            Log.d(TAG, "getPredictions: Interpreter initialized successfully.");

            // --- Input Normalization ---
            // Must exactly match the Python training script's preprocessing:
            //   age / 100.0, income / 1_000_000.0, category / 5.0
            int mappedCategoryInt = mapCategory(user.getCategory());
            float scaledAge      = (float) user.getAge()      / 100.0f;
            float scaledIncome   = (float) user.getIncome()   / 1_000_000.0f;
            float scaledCategory = (float) mappedCategoryInt  / 5.0f;

            Log.d(TAG, "getPredictions: Input → age=" + scaledAge
                    + ", income=" + scaledIncome
                    + ", category=" + scaledCategory
                    + " (raw: age=" + user.getAge()
                    + ", income=" + user.getIncome()
                    + ", cat='" + user.getCategory() + "' → " + mappedCategoryInt + ")");

            // Strictly typed float[][] — TFLite is strict about data types
            float[][] input  = new float[][]{{scaledAge, scaledIncome, scaledCategory}};
            float[][] output = new float[1][12];

            interpreter.run(input, output);
            interpreter.close();

            Log.d(TAG, "getPredictions: Inference complete. Raw output probabilities:");
            for (int i = 0; i < 12; i++) {
                Log.d(TAG, "  scheme[" + i + "] = " + output[0][i]);
            }

            // --- Step 1: Map probabilities to schemes by index ---
            final Map<Scheme, Float> schemeScores = new HashMap<>();
            for (int i = 0; i < allSchemes.size(); i++) {
                Scheme scheme = allSchemes.get(i);
                // Only the first 12 schemes get a real score; extras get 0
                float score = (i < 12) ? output[0][i] : 0.0f;
                schemeScores.put(scheme, score);
            }

            // --- Step 2: Strict Category Filter (Hybrid) ---
            // Keep only schemes whose category matches the user's category,
            // OR schemes that are "General" (universally applicable).
            String userCategory = user.getCategory() != null ? user.getCategory().trim() : "";
            List<Scheme> filteredSchemes = new ArrayList<>();
            for (Scheme scheme : allSchemes) {
                String schemeCategory = scheme.getBeneficiaryType() != null
                        ? scheme.getBeneficiaryType().trim()
                        : "";
                boolean isGeneral = schemeCategory.equalsIgnoreCase("General");
                boolean matchesUser = schemeCategory.equalsIgnoreCase(userCategory);
                if (isGeneral || matchesUser) {
                    filteredSchemes.add(scheme);
                } else {
                    Log.d(TAG, "Filtered OUT: '" + scheme.getTitle()
                            + "' (category: '" + schemeCategory
                            + "') does not match user category: '" + userCategory + "'");
                }
            }

            Log.d(TAG, "getPredictions: " + allSchemes.size() + " total schemes → "
                    + filteredSchemes.size() + " after category filter (user: '" + userCategory + "')");

            // --- Step 3: Sort filtered list by TFLite score, descending ---
            Collections.sort(filteredSchemes, (s1, s2) -> {
                Float score1 = schemeScores.get(s1);
                Float score2 = schemeScores.get(s2);
                if (score1 == null) score1 = 0f;
                if (score2 == null) score2 = 0f;
                return Float.compare(score2, score1); // descending
            });

            Log.d(TAG, "getPredictions: Sorted. Top recommendation: "
                    + (filteredSchemes.isEmpty() ? "none" : filteredSchemes.get(0).getTitle()));

            return filteredSchemes;

        } catch (IOException e) {
            Log.e(TAG, "TFLite Engine Crash: Failed to load model file. Returning unsorted schemes.", e);
            return allSchemes;
        } catch (Exception e) {
            Log.e(TAG, "TFLite Engine Crash: Unexpected error during inference. Returning unsorted schemes.", e);
            return allSchemes;
        }
    }
}
