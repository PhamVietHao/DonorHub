package com.example.donorhub.Models;

import java.util.Date;
import java.util.List;

public class Event {
    private String id;
    private String siteId;
    private List<String> userIds; // Array to store all users participating in the event
    private Date startDate; // Start date of the event
    private Date endDate; // End date of the event
    private List<String> bloodTypes; // Array to store multiple blood types needed

    // Constructors
    public Event() {}

    public Event(String id, String siteId, List<String> userIds, Date startDate, Date endDate, List<String> bloodTypes) {
        this.id = id;
        this.siteId = siteId;
        this.userIds = userIds;
        this.startDate = startDate;
        this.endDate = endDate;
        this.bloodTypes = bloodTypes;
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

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<String> getBloodTypes() {
        return bloodTypes;
    }

    public void setBloodTypes(List<String> bloodTypes) {
        this.bloodTypes = bloodTypes;
    }
}