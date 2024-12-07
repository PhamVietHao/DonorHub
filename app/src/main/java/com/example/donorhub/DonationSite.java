package com.example.donorhub;

import java.util.List;

public class DonationSite {
    private String id;
    private String name;
    private String address;
    private String dateOfEvent; // New attribute for the date of the event
    private String startTime; // New attribute for the start time of the event
    private String endTime; // New attribute for the end time of the event
    private List<String> requiredBloodTypes;
    private double latitude;
    private double longitude;
    private String adminId; // Attribute to store the admin user ID

    // Constructors
    public DonationSite() {}

    public DonationSite(String id, String name, String address, String dateOfEvent, String startTime, String endTime, List<String> requiredBloodTypes, double latitude, double longitude, String adminId) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.dateOfEvent = dateOfEvent;
        this.startTime = startTime;
        this.endTime = endTime;
        this.requiredBloodTypes = requiredBloodTypes;
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

    public String getDateOfEvent() { return dateOfEvent; }
    public void setDateOfEvent(String dateOfEvent) { this.dateOfEvent = dateOfEvent; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public List<String> getRequiredBloodTypes() { return requiredBloodTypes; }
    public void setRequiredBloodTypes(List<String> requiredBloodTypes) { this.requiredBloodTypes = requiredBloodTypes; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }
}
