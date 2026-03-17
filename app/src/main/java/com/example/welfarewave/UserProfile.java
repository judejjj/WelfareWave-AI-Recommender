package com.example.welfarewave;

import java.io.Serializable;

/**
 * Firestore model for collection: user_profiles
 */
public class UserProfile implements Serializable {
    private String id;
    private String name;
    private String email;
    private int age;
    private double income;
    private String category;
    
    private String dob;
    private String education;
    private String relationshipStatus;
    private String caste;
    private int familyMembers;
    private String fatherOccupation;
    private String motherOccupation;
    private String sex;
    private boolean hasGovtEmployee;

    public UserProfile() {
        // Required empty constructor for Firestore
    }

    public UserProfile(String id, String name, String email, int age, double income, String category,
                       String dob, String education, String relationshipStatus, String caste,
                       int familyMembers, String fatherOccupation, String motherOccupation,
                       String sex, boolean hasGovtEmployee) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
        this.income = income;
        this.category = category;
        this.dob = dob;
        this.education = education;
        this.relationshipStatus = relationshipStatus;
        this.caste = caste;
        this.familyMembers = familyMembers;
        this.fatherOccupation = fatherOccupation;
        this.motherOccupation = motherOccupation;
        this.sex = sex;
        this.hasGovtEmployee = hasGovtEmployee;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public double getIncome() {
        return income;
    }

    public void setIncome(double income) {
        this.income = income;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getRelationshipStatus() {
        return relationshipStatus;
    }

    public void setRelationshipStatus(String relationshipStatus) {
        this.relationshipStatus = relationshipStatus;
    }

    public String getCaste() {
        return caste;
    }

    public void setCaste(String caste) {
        this.caste = caste;
    }

    public int getFamilyMembers() {
        return familyMembers;
    }

    public void setFamilyMembers(int familyMembers) {
        this.familyMembers = familyMembers;
    }

    public String getFatherOccupation() {
        return fatherOccupation;
    }

    public void setFatherOccupation(String fatherOccupation) {
        this.fatherOccupation = fatherOccupation;
    }

    public String getMotherOccupation() {
        return motherOccupation;
    }

    public void setMotherOccupation(String motherOccupation) {
        this.motherOccupation = motherOccupation;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public boolean isHasGovtEmployee() {
        return hasGovtEmployee;
    }

    public void setHasGovtEmployee(boolean hasGovtEmployee) {
        this.hasGovtEmployee = hasGovtEmployee;
    }
}
