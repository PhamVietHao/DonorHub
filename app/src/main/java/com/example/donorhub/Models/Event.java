package com.example.donorhub.Models;

import java.util.Date;

public class Event {
    private String id;
    private String siteId;
    private String userId;
    private Date date;
    private String bloodType;

    // Constructors
    public Event() {}

    public Event(String id, String siteId, String userId, Date date, String bloodType) {
        this.id = id;
        this.siteId = siteId;
        this.userId = userId;
        this.date = date;
        this.bloodType = bloodType;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }
}
