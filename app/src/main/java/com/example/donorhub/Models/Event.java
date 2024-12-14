package com.example.donorhub.Models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Event {
    private String id;
    private String siteId;
    private String eventName;
    private List<String> userIds;
    private Date startDate;
    private Date startTime;
    private Date endTime;
    private List<String> bloodTypes;
    private List<String> userIdsVolunteer;

    // Constructors
    public Event() {
        this.userIds = new ArrayList<>();
        this.userIdsVolunteer = new ArrayList<>();
    }

    public Event(String id, String siteId, String eventName, List<String> userIds, Date startDate, Date startTime, Date endTime, List<String> bloodTypes, List<String> userIdsVolunteer) {
        this.id = id;
        this.siteId = siteId;
        this.eventName = eventName;
        this.userIds = userIds != null ? userIds : new ArrayList<>();
        this.startDate = startDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.bloodTypes = bloodTypes;
        this.userIdsVolunteer = userIdsVolunteer != null ? userIdsVolunteer : new ArrayList<>();
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

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds != null ? userIds : new ArrayList<>();
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public List<String> getBloodTypes() {
        return bloodTypes;
    }

    public void setBloodTypes(List<String> bloodTypes) {
        this.bloodTypes = bloodTypes;
    }

    public List<String> getUserIdsVolunteer() {
        return userIdsVolunteer;
    }

    public void setUserIdsVolunteer(List<String> userIdsVolunteer) {
        this.userIdsVolunteer = userIdsVolunteer != null ? userIdsVolunteer : new ArrayList<>();
    }
}