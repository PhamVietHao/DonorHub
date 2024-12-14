package com.example.donorhub.Models;

public class User {
    private String id;
    private String name;
    private String email;
    private String password;
    private boolean isAdmin;
    private boolean isAdminSite; // New attribute
    private String bloodType; // New attribute
    private int milestone; // New attribute

    // Constructors
    public User() {}

    public User(String id, String name, String email, String password, boolean isAdmin, boolean isAdminSite, String bloodType, int milestone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.isAdmin = isAdmin;
        this.isAdminSite = isAdminSite; // Initialize new attribute
        this.bloodType = bloodType; // Initialize new attribute
        this.milestone = milestone; // Initialize new attribute
    }

    // Getters and Setters
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public boolean isAdminSite() {
        return isAdminSite;
    }

    public void setAdminSite(boolean adminSite) {
        isAdminSite = adminSite;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public int getMilestone() {
        return milestone;
    }

    public void setMilestone(int milestone) {
        this.milestone = milestone;
    }
}