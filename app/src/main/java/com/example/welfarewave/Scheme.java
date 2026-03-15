package com.example.welfarewave;

import java.io.Serializable;

/**
 * Firestore model for collection: welfare_schemes
 *
 * Fields (must match Firestore keys):
 * - title
 * - description
 * - beneficiaryType
 * - eligibilityRules
 * - benefits
 * - applicationUrl
 */
public class Scheme implements Serializable {

    private String id;
    private String title;
    private String description;
    private String beneficiaryType;
    private String eligibilityRules;
    private String benefits;
    private String applicationUrl;

    // Cache translated fields
    private String translatedTitle;
    private String translatedDescription;
    private String translatedEligibilityRules;
    private String translatedBenefits;

    private com.google.firebase.Timestamp timestamp;

    // Required empty constructor for Firestore
    public Scheme() {
    }

    public Scheme(String id, String title, String description, String beneficiaryType,
            String eligibilityRules, String benefits, String applicationUrl,
            com.google.firebase.Timestamp timestamp) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.beneficiaryType = beneficiaryType;
        this.eligibilityRules = eligibilityRules;
        this.benefits = benefits;
        this.applicationUrl = applicationUrl;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBeneficiaryType() {
        return beneficiaryType;
    }

    public void setBeneficiaryType(String beneficiaryType) {
        this.beneficiaryType = beneficiaryType;
    }

    public String getEligibilityRules() {
        return eligibilityRules;
    }

    public void setEligibilityRules(String eligibilityRules) {
        this.eligibilityRules = eligibilityRules;
    }

    public String getBenefits() {
        return benefits;
    }

    public void setBenefits(String benefits) {
        this.benefits = benefits;
    }

    public String getApplicationUrl() {
        return applicationUrl;
    }

    public void setApplicationUrl(String applicationUrl) {
        this.applicationUrl = applicationUrl;
    }

    public com.google.firebase.Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(com.google.firebase.Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getTranslatedTitle() {
        return translatedTitle;
    }

    public void setTranslatedTitle(String translatedTitle) {
        this.translatedTitle = translatedTitle;
    }

    public String getTranslatedDescription() {
        return translatedDescription;
    }

    public void setTranslatedDescription(String translatedDescription) {
        this.translatedDescription = translatedDescription;
    }

    public String getTranslatedEligibilityRules() {
        return translatedEligibilityRules;
    }

    public void setTranslatedEligibilityRules(String translatedEligibilityRules) {
        this.translatedEligibilityRules = translatedEligibilityRules;
    }

    public String getTranslatedBenefits() {
        return translatedBenefits;
    }

    public void setTranslatedBenefits(String translatedBenefits) {
        this.translatedBenefits = translatedBenefits;
    }
}
