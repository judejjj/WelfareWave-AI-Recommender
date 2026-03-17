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
import java.util.Iterator;
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

            // --- Step 2: Strict Category & Rules Filter (Hybrid) ---
            // Keep only schemes whose category matches the user's category,
            // OR schemes that are "General" (universally applicable).
            // AND adhere to strict Caste, Govt Employee, and Sex rules.
            List<Scheme> filteredSchemes = new ArrayList<>(allSchemes);
            Iterator<Scheme> iterator = filteredSchemes.iterator();
            
            while (iterator.hasNext()) {
                Scheme scheme = iterator.next();
                
                // 1. Category Mismatch
                String schemeCat = scheme.getBeneficiaryType() != null ? scheme.getBeneficiaryType().trim() : "";
                String userCat = user.getCategory() != null ? user.getCategory().trim() : "";
                if (!schemeCat.equalsIgnoreCase(userCat) && !schemeCat.equalsIgnoreCase("General")) {
                    Log.d(TAG, "Filtered OUT (Category): '" + scheme.getTitle() + "'");
                    iterator.remove();
                    continue;
                }

                // 2. Sex Mismatch
                String schemeSex = scheme.getTargetSex() != null ? scheme.getTargetSex().trim() : "";
                String userSex = user.getSex() != null ? user.getSex().trim() : "";
                if (!schemeSex.isEmpty() && !schemeSex.equalsIgnoreCase("All") && !schemeSex.equalsIgnoreCase(userSex)) {
                    Log.d(TAG, "Filtered OUT (Sex Rule): '" + scheme.getTitle() + "' expects '" + schemeSex + "' but got '" + userSex + "'");
                    iterator.remove();
                    continue;
                }

                // 3. Caste Mismatch
                String schemeCaste = scheme.getTargetCaste() != null ? scheme.getTargetCaste().trim() : "";
                String userCaste = user.getCaste() != null ? user.getCaste().trim() : "";
                boolean casteMismatch = !schemeCaste.isEmpty() && !schemeCaste.equalsIgnoreCase("All") && !schemeCaste.toUpperCase().contains(userCaste.toUpperCase());
                if (casteMismatch) {
                    Log.d(TAG, "Filtered OUT (Caste Rule): '" + scheme.getTitle() + "' expects '" + schemeCaste + "' but got '" + userCaste + "'");
                    iterator.remove();
                    continue;
                }
                
                // 4. Strict Govt Employee Rule
                if (!scheme.isAllowsGovtEmployee() && user.isHasGovtEmployee()) {
                    Log.d(TAG, "Filtered OUT (Govt Employee Rule): '" + scheme.getTitle() + "' does not allow govt employees.");
                    iterator.remove();
                    continue;
                }

                // 5. Income Cap filter
                if (scheme.getIncomeCap() > 0 && user.getIncome() > scheme.getIncomeCap()) {
                    Log.d(TAG, "Filtered OUT (Income Cap): '" + scheme.getTitle() + "' expects max " + scheme.getIncomeCap() + " but user has " + user.getIncome());
                    iterator.remove();
                    continue;
                }
            }

            Log.d(TAG, "getPredictions: " + allSchemes.size() + " total schemes → "
                    + filteredSchemes.size() + " after ALL strict rule filters.");

            // --- Step 3: Point-Based Scoring Phase ---
            // Calculate a total score for each scheme based on ML baseline + weighted rules.
            Collections.sort(filteredSchemes, (s1, s2) -> {
                double totalScore1 = calculateTotalScore(s1, user, schemeScores.get(s1));
                double totalScore2 = calculateTotalScore(s2, user, schemeScores.get(s2));

                return Double.compare(totalScore2, totalScore1); // Descending
            });

            Log.d(TAG, "getPredictions: Sorted with Point-Based Scoring Algorithm.");
            return filteredSchemes;

        } catch (IOException e) {
            Log.e(TAG, "Prediction failed", e);
            return allSchemes;
        } catch (Exception e) {
            Log.e(TAG, "TFLite Engine Crash: Unexpected error during inference. Returning unsorted schemes.", e);
            return allSchemes;
        }
    }

    /**
     * Point-Based Scoring Algorithm:
     * - Base Score: ML Probability
     * - Caste Match: +100.0
     * - Sex Match: +50.0
     * - Category Match: +10.0
     */
    private static double calculateTotalScore(Scheme s, UserProfile user, Float mlScore) {
        double totalScore = (mlScore != null ? mlScore : 0.0);

        // 1. Caste Match (+100)
        String schemeCaste = s.getTargetCaste() != null ? s.getTargetCaste().trim() : "";
        String userCaste = user.getCaste() != null ? user.getCaste().trim() : "";
        if (!schemeCaste.isEmpty() && !schemeCaste.equalsIgnoreCase("All")) {
            if (schemeCaste.toUpperCase().contains(userCaste.toUpperCase())) {
                totalScore += 100.0;
            }
        }

        // 2. Sex Match (+50)
        String schemeSex = s.getTargetSex() != null ? s.getTargetSex().trim() : "";
        String userSex = user.getSex() != null ? user.getSex().trim() : "";
        if (!schemeSex.isEmpty() && !schemeSex.equalsIgnoreCase("All")) {
            if (schemeSex.equalsIgnoreCase(userSex)) {
                totalScore += 50.0;
            }
        }

        // 3. Category Match (+10)
        String schemeCat = s.getBeneficiaryType() != null ? s.getBeneficiaryType().trim() : "";
        String userCat = user.getCategory() != null ? user.getCategory().trim() : "";
        if (schemeCat.equalsIgnoreCase(userCat)) {
            totalScore += 10.0;
        }

        return totalScore;
    }
}
