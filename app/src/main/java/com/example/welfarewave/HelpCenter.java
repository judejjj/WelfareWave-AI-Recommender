package com.example.welfarewave;

public class HelpCenter {
    private String id;
    private String name;
    private String location;
    private String phone;

    public HelpCenter() {
        // Required empty constructor for Firestore
    }

    public HelpCenter(String id, String name, String location, String phone) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.phone = phone;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
