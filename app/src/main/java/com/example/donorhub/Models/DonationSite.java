package com.example.donorhub.Models;

import java.util.List;

public class DonationSite {
    private String id;
    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private String adminId; // Attribute to store the admin user ID

    // Constructors
    public DonationSite() {}

    public DonationSite(String id, String name, String address, double latitude, double longitude, String adminId) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.adminId = adminId;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }
}