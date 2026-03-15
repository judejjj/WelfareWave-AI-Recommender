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

    public UserProfile() {
        // Required empty constructor for Firestore
    }

    public UserProfile(String id, String name, String email, int age, double income, String category) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
        this.income = income;
        this.category = category;
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
}
